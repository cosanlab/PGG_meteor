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
	var currentUser = Meteor.userId();
	//Wait 5 seconds to send a user back to the lobby
	Meteor.setTimeout(function(){
		Meteor.call('goToLobby', currentUser);

	},5000);
    
});