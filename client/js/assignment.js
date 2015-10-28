//Start tracking a user's connection status now
/*
Template.instructionsInteractive.onRendered(function(){
	UserStatus.startMonitor(
		{
			'threshold': 10000,
			'interval': 5000,
			'idleOnBlur': false
		});

}); */

var assignmentSteps = [
	{
		template: "matched"
	}
];

//Setup interactive instructions logic
Template.assignment.helpers({
	options: {
		steps: assignmentSteps,
		onFinish: function(){
			var currentUser = Meteor.userId();
			var gameId = Games.findOne()._id;
    		Meteor.call('startGame',gameId, currentUser);
		}
	}
});

Template.matched.helpers({
	numPlayers: function(){
		return groupSize;
	},
	numRounds: function(){
		return numRounds;
	}
});


