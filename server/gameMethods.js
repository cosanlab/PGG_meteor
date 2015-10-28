Meteor.methods({
	//Adds a new game document to the database
	'createGame': function(gameId, playerIds, condition){
		var playersData = {};	
		for(var i=0; i<playerIds.length; i++) {
			playersData[playerIds[i]] = {
				name: letters[i],
				readyStatus: false,
				rematched: Players.findOne(playerIds[idx]).needRematch,
				instrCmp: false,
				contributions:{},
				messages: {}
			};
		}
		var data = {
			_id: gameId,
			condition: condition,
			state: "assignment",
			round: 1,
			players: playersData
		};
		Partitioner.directOperation(function(){
			Games.insert(data);
		});
	},
	//General purpose document modication function
	'updateGameInfo': function(gameId,data,operation){
		Partitioner.directOperation(function(){
			if(operation == 'set'){
				Games.update(currentUser, {$set: data});
			} else if(operation == 'inc'){
				Games.update(currentUser, {$inc: data});
			} else if(operation == 'dec'){
				Games.update(currentUser, {$dec: data});
			}
		});
	},
	'startGame': function(gameId, currentUser){
		var game = Games.findOne(gameId);
		var pKey = makePQuery(currentUser,'readyStatus',true);
		Meteor.call('updateGameInfo',game._id,pKey,'set');
		game = Games.findOne(gameId);
		//some underscore magic to check if all players are ready
		if(_.every(_.pluck(game.players,'readyStatus'),_.identity)){
			Meteor.call('updateGameInfo',game._id,{'state':'pChoose'},'set');
		}
	}
});

//Function to make a variable-based dot-notation query for updating a nested mongo document 
function makePQuery(currentUser,field,value){
	var pKey = {};
	pkey['players.' + currentUser + '.' + field] = value;
	return pKey;
}