Meteor.methods({
	//Adds a new game document to the database
	'createGame': function(gameId, playerIds, condition){
		var playersData = {};
		for(var i=0, plen = playerIds.length; i<plen; i++) {
			playersData[playerIds[i]] = {
				name: letters[i],
				readyStatus: false,
				rematched: Players.findOne(playerIds[i]).needRematch,
				contributions:[],
				firstMessages: [],
				secondMessages: [],
			};
			if(condition == '2G'){
				var partner = (i+(groupSize/2)) < groupSize ? (i+(groupSize/2)) : (i+(groupSize/2)-groupSize);
				var neighbors = [];
				for (var j = -1; j < 2; j+=2){
					var nIdx = (i+j) < playerIds.length ? (i+j) : (i+j-playerIds.length);
					neighbors.push(playerIds.slice(nIdx)[0]);
				}
				playersData[playerIds[i]].neighbors = neighbors;
				playersData[playerIds[i]].partner = playerIds[partner];
			}
		}
		var data = {
			_id: gameId,
			condition: condition,
			state: "assignment",
			round: 1,
			players: playersData
		};
		Partitioner.bindGroup(gameId,function(){
			Games.insert(data);
		});
	},
	//General purpose document modication function, can handle upto 2 simultaneous db operations
	'updateGameInfo': function(gameId,data,operation){
		if (data.constructor.toString().indexOf("Array") > -1){
			var query1 = data[0];
			var query2 = data[1];
		}
		Partitioner.directOperation(function(){
			if(operation == 'set'){
				Games.update(gameId, {$set: data});
			} else if(operation == 'inc'){
				Games.update(gameId, {$inc: data});
			} else if(operation == 'dec'){
				Games.update(gameId, {$dec: data});
			} else if(operation == 'push'){
				Games.update(gameId, {$push: data});
			} else if (operation == 'setinc'){
				Games.update(gameId, {$set: query1, $inc: query2});
			}
		});
	},
	//Tries to start a game if all players are ready
	'startGame': function(gameId, currentUser){
		var pKey = makePQuery(currentUser,'readyStatus',true);
		Meteor.call('updateGameInfo',gameId,pKey,'set');
		var game = Games.findOne(gameId);
		//some underscore magic to check if all players are ready
		if(_.every(_.pluck(game.players,'readyStatus'),_.identity)){
			Meteor.call('updateGameInfo',game._id,{'state':'pChoose'},'set');
		}
	},
	//Adds a single client's data to the Game document and updates the game state if all players have inserted that data into the document by comparing how many insertions have been made relative to the current game round; Expects an array called data with: [dbFieldName,dbFieldVal,nextState,arrayOfAutoAdvanceStates,delayForAutoAdvancing]
	'addPlayerRoundData': function(gameId, currentUser,data,delay){
		var game = Games.findOne();
		if(game.players[currentUser][data[0]].length < game.round){
			var pKey = makePQuery(currentUser,data[0],data[1]);
			Meteor.call('updateGameInfo',gameId,pKey,'push');
		}
		game = Games.findOne(gameId);
		if(_.every(_.pluck(game.players,data[0]),
			function(elem){return elem.length == game.round;})){
			if(delay > 0){
				Meteor.call('autoAdvanceState',game._id,data[2],data[3],delay);
			} else{
				Meteor.call('updateGameInfo',game._id,data[2],'set');
			}
			
		}
	},
	//Most of the time, clients trigger game state changes based on events (button clicks), but occassionally the server should trigger a state change based on a timer. This function handles those automatic state changes by making delayed method calls based on an array of future states. Will also increment the round counter if it encounters the last possible game state (gOut) in the delayedStates array, or end the game if the it's the last round.
	'autoAdvanceState': function(gameId,immediateState,delayedStates,delay){
		Meteor.call('updateGameInfo',gameId,{'state':immediateState},'set');
		var stateCounter = 0;
		var repeatCallID = Meteor.setInterval(function(){
			//If we're at the last possible game state, increment the round counter, switch to that state, see if we need to end the game 
			if(delayedStates[stateCounter] == 'gOut'){
				Meteor.call('updateGameInfo',gameId,[{'state':delayedStates[stateCounter]},{'round':1}],'setinc');
				var currentRound = Games.findOne(gameId).round;
				if(currentRound > numRounds){
					Meteor.setTimeout(function(){
						Meteor.call('endGame',gameId);						
					},delay);
					return Meteor.clearInterval(repeatCallID);
				}
			} else {
				Meteor.call('updateGameInfo',gameId,{'state':delayedStates[stateCounter]},'set');
			}
			stateCounter ++;
			if(stateCounter > delayedStates.length - 1){
				Meteor.clearInterval(repeatCallID);
			}
		},delay);
	},
	//Function updates all player status to finished and ends a game, changing its state and tearing down the experiment instance thereby sending all players back to the lobby, where they'll be shuttled to the exit survey
	'endGame':function(gameId){
		var asst;
		calcBonuses(gameId);
		Meteor.setTimeout(function(){
			Meteor.call('updateGameInfo',gameId,{'state':'ended'},'set');
			var exp = TurkServer.Instance.getInstance(gameId);
			if(exp != null){
				exp.teardown(returnToLobby = true);
				console.log("ASSIGNER: Game successfully ended: " + gameId);
			} else{
				console.log("ASSIGNER: Game could not be ended! No instance for: " + gameId);
			}

		});
	}
});

//Function to make a variable-based dot-notation query for updating a nested mongo document 
function makePQuery(currentUser,field,value){
	var pKey = {};
	pKey['players.' + currentUser + '.' + field] = value;
	return pKey;
}
//Function to randomly choose a round, calculate the bonus, store it in the players db and add it to the assignment
function calcBonuses(gameId){
	var game = Games.findOne(gameId);
	var roundNum = _.random(0,numRounds-1);
	var playerIds = _.keys(game.players);
	var pot = _.reduce(_.map(_.pluck(game.players,'contributions'),function(list) {return list[roundNum];}),function(a,b){return a+b;});
	var potSplit = Math.round((pot*1.5)/playerIds.length);
	var asst;
	var bonus;
	var contribution;
	_.each(playerIds,function(player){
		contribution = game[player].contributions[roundNum];
		bonus = (100 - contribution + potSplit) * bonusPaymentConversion/100;
		asst = TurkServer.Assignment.getCurrentUserAssignment(player);
		asst.addPayment(bonus);
		Meteor.call('updatePlayerInfo',player,{'status':'finished','bonus':bonus},'set');
	});
	return;
}
