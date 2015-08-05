Template.game.helpers({
	//Only show message input to player B
	isPlayerB: function(){
		var playerB = Games.findOne().playerB;
		if(Meteor.userId() == playerB){
			return true;
		} else{
			return false;
		}
	},	
	gameState: function(){
		return Games.findOne().state;
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
			$(".playerAleft").attr('class','playerAleft choice');
			Meteor.call('updatePlayerChoice', game._id,'Left');
			Meteor.call('updateGameState',game._id, 'payoffs');
			Router.go('payoffs');
		}	
	},
	'click .playerAright': function(event){
		event.preventDefault();
		var game= Games.findOne();
		var currentUser = Meteor.userId();
		if(currentUser == game.playerA && game.state=='playerAdeciding'){
			$(".playerAright").attr('class','playerAright choice');
			Meteor.call('updatePlayerChoice',game._id,'Right');
			Meteor.call('updateGameState',game._id, 'playerBdeciding');
		}	
	},	
	'click .playerBleft': function(event){
		event.preventDefault();
		var game= Games.findOne();
		var currentUser = Meteor.userId();
		if(currentUser == game.playerB && game.state=='playerBdeciding'){
			$(".playerBleft").attr('class','playerBleft choice');
			Meteor.call('updatePlayerChoice', game._id,'Left');
			Meteor.call('updateGameState',game._id, 'payoffs');
		}	Router.route('payoffs');
	},
	'click .playerBright': function(event){
		event.preventDefault();
		var game= Games.findOne();
		var currentUser = Meteor.userId();
		if(currentUser == game.playerB && game.state=='playerBdeciding'){
			$(".playerBright").attr('class','playerBright choice');
			Meteor.call('updatePlayerChoice', game._id, 'Right');
			Meteor.call('updateGameState',game._id, 'payoffs');
		}	Router.route('payoffs');
	}	
});

//Mesage input event handler
Template.messageForm.events({
		'keydown input.form-control': function(event){
		if(event.which===13){
        var message = event.target.value;
        var gameId = Games.findOne()._id;
        Meteor.call('addMessage',gameId, message);
        Meteor.call('updateGameState',gameId, 'playerAdeciding');
        event.target.value = "";
    	}	

	}
});

//Message display template helper
Template.messageDisplay.helpers({
	message: function(){
		return Games.findOne().message;
	}
});
