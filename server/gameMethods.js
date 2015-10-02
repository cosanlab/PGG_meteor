Meteor.methods({
	//Adds a new game document to the database
	'createGame': function(gameId, playerIds, condition){
		var playersData = {
			'A':{},'B':{},'C':{},'D':{},'E':{}
		};
		var idx = 0;
		for (var player in playersData){
			if (playersData.hasOwnProperty(player)){
				player = {
					name: playerIds[idx],
					readyStatus: false,
					rematched: Players.findOne(playerIds[idx]).needRematch,
					instrCmp: false,
					contributions:{},
					messages: {}
				};
				idx += 1;
			}
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