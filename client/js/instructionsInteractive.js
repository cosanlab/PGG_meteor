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
		template: "step9",
		onLoad: function(){
			$(".action-tutorial-finish").text("I'm Ready!");
		}
	},

];

Template.instructionsInteractive.helpers({
	options: {
		steps: tutorialSteps,
		onFinish: function(){
			var gameId = Games.findOne()._id;
    		Meteor.call('playerReady',gameId);
		}
	}
});

Template.step9.helpers({

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

