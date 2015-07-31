//Subjects DB
Meteor.publish('Players', function(){
	return Players.find({});
});

//Games DB
Meteor.publish('Games', function(){
    var currentUser = this.userId;	
	return Games.find({playerA: currentUser,playerB: currentUser});
});

Meteor.methods({
	'addPlayer': function(){
		var currentUser = Meteor.userId();
		var data = {
			_id: currentUser,
			name: currentUser,
			enterTime: new Date(),
			status: 'waiting'
		};
		if(!currentUser){
			throw new Meteor.Error("not-logged-in", "You're not logged in.");
		}
		Players.insert(data);
	},
	'removePlayer': function(){
		var currentUser = Meteor.userId();
		var data = {
			_id: currentUser,
		};
		if(!currentUser){
			throw new Meteor.Error("not-logged-in", "You're not logged in.");
		}
		return Players.remove(data);
	},
	'updatePlayer': function(playerId, state){
		check(state,String);
		return Players.update(playerId, {$set: {status: state}});
	},
	'createGame': function(clientId, partnerId){
		// Security
		var currentUser = Meteor.userId();
		if(!currentUser){
			throw new Meteor.Error("not-logged-in", "You're not logged in.");
		}

		// Randomize role
		var isPlayerA = Math.random() > 0.5;
		var data;
		if(isPlayerA){
			data = {
				playerA: clientId,
				playerB: partnerId,
				state: "instructions"
			};
		} else {
			data = {
				playerA: partnerId,
				playerB: clientId,
				state: "instructions"
			};
		}

		// update each player's status
		Meteor.call('updatePlayer', clientId, 'instructions');
		Meteor.call('updatePlayer', partnerId, 'instructions');

		// Add Game to DB
		return Games.insert(data);
	},
	'matchPlayers': function(){
		var clientId = Meteor.userId();
		var numPlayers = Players.find({status:'waiting'},{}).count();
		// The way to extract just the id from the mongo query
		var partnerId = Players.find({}, {fields: {'_id':1}, sort:{enterTime:1},limit:1}).fetch()[0]._id;
		if (numPlayers >= 2){
			// Create a new game with each player
			return Meteor.call('createGame', clientId, partnerId);
		}
	},
	'updateGame': function(gameId, state){
		check(state,String);
		var currentUser = Meteor.userId();
		if(!currentUser){
			throw new Meteor.Error("not-logged-in", "You're not logged in.");
		}
		return Games.update(gameId, {$set: {status: state}});
	}
});
