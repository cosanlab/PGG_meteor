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
		template: "step1",
		onLoad: function() {
			$(".playerAleft, #playerAleft, .playerAright, #playerAright").attr("stroke","black").attr("fill","black");
			$(".playerBleft, #playerBleft, .playerBright, #playerBright").attr("stroke","black").attr("fill","black");
		}
	},
	{	
		template: "step2",
		spot: "#tree",
		onLoad: function() {
		$(".playerAleft, #playerAleft, .playerAright, #playerAright").attr("stroke","black").attr("fill","black");
		$(".playerBleft, #playerBleft, .playerBright, #playerBright").attr("stroke","black").attr("fill","black");
		}
	},
	{	
		template: "step3",
		spot: "#tree",
		onLoad: function(){
			$(".playerAleft, #playerAleft, .playerAright, #playerAright").attr("stroke","red").attr("fill","red");
		}
	},
	{	
		template: "step4",
		spot: ".playerAleft",
		onLoad: function(){
			$(".playerAleft, #playerAleft, .playerAright, #playerAright").attr("stroke","black").attr("fill","black");
		}
	},
	{	
		template: "step5",
		spot: ".playerAright",
		onLoad: function(){
			$(".playerBleft, #playerBleft, .playerBright, #playerBright").attr("stroke","black").attr("fill","black");
}
	},
	{
		template: "step6",
		spot: "#tree",
		onLoad: function(){
			$(".playerBleft, #playerBleft, .playerBright, #playerBright").attr("stroke","red").attr("fill","red");
		}
	},
	{	
		template: "step7",
		spot: ".playerBleft",
		onLoad: function(){
			$(".playerBleft, #playerBleft, .playerBright, #playerBright").attr("stroke","black").attr("fill","black");
		}
	},
	{	
		template: "step8",
		spot: ".playerBright",
	},
	{
		template: "step9"
	},
	{
		template: "quiz",
		spot:"body",
		require: {
			event: 'correctAnswer'
		}
	},

];
//Tutorial step helpers
Template.step2.helpers({
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

//Global event emitter for quiz
var emitter = new EventEmitter();

//Setup interactive instructions logic
Template.instructions.helpers({
	options: {
		steps: tutorialSteps,
		emitter: emitter,
		onFinish: function(){
			var gameId = Games.findOne()._id;
			var currentUser = Meteor.userId();
    		Meteor.call('playerInstrComplete',gameId, currentUser);
		}
	}
});

Template.quiz.helpers({
	feedback: function(){
		var currentUser = Meteor.userId();
		var player = Players.findOne(currentUser);
		var text;
		var dispClass;
		var promptDisp = 'visibility:hidden';
		if(player.passedQuiz){
			text = "Correct!";
			dispClass = 'correctQuiz';
			promptDisp = "";
		} else if(!player.passQuiz){
			if(player.quizAttempts == 0){
				text = "Placehodler";
				dispClass = 'noresponseQuiz';
			}
			else if(player.quizAttempts > 0 && player.quizAttempts < 2){
				text = "Incorrect. One try remaining";
				dispClass = 'incorrectQuiz';
			} 
		}
		return{
			text: text,
			dispClass: dispClass,
			promptDisp: promptDisp
		};
	}
});
//Event handler for quiz during instructions
	Template.quiz.events({
	'change #B':function(event){
		event.stopPropagation();
		event.preventDefault();
		var currentUser = Meteor.userId();
		Meteor.call('passedQuiz', currentUser);
		emitter.emit('correctAnswer');
	},
	'change #A, change #C':function(event){
		event.stopPropagation();
		event.preventDefault();
		var currentUser = Meteor.userId();
		var gameId = Games.findOne()._id;
		var userInst = Meteor.users.findOne(currentUser).group; 
		Meteor.call('incQuizAttempts',currentUser, gameId, userInst);
	}
});

