var beliefs = [
{
	template: 'beliefs',
	spot: 'body'
}];

Template.beliefsPopup.helpers({
	options: {
		steps: beliefs,
		onFinish: function(){
			var game = Games.findOne();
			var currentUser = Meteor.userId();
			var playerBelief = $("#beliefRating").val();
			console.log(playerBelief);
			$('.action-tutorial-finish').text(" ");
			Meteor.call('updatePlayerBelief', game._id,currentUser,playerBelief);

		}
	}
});

//Change belief submit button text
Template.beliefs.onRendered(function(){
	$(".action-tutorial-finish").text("Continue");
});

Template.beliefs.helpers({
	beliefs: function(){
		var game = Games.findOne();
		var playerId;
		var question;
		if (Meteor.userId() == game.playerA){
			playerId = 'Player A: ';
			question = 'If you choose RIGHT, how likely do you think Player B is to choose LEFT?';
		}
		else{
			playerId = 'Player B: ';
			question = 'How likely do you think Player A is to choose LEFT?';
		}
		return {
			playerId: playerId,
			question: question
		};

	},
    limit: function () {
      return "50";
    }
  });
