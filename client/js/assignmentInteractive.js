var roleAssignment = [
	{	
		template: "assignment"
	}

];

Template.assignmentInteractive.helpers({
	options: {
		steps: roleAssignment,
		onFinish: function(){
			var gameId = Games.findOne()._id;
			var currentUser = Meteor.userId();
			$(".action-tutorial-finish").text("	");
    		Meteor.call('playerReady',gameId, currentUser);
		}
	}
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

//Change role assignment ready button text
Template.assignment.onRendered(function(){
	$(".action-tutorial-finish").text("Ready!");
});

