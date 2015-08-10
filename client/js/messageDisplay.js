Template.messageDisplay.helpers({
	//Only show message input to player B
	isPlayerB: function(){
		var playerB = Games.findOne().playerB;
		if(Meteor.userId() == playerB){
			return true;
		} else{
			return false;
		}
	},	
	isPlayerA: function(){
		var playerA = Games.findOne().playerA;
		if(Meteor.userId() == playerA){
			return true;
		} else{
			return false;
		}
	},
	gameState: function(){
		return Games.findOne().state;
	},
	condition: function(){
		var game = Games.findOne();
		if(game.condition == 'noMessaging'){
			Meteor.call('addMessage',game._id, '');
		}
		return game.condition;
	}
});