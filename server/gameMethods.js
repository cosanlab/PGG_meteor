Meteor.methods({
	//Adds a new game document to the database
	'createGame': function(gameId, playerIds, condition){
		var playersData = {};	
		for(var i=0; i<playerIds.length; i++) {
			playersData[letters[i]] = {
				name: playerIds[i],
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
			state: "initialized",
			round: 1,
			players: playersData
		};
		Partitioner.directOperation(function(){
			Games.insert(data);
		});
		//Meteor.call('resetPlayerRematch', clientId);
		//Meteor.call('resetPlayerRematch', partnerId);
	},
});