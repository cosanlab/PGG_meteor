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
				playerB: '0',
			};
		} else if(game.PlayerBChoice == 'Right'){
			return {
				playerA: '2',
				playerB: '2',
			};
		}
	}
});

//Allow client to submit HIT to mturk
Template.payoffs.events({
	'click button': function(){
		Meteor.call('goToEndSurvey');
	}
});