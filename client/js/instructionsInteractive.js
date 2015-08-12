var tutorialSteps = [
	{
		template: "step1",
		onLoad: function() {
			console.log("tutorial has started");
		}
	},
	{	
		template: "step2",
		spot: "#tree"
	},
	{	
		template: "step3",
		spot: ".playerAleft, .playerAright"
	},
	{	
		template: "step4",
		spot: ".playerAleft"
	},
	{	
		template: "step5",
		spot: ".playerAright"
	}

];

Template.instructionsInteractive.helpers({
	options: {
		steps: tutorialSteps,
		onFinish: function(){
			var gameId = Games.findOne()._id;
    		Meteor.call('playerReady',gameId);
		}
	},

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

Template.instructionsInteractive.events({

  'click .ready-to-start': function(event){
    var gameId = Games.findOne()._id;
    Meteor.call('playerReady',gameId);
    $(".btn").text("I'm Ready!").removeClass('btn-danger').addClass('btn-success');

  }

});
