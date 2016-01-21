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
			$(".action-tutorial-finish").text("	");
    		Meteor.call('startGame',gameId, currentUser);
		}
	}
});

Template.matched.helpers({
	otherPlayers: function(){
		return groupSize-1;
	},
	numRounds: function(){
		return numRounds;
	},
	batchCond: function(){
		var batchCond = Batches.findOne().treatments[0];
		var messaging;
		var fullInfo;
		switch (batchCond) {
			case '2G':
				messaging = true;
				fullInfo = false;
				break;
			case '2NG':
				messaging = false;
				fullInfo = false;
				break;
			case '6G':
				messaging = true;
				fullInfo = true;
				break;
			case '6NG':
				messaging = false;
				fullInfo = true;
				break;
		}
		return {
			messaging: messaging,
			fullInfo: fullInfo
		};
	}
});

//Change role assignment ready button text
Template.assignment.onRendered(function(){
	$(".action-tutorial-finish").text("Ready!");
	$.playSound('/bell');
});
