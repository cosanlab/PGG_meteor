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
	{	
		template: "assignment",
		onLoad: function(){
			$(".action-tutorial-finish").text("I'm Ready!");
		}
	},

];
//Global event emitter for quiz
var emitter = new EventEmitter();

//Setup interactive instructions logic
Template.instructionsInteractive.helpers({
	options: {
		steps: tutorialSteps,
		emitter: emitter,
		onFinish: function(){
			var gameId = Games.findOne()._id;
			var currentUser = Meteor.userId();
    		Meteor.call('playerReady',gameId, currentUser);
		}
	}
});

Template.quiz.helpers({
	feedback: function(){
		var currentUser = Meteor.userId();
		var player = Players.findOne(currentUser);
		var gameId = Games.findOne()._id;
		var text;
		var dispClass;
		if(player.passedQuiz){
			text = "Correct!";
			dispClass = 'correctQuiz';
		} else if(!player.passQuiz){
			if(player.quizAttempts == 0){
				text = "Placehodler";
				dispClass = 'noresponseQuiz';
			}
			if(player.quizAttempts > 0 && player.quizAttempts < 2){
				text = "Incorrect. One try remaining";
				dispClass = 'incorrectQuiz';
			} else if(player.quizAttempts == 2){
				Meteor.call('failedQuiz',currentUser,gameId);
			}
		}
		return{
			text: text,
			dispClass: dispClass
		};
	}
});
//Event handler for quiz during instructions
	Template.quiz.events({
	'click #correct':function(event){
		event.stopPropagation();
		var currentUser = Meteor.userId();
		Meteor.call('passedQuiz', currentUser);
		emitter.emit('correctAnswer');
	},
	'click #incorrect':function(event){
		event.stopPropagation();
		var currentUser = Meteor.userId();
		Meteor.call('incQuizAttempts',currentUser);
	},
});
//Assignment logic based on player role and condition
Template.assignment.helpers({
	game: function(){
		var game= Games.findOne();
		var currentUser = Meteor.userId();
		if(currentUser == game.playerA){
			return {
				player: 'Player A',
				condition: game.condition
			};
		} else if(currentUser == game.playerB){
			return {
				player: 'Player B',
				condition: game.condition
			};
		}
	}
});
