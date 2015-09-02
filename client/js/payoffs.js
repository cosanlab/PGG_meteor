Template.payoffs.helpers({
	payoffs: function(){
		var game = Games.findOne();
		if(game.PlayerAChoice == 'Left'){
			return {
				playerA: '0.10',
				playerB: '0.30',
			};
		} else if(game.PlayerBChoice == 'Left'){
			return {
				playerA: '0.00',
				playerB: '0.10',
			};
		} else if(game.PlayerBChoice == 'Right'){
			return {
				playerA: '0.20',
				playerB: '0.20',
			};
		}
	}
});

//Not sure if this is the best way to do this
Template.payoffs.onRendered(function(){
	var currentUser = Meteor.userId();
	var gameId = Games.findOne()._id;
	Meteor.call('calcBonuses', gameId, currentUser);
	//Wait 5 seconds to send a user back to the lobby
	Meteor.setTimeout(function(){
		Meteor.call('goToLobby', currentUser);
		//Meteor.call('sendToExitSurvey', currentUser);

	},5000);
    
});