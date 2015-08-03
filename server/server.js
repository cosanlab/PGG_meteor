//Subjects DB
Meteor.publish('Players', function(){
	return Players.find({});
});

//Games DB
Meteor.publish('Games', function(){
	return Games.find({});
});

/*
//Players Waiting - might be security issue to return all user info
Meteor.publish('usersGroup', function(group) {
  check(group, String);
  var group = Players.findOne(status);
  var selector = {_id: {$in: group.members}};
  var options = {fields: {name: 1, enterTime: 1}};
  return Meteor.Players.find(selector, options);
});
*/

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
		return Players.insert(data);	
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
		return Players.update(playerId, {$set: {status: state}});
	},
	'createGame': function(clientId, partnerId){
		// Randomize role
		var isPlayerA = Math.random() > 0.5;
		var data;
		if(isPlayerA){
			data = {
				playerA: clientId,
				playerB: partnerId
			};
		} else {
			data = {
				playerA: partnerId,
				playerB: clientId
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
	}
});
