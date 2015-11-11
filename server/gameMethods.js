Meteor.methods({
	//Adds a new game document to the database
	'createGame': function(gameId, playerIds, condition){
		var playersData = {};
		var icons = _.shuffle(avatars);
		for(var i=0, plen = playerIds.length; i<plen; i++) {
			playersData[playerIds[i]] = {
				name: letters[i],
				icon: icons[i],
				readyStatus: false,
				rematched: Players.findOne(playerIds[i]).needRematch,
				contributions:[],
				firstMessages: [],
				secondMessages: [],
				playerRatings: {}
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
		var game = Games.findOne(gameId);
		var pKey;
		if(game.players[currentUser][data[0]].length < game.round){
			pKey = makePQuery(currentUser,data[0],data[1]);
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
	//Same as add player round data, but instead adds non-round based data, i.e. expects a single insertion to a single field across the entire game for each client
	'addPlayerStaticData':function(gameId, currentUser,data,delay){
		var game = Games.findOne(gameId);
		if(_.isEmpty(game.players[currentUser][data[0]])){
			pKey = makePQuery(currentUser,data[0],data[1]);
			Meteor.call('updateGameInfo',gameId,pKey,'set');
		}
		game = Games.findOne(gameId);
		if(_.every(_.pluck(game.players,data[0]),
			function(elem){return !_.isEmpty(elem);})){
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
			//If we encounter the ended game auto state, end the game, otherwise if we encounter the round outcome autostate, updated the round counter and switch to that state, otherwise switch the autostate 
			if (Games.findOne(gameId).state == 'lostUser'){
				return Meteor.clearInterval(repeatCallID);
			} else if(delayedStates[stateCounter] == 'ended'){
				Meteor.call('endGame',gameId,false);
				return Meteor.clearInterval(repeatCallID);
			} else if(delayedStates[stateCounter] == 'gOut'){
				Meteor.call('updateGameInfo',gameId,[{'state':delayedStates[stateCounter]},{'round':1}],'setinc');
			} else {
				Meteor.call('updateGameInfo',gameId,{'state':delayedStates[stateCounter]},'set');
			}
			stateCounter ++;
			if(stateCounter > delayedStates.length - 1){
				Meteor.clearInterval(repeatCallID);
			}
		},delay);
	},
	//Function updates all player status according to how a game ended. Also changes the game state appropriately and calculates bonuses. Tears down experiment and initiates exit survey after. Take an optional whoEnded argument that should be provided if premature is true
	'endGame':function(gameId, premature){
		var exp = TurkServer.Instance.getInstance(gameId);
		var game = Games.findOne(gameId);
		var state;
		var log;
		if(!premature){
			_.each(_.keys(game.players), function(player){
				Meteor.call('updatePlayerInfo',player,{'status':'finished'},'set');
			});
			state = 'ended';
			log = "ASSIGNER: Game " + gameId + " successfully ended!";
		} else {
			state = 'connectionError';
			log = "ASSIGNER: Game " + gameId + " went boom!";
			//Clear any other bomb timers from others who might have disconnected. Change the player status differently depending on if player caused the game end (or left while waiting for reconnection), or if they were affected by it
			var batchId = Experiments.findOne(exp.groupId).batchId;
			var batch = TurkServer.Batch.getBatch(batchId);
			var leftPlayers = batch.assigner.disconnectTimers[gameId].players;
			var stayedPlayers = _.difference(_.keys(game.players),leftPlayers);
			_.each(leftPlayers,
				function(player){
					Meteor.call('updatePlayerInfo',player,{'status':'leftGame'},'set');
				});
			_.each(stayedPlayers,
				function(player){
					Meteor.call('updatePlayerInfo',player,{'status':'connectionLost'},'set');
				});
			//Remove the game from the assigner's timer list
			batch.assigner.disconnectTimers = _.omit(batch.assigner.disconnectTimers,gameId); 
		}
		calcBonuses(game);
		Meteor.call('updateGameInfo',gameId,{'state':state},'set');
		
		if(exp != null){
			exp.teardown(returnToLobby = true);
			console.log(log);
		} else{
			console.log("ASSIGNER: Game could not be ended! No instance for: " + gameId);
		}

	},
	//Gets called reactively when a user in a game instance (i.e. has group id) disconnects from Meteor. Starts a game time bomb much like the lobby time bomb. Also stores previous game state
	'connectionChange': function(currentUser, gameId, connection){
		var asst = TurkServer.Assignment.getCurrentUserAssignment(currentUser);
		var batch = TurkServer.Batch.getBatch(asst.batchId);
		var game = Games.findOne(gameId);
		//Make a bomb
		if(connection == 'disconnect'){
			var prevState = game.state;
			if (prevState != 'lostUser'){
				Meteor.call('updateGameInfo',gameId, {'state':'lostUser'},'set');
			}
			if(!_.has(batch.assigner.disconnectTimers, gameId)){
				var disconnectBomb = Meteor.setTimeout(function(){
					Meteor.call('endGame',gameId, true);
				},disconnectTimeout*60*1000);
				batch.assigner.disconnectTimers[gameId] = {disconnectBomb: disconnectBomb, state: prevState, players:[]};
				batch.assigner.disconnectTimers[gameId].players.push(currentUser);
				console.log("ASSIGNER: User " + asst.workerId + 'disconnected! Game ' + gameId + ' state saved! Also set up the bomb!');
			} else if(_.has(batch.assigner.disconnectTimers, gameId)){
				batch.assigner.disconnectTimers[gameId].players.push(currentUser);
				console.log("ASSIGNER: User " + asst.workerId + 'disconnected! Game ' + gameId + ' already has a bomb!');
			}		
			
		} else if(connection == 'reconnect'){
			//Remove the reconnected player
			batch.assigner.disconnectTimers[gameId].players = _.xor(batch.assigner.disconnectTimers[gameId].players,[currentUser]);
			//Defuse bomb and resume game if all players have reconnected
			if(batch.assigner.disconnectTimers[gameId].players.length == 0){
				Meteor.clearTimeout(batch.assigner.disconnectTimers[gameId].disconnectBomb);
				var revertState = batch.assigner.disconnectTimers[gameId].state;
				switch(revertState){
					case 'pDisp':
						Meteor.call('autoAdvanceState',gameId,revertState,['pSendMess1'],5000);
					break;
					case 'pReceiveMess1':
						Meteor.call('autoAdvanceState',gameId,revertState,['pSendMess2'],5000);
					break;
					case 'pReceiveMess2':
						if(game.round < numRounds){
							Meteor.call('autoAdvanceState',gameId,revertState,['gOut','pChoose'],5000);
						} else {

						} Meteor.call('autoAdvanceState',gameId,revertState,['gOut','playerRatings'],5000);
					break;
					default:
						Meteor.call('updateGameInfo',gameId,{'state':revertState},'set');
				}				
				batch.assigner.disconnectTimers = _.omit(batch.assigner.disconnectTimers,gameId);
				console.log("ASSIGNER: User " + asst.workerId + 'reconnected! Game ' + gameId + 'bomb defused!');
			} else {
				console.log("ASSIGNER: User " + asst.workerId + 'reconnected! Game ' + gameId + 'bomb NOT YET defused!');
			}
		}
	}
});

//Function to make a variable-based dot-notation query for updating a nested mongo document 
function makePQuery(currentUser,field,value){
	var pKey = {};
	pKey['players.' + currentUser + '.' + field] = value;
	return pKey;
}
//Function to randomly choose a round, calculate the bonus, store it in the players db and add it to the assignment; uses fixed bonus if game ended prematurely
function calcBonuses(game){
	var playerIds = _.keys(game.players);
	var pot;
	var bonus;
	var roundNum;
    var tooEarly = _.any(_.pluck(game.players,'contributions',_.isEmpty));

	//If game ended too early, give players who didn't leave a fixed bonus
	if (game.round == 1 && tooEarly){
		bonus = disconnectPay;
		_.each(playerIds, function(player){
			if(Players.findOne(player).status != 'leftGame'){
				asst = TurkServer.Assignment.getCurrentUserAssignment(player);
				asst.addPayment(bonus);
				Meteor.call('updatePlayerInfo',player,{'bonus':bonus},'set');
			}
		});
		return;
	} else {
		//If game ended normally give everyone a random round bonus
		if(game.round > numRounds){
			roundNum = _.random(0,game.round-2);
		//If game ended early, but not too early, give everyone who didn't leave a random round bonus
		} else if(game.round >= 1 || game.round <= numRounds){
			roundNum = _.random(0,game.round-1);
		} 
		pot = _.reduce(_.map(_.pluck(game.players,'contributions'),function(list) {return list[roundNum];}),function(a,b){return a+b;});
		var potSplit = Math.round((pot*1.5)/playerIds.length);
		var asst;
		var contribution;
		_.each(playerIds,function(player){
			contribution = game.players[player].contributions[roundNum];
			bonus = (100 - contribution + potSplit) * bonusPaymentConversion/100;
			if(Players.findOne(player).status != 'leftGame'){
				asst = TurkServer.Assignment.getCurrentUserAssignment(player);
				asst.addPayment(bonus);
				Meteor.call('updatePlayerInfo',player,{'bonus':bonus},'set');
			}
		});
		return;
	}
}