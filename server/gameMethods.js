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
				messages: []
			};
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
	//General purpose document modication function
	'updateGameInfo': function(gameId,data,operation){
		Partitioner.directOperation(function(){
			if(operation == 'set'){
				Games.update(gameId, {$set: data});
			} else if(operation == 'inc'){
				Games.update(gameId, {$inc: data});
			} else if(operation == 'dec'){
				Games.update(gameId, {$dec: data});
			} else if(operation == 'push'){
				Games.update(gameId, {$push: data});
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
	//Adds a single client's data to the Game document and updates the game state if all players have inserted that data into the document by comparing how many insertions have been made relative to the current game round; Can also auto-advance another game state if a second state and delay are passed into the data array
	'addPlayerRoundData': function(gameId, currentUser,data){
		//var dataArray = _.flatten(_.pairs(data));
		var pKey = makePQuery(currentUser,data[0],data[1]);
		Meteor.call('updateGameInfo',gameId,pKey,'push');
		var game = Games.findOne(gameId);
		if(_.every(_.pluck(game.players,data[0]),
			function(elem){return elem.length == game.round;})){
			if(data.length > 3){
				Meteor.call('autoAdvanceState',game._id,data[2],data[3],data[4]);
			} else{
				Meteor.call('updateGameInfo',game._id,data[2],'set');
			}
			
		}
	},
	//Most of the time, clients trigger game state changes based on events (button clicks), but occassionally the server should trigger a state change based on a timer. This function handles that second automatic state change. 
	'autoAdvanceState': function(gameId,startState,endState,delay){
		Meteor.call('updateGameInfo',gameId,{'state':startState},'set');
		Meteor.setTimeout(function(){
			Meteor.call('updateGameInfo',gameId,{'state':endState},'set');
		},delay);
	}
});

//Function to make a variable-based dot-notation query for updating a nested mongo document 
function makePQuery(currentUser,field,value){
	var pKey = {};
	pKey['players.' + currentUser + '.' + field] = value;
	return pKey;
}