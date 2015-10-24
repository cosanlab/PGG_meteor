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

var tutorialSteps = [
	{
		template: "accept"
	},
	{	
		template: "overview"
	},
	{	
		template: "rounds"
	},
	{
		template: "exampleRound"
	},
	{
		template: "recap"
	},
	{	
		template: "timing"
	},
	{	
		template: "preQuiz"
	},
	{
		template: "quiz", 
		require: {
			event: 'submittedQuiz'
		}
	}
];

quizEmitter = new EventEmitter();
//Setup interactive instructions logic
Template.instructions.helpers({
	options: {
		steps: tutorialSteps,
		emitter: quizEmitter,
		onFinish: function(){
			var currentUser = Meteor.userId();
			var passedQuiz = Players.findOne(currentUser).passedQuiz;
    		Meteor.call('checkPlayerEligibility',currentUser, passedQuiz);
		}
	}
});

//Tutorial steps helpers
Template.overview.helpers({
	numPlayers: function(){
		return groupSize;
	},
	numRounds: function(){
		return numRounds;
	},
	game: function(){
		var game = Games.findOne();
		return{
			condition: game.condition,
		};
	}
});

Template.recap.helpers({
	numPlayers: function(){
		return groupSize;
	},
	numRounds: function(){
		return numRounds;
	}
});


Template.timing.helpers({
	otherPlayers: function(){
		return groupSize-1;
	}
});

