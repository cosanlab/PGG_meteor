Template.game.helpers({
	game: function(){
		var game = Games.findOne();
		return {
			condition: game.condition,
			state: game.state,
			round: game.round,

		};
	},
});

//Player Contribution Template
Template.playerContribution.onRendered(function(){
	$(".action-tutorial-finish").text("Continue");
});

Template.playerContribution.helpers({
    limit: function () {
      return "50";
    }
  });

Template.playerContribution.events({
	'click button': function(event){
		event.preventDefault();
		$('#contributionSubmit').text(" ");
		var contribution = $('#contributionSubmit').val();
		var gameId = Games.findOne()._id;
		var currentUser = Meteor.userId();
		var nextState = 'pDisp';
		var autoState = 'pSendMess1';
		var delay = 5000;
		Meteor.call('addPlayerRoundData',gameId,currentUser,['contributions',contribution,nextState,autoState,delay]);
	}
});

//Player Display Template
Template.playerDisplay.helpers({
	'contributions': function(){
		var currentUser = Meteor.userId();
		var game = Games.findOne();
		var allNames = letters.slice(0,groupSize);
		var pName = game.players[currentUser].name;
		var pIdx = _.indexOf(allNames,pName);
		var round = game.round;
		var neighborContributions = [];
		if(game.condition == '2G'){
			var neighborName;
			var neighborId;
			for(var i = -1; i < 2; i+=2){
				neighborName =  allNames.slice(pIdx + i);
				neighborId = _.findkey(game.players,
					function(key){return key.name == neighborName;});
				neighborContributions.push(game.players[neighborId].contribution[round]);
			}
		}
		return neighborContributions;
	}

});


//Messaging Templates
Template.messageForm.inheritsHelpersFrom('game');
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
        	Meteor.call('updateGameState', gameId, 'beliefs');
        //	Meteor.setTimeout(function(){
      	//		Meteor.call('updateGameState',gameId, 'beliefs');
    	//	},10000);
        	event.target.value = "";
    	}
	}
});

Template.messageDisplay.inheritsHelpersFrom('game');
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

//Player/Group earnings template
Template.playerEarnings.helpers({

});

