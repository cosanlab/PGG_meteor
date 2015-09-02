Template.game.helpers({
	game: function(){
		var game = Games.findOne();
		var Adecide = "visibility:hidden";
		var Bdecide = "visibility:hidden";
		var messageForm = "visibility:hidden";
		var messageDisplay = "visibility:hidden";

		if(Meteor.userId() == game.playerA){
			Adecide = "";
		} else if(Meteor.userId() == game.playerB){
			if(game.condition == 'withMessaging'){
				messageForm = "";
			}
			Bdecide = "";
		}
		if(game.condition == 'withMessaging' && game.state=='playerAdeciding'){
			messageDisplay = "";
		}
		return {
			condition: game.condition,
			state: game.state,
			Adecide: Adecide,
			Bdecide: Bdecide,
			messageForm: messageForm,
			messageDisplay: messageDisplay
		};
	}
});

//Set the game tree colors dynamically based on player choices
Template.gameTree.helpers({
	branch: function(){
		var game = Games.findOne();
		var colorAL = "black"; var widthAL = "3"; 
		var colorAR = "black"; var widthAR = "3";
		var colorBL = "black"; var widthBL = "3";
	    var colorBR = "black"; var widthBR = "3";
		if(game.PlayerAChoice == 'Left'){
			 colorAL = "#33CCFF";
			 widthAL = "6";
		} else if(game.PlayerAChoice == 'Right'){
			colorAR = "#33CCFF";
			widthAR = "6";
		} 
		if(game.PlayerBChoice == 'Left'){
			 colorBL = "#33CCFF";
			 widthBL = "6";
		} else if(game.PlayerBChoice == 'Right'){
			colorBR = "#33CCFF";
			widthBR = "6";
		}
		return{
			colorAL: colorAL,
			widthAL: widthAL,
			colorAR: colorAR,
			widthAR: widthAR,
			colorBL: colorBL,
			widthBL: widthBL,
			colorBR: colorBR,
			widthBR: widthBR
		};
	}
});

//Event handlers for game 
Template.gameTree.events({
	//Meteor doesn't have a hover event handler so this is the way to go
	//and is what jquery does behind the scenes anyway
	'mouseenter .playerAleft':function(event){
		var game = Games.findOne();
		var currentUser = Meteor.userId();
		if(currentUser == game.playerA && game.state=='playerAdeciding'){
			$(".playerAleft").attr('class','playerAleft hover');		
		}
	},
	'mouseleave .playerAleft':function(event){
		var game = Games.findOne();
		var currentUser = Meteor.userId();
		if(currentUser == game.playerA && game.state=='playerAdeciding'){
			$(".playerAleft").attr('class','playerAleft');
		}
	},
	'mouseenter .playerAright':function(event){
		var game = Games.findOne();
		var currentUser = Meteor.userId();
		if(currentUser == game.playerA && game.state=='playerAdeciding'){
			$(".playerAright").attr('class','playerAright hover');		
		}
	},
	'mouseleave .playerAright':function(event){
		var game = Games.findOne();
		var currentUser = Meteor.userId();
		if(currentUser == game.playerA && game.state=='playerAdeciding'){
			$(".playerAright").attr('class','playerAright');
		}
	},
	'mouseenter .playerBleft':function(event){
		var game = Games.findOne();
		var currentUser = Meteor.userId();
		if(currentUser == game.playerB && game.state=='playerBdeciding'){
			$(".playerBleft").attr('class','playerBleft hover');		
		}
	},
	'mouseleave .playerBleft':function(event){
		var game = Games.findOne();
		var currentUser = Meteor.userId();
		if(currentUser == game.playerB && game.state=='playerBdeciding'){
			$(".playerBleft").attr('class','playerBleft');
		}
	},
	'mouseenter .playerBright':function(event){
		var game = Games.findOne();
		var currentUser = Meteor.userId();
		if(currentUser == game.playerB && game.state=='playerBdeciding'){
			$(".playerBright").attr('class','playerBright hover');		
		}
	},
	'mouseleave .playerBright':function(event){
		var game = Games.findOne();
		var currentUser = Meteor.userId();
		if(currentUser == game.playerB && game.state=='playerBdeciding'){
			$(".playerBright").attr('class','playerBright');
		}
	},	
	'click .playerAleft': function(event){
		event.preventDefault();
		var game = Games.findOne();
		var currentUser = Meteor.userId();
		if(currentUser == game.playerA && game.state=='playerAdeciding'){
			$(".playerAleft").attr('class','playerAleft');
			Meteor.call('updatePlayerChoice', game._id,'A','Left');
			Meteor.call('updateGameState',game._id, 'finalChoices');
		}	
	},
	'click .playerAright': function(event){
		event.preventDefault();
		var game= Games.findOne();
		var currentUser = Meteor.userId();
		if(currentUser == game.playerA && game.state=='playerAdeciding'){
			$(".playerAright").attr('class','playerAright');
			Meteor.call('updatePlayerChoice',game._id,'A','Right');
			Meteor.call('updateGameState',game._id, 'playerBdeciding');
		}
	},	
	'click .playerBleft': function(event){
		event.preventDefault();
		var game= Games.findOne();
		var currentUser = Meteor.userId();
		if(currentUser == game.playerB && game.state=='playerBdeciding'){
			$(".playerBleft").attr('class','playerBleft');
			Meteor.call('updatePlayerChoice', game._id,'B','Left');
			Meteor.call('updateGameState',game._id, 'finalChoices');
		}
	},
	'click .playerBright': function(event){
		event.preventDefault();
		var game= Games.findOne();
		var currentUser = Meteor.userId();
		if(currentUser == game.playerB && game.state=='playerBdeciding'){
			$(".playerBright").attr('class','playerBright');
			Meteor.call('updatePlayerChoice', game._id,'B','Right');
			Meteor.call('updateGameState',game._id, 'finalChoices');
		}
	}	
});

//Give messageForm access to game template helpers 
Template.messageForm.inheritsHelpersFrom('game');
//Mesage input event handler
Template.messageForm.events({
		'click .bubble' : function(event){
			event.preventDefault();
			if($('.message-form').text() == 'Type a message here and press Enter to send...'){
				$('.message-form').text('');
			}
		},
		'keydown p.message-form': function(event){
		if(event.which===13){
        	var message = $('.message-form').text();
        	var gameId = Games.findOne()._id;
        	Meteor.call('addMessage',gameId, message);
        	Meteor.call('updateGameState',gameId, 'playerAdeciding');
        	event.target.value = "";
    	}
	}
});

//Give messageDisplay access to game template helpers 
Template.messageDisplay.inheritsHelpersFrom('game');
//Message display template helper
Template.messageDisplay.helpers({
	message: function(){
		var game = Games.findOne();
		var whoSaid;
		var currentUser = Meteor.userId();
		if(currentUser==game.playerA){
			whoSaid = 'Player B says:';
		}
		else if(currentUser == game.playerB){
			whoSaid = 'You said:';
		}
		return {
			content: game.message,
			whoSaid: whoSaid
		};
	}
});

