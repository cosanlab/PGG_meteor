/*Template.instructions.helpers({
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

Template.instructions.events({

  'click .ready-to-start': function(event){
    var gameId = Games.findOne()._id;
    Meteor.call('playerReady',gameId);
    $(".btn").text("I'm Ready!").removeClass('btn-danger').addClass('btn-success');

  }

}); */
