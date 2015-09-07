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
//Quiz global vars
var emitter = new EventEmitter();

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

//Only give clients 2 tries
var tries = 2;
Template.quiz.events({
	'click #correct':function(event){
		$('.quiz-answer').css({'visibility':'visible', 'color':'#00CC00', 'font-weight':'bold'}).text('Correct!');
		
		emitter.emit('correctAnswer');
	},
	'click #incorrect':function(event){
		tries --;
		if(tries > 0){
			$('.quiz-answer').css({'visibility':'visible', 'color':'#FF0000', 'font-weight': 'bold'}).text('Incorrect. One try remaining.');		
			emitter.emit('incorrectAnswer');
		} else{
			Meteor.call('notEligible', Meteor.userId());
		}
	},


});

