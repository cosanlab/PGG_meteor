//Subjects DB
Meteor.publish('Players', function(){
	return Players.find();
});

//Games DB
Meteor.publish('Games', function(){
	return Games.find();
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
			status: "waiting"
		};
		if(!currentUser){
			throw new Meteor.Error("not-logged-in", "You're not logged in.");
		}
		return Players.insert(data);
	},
	'removePlayer': function(playerId){
		var currentUser = Meteor.userId();
		var data = {
			_id: currentUser,
		};
		if(!currentUser){
			throw new Meteor.Error("not-logged-in", "You're not logged in.");
		}
		return Players.remove(data);
	}	
});
