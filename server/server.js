//Subjects DB
Meteor.publish('Players', function(){
	return Players.find();
});

//Games DB
Meteor.publish('Games', function(){
	return Games.find();
});

Meteor.methods({
	'addPlayer': function(playerId){
		var currentUser = Meteor.user();
		var data = {
			name: currentUser.username,
			enterTime: new Date(),
			status: "waiting"
		};
		if(!currentUser){
			throw new Meteor.Error("not-logged-in", "You're not logged in.");
		}
		return Players.insert(data);
	}	
});
