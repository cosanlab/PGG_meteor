Template.game.helpers({
	game: function(){
		var game = Games.findOne();
		var isPlayerA = false; 
		var isPlayerB = false;
		if(Meteor.userId() == game.playerA){
			isPlayerA = true;
		} else if(Meteor.userId() == game.playerB){
			isPlayerB = true;
		}
		var state = game.state;
		var condition = game.condition;
		if(condition == 'noMessaging'){
			Meteor.call('addMessage',game._id, '');
		}
		return {
			condition: condition,
			isPlayerA: isPlayerA,
			isPlayerB: isPlayerB,
			state: state
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
		var RT = (Date.now()-sTime)/1000;
		var game = Games.findOne();
		var currentUser = Meteor.userId();
		if(currentUser == game.playerA && game.state=='playerAdeciding'){
			$(".playerAleft").attr('class','playerAleft');
			Meteor.call('updatePlayerChoice', game._id,'A','Left',RT);
			Meteor.call('updateGameState',game._id, 'finalChoices');
		}	
		sTime = Date.now();
	},
	'click .playerAright': function(event){
		event.preventDefault();
		var RT = (Date.now()-sTime)/1000;
		var game= Games.findOne();
		var currentUser = Meteor.userId();
		if(currentUser == game.playerA && game.state=='playerAdeciding'){
			$(".playerAright").attr('class','playerAright');
			Meteor.call('updatePlayerChoice',game._id,'A','Right',RT);
			Meteor.call('updateGameState',game._id, 'playerBdeciding');
		}
		sTime = Date.now();	
	},	
	'click .playerBleft': function(event){
		event.preventDefault();
		var RT = (Date.now()-sTime)/1000;
		var game= Games.findOne();
		var currentUser = Meteor.userId();
		if(currentUser == game.playerB && game.state=='playerBdeciding'){
			$(".playerBleft").attr('class','playerBleft');
			Meteor.call('updatePlayerChoice', game._id,'B','Left',RT);
			Meteor.call('updateGameState',game._id, 'finalChoices');
		}
		sTime = Date.now();
	},
	'click .playerBright': function(event){
		event.preventDefault();
		var RT = (Date.now()-sTime)/1000;
		var game= Games.findOne();
		var currentUser = Meteor.userId();
		if(currentUser == game.playerB && game.state=='playerBdeciding'){
			$(".playerBright").attr('class','playerBright');
			Meteor.call('updatePlayerChoice', game._id,'B','Right',RT);
			Meteor.call('updateGameState',game._id, 'finalChoices');
		}
		sTime = Date.now();
	}	
});

//Mesage input event handler
Template.messageForm.events({
		'keydown input.form-control': function(event){
		if(event.which===13){
			var RT = (Date.now()-sTime)/1000;
        	var message = event.target.value;
        	var gameId = Games.findOne()._id;
        	Meteor.call('addMessage',gameId, message, RT);
        	Meteor.call('updateGameState',gameId, 'playerAdeciding');
        	event.target.value = "";
    	}
    	sTime = Date.now();	

	}
});

//Message display template helper
Template.messageDisplay.helpers({
	message: function(){
		return Games.findOne().message;
	}
});

//Initialize timing info
Template.game.onRendered(function(){
	sTime = Date.now();
});

