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
	otherPlayers: function(){
		return String(groupSize - 1);
	},
	numPlayers: function(){
		return String(groupSize);
	},
	numRounds: function(){
		return String(numRounds);
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
				fullInfo = false;
				break;
		}
		return {
			messaging: messaging,
			fullInfo: fullInfo
		};
	}
});
Template.rounds.inheritsHelpersFrom('overview');
Template.exampleRound.inheritsHelpersFrom('overview');
Template.recap.inheritsHelpersFrom('overview');
Template.timing.inheritsHelpersFrom('overview');

