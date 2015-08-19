Template.payoffs.helpers({
	payoffs: function(){
		var game = Games.findOne();
		if(game.PlayerAChoice == 'Left'){
			return {
				playerA: '1',
				playerB: '3',
			};
		} else if(game.PlayerBChoice == 'Left'){
			return {
				playerA: '0',
				playerB: '1',
			};
		} else if(game.PlayerBChoice == 'Right'){
			return {
				playerA: '2',
				playerB: '2',
			};
		}
	}
});

Template.payoffs.onRendered(function(){
	var gameId = Games.findOne()._id;
	//Wait 5 seconds to end experiment
	Meteor.setTimeout(function(){
		Meteor.call('goToEndSurvey', gameId);

	},5000);
    
});