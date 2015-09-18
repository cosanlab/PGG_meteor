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

Template.beliefs.events({
	'click #beliefSubmit': function(event){
		event.preventDefault();
		var game = Games.findOne();
		var currentUser = Meteor.userId();
		var playerBelief = $("#beliefRating").val();
		console.log(playerBelief);
		$('#beliefSubmit').removeClass('btn-primary').addClass('btn-warning').text('Wating for other player...').prop('disabled',true);
		Meteor.call('updatePlayerBelief', game._id,currentUser,playerBelief);
	}
});