Template.game.helpers({
	game: function(){
		var game = Games.findOne();
		var messaging = false;
		var messagePrompt;
		var messageSubPromptDisplay = "";
		var messageSubPrompt = "Placeholder text";
		var messageSubPrompt2 = "Placeholder text";
		switch(game.state){
			case 'pChoose':
				messagePrompt = 'How many points do you want to contribute to the group account?';
				break;
			case 'pDisp':
				messagePrompt = 'Two other players contributed:';
				break;
			case 'pSendMess1':
				messaging = true;
				messagePrompt = 'What do you want to say to another player?';
				messageSubPrompt = "Insert your message below:";
				messageSubPrompt2 = "(press Enter to send)";
				break;
			case 'pSendMess2':
				messaging = true;
				messagePrompt = 'What do you want to say to another player?';
				messageSubPrompt = "Insert your message below:";
				messageSubPrompt2 = "(press Enter to send)";
				break;
			case 'pReceiveMess1':
				messaging = true;
				messagePrompt = 'Your conversation:';
				messageSubPromptDisplay = 'visibility:hidden';
				break;	
			case 'pReceiveMess2':
				messaging = true;
				messagePrompt = 'Your conversation:';
				messageSubPromptDisplay = 'visibility:hidden';
				break;
			case 'gOut':
				messagePrompt = 'Your earnings:';
				break;
			case 'playerRatings':
				messagePrompt = 'How much would you want to player with each player again?';
				messageSubPrompt = 'Rate using the sliders below:';
				messageSubPrompt2 = 'Left = "Never"    Right = "Definitely"';
				break;
			case 'finalOut':
				messagePrompt = 'Final Earnings:';
				messageSubPrompt = '(Your earnings are highlighted)';
		}

		return {
			condition: game.condition,
			state: game.state,
			round: game.round,
			messaging: messaging,
			messagePrompt: messagePrompt,
			messageSubPromptDisplay: messageSubPromptDisplay,
			messageSubPrompt: messageSubPrompt,
			messageSubPrompt2: messageSubPrompt2
		};
	}
});

//Player Contribution Template
Template.playerContribution.helpers({
    limit: function () {
      return "50";
    }
  });

Template.playerContribution.events({
	'click button': function(event){
		event.preventDefault();
		$('#contributionSubmit').text("Waiting for other players... ").removeClass('btn-primary').addClass('btn-warning').prop('disabled',true);
		var contribution = parseInt($('#contributionSlider').val());
		var gameId = Games.findOne()._id;
		var currentUser = Meteor.userId();
		var nextState = 'pDisp';
		var autoStates = ['pSendMess1'];
		var delay = 7000;
		Meteor.call('addPlayerRoundData',gameId,currentUser,['contributions',contribution,nextState,autoStates],delay);
	}
});

//Other's contributions Display Template
Template.playerDisplay.helpers({
	'contributions': function(){
		var currentUser = Meteor.userId();
		var game = Games.findOne();
		var round = game.round - 1;
		var contributions = [];
		var data = {};
		if(game.condition == '2G'){		
			var neighbors = game.players[currentUser].neighbors;	
			for (var n=0, nLen = neighbors.length; n<nLen; n++){
				data.icon = game.players[neighbors[n]].icon;
				data.amount = game.players[neighbors[n]].contributions[round];
				contributions.push(data);
				data = {};
			}
		} else{
			for (var p in game.players){
				data.icon = game.players[p].icon;
				data.amount = game.players[p].contributions[round];
				contributions.push(data);
			}
		}
		return contributions;
	}

});


//Messaging Templates
Template.messageForm.inheritsHelpersFrom('game');
Template.messageForm.helpers({
	'messageContent':function(){
		var game = Games.findOne();
		var round = game.round - 1;
		var currentUser = Meteor.userId();
		var partner = game.players[currentUser].partner;
		return {
			usrMess1: game.players[currentUser].firstMessages[round],
			partnerMess1: game.players[partner].firstMessages[round],
			usrMess2: game.players[currentUser].secondMessages[round],
			partnerMess2: game.players[partner].secondMessages[round],
			partnerIcon: game.players[partner].icon,
			userIcon: game.players[currentUser].icon
		};
	}
});
Template.messageForm.onRendered(function(){
	$('#messagePrompt').html('Insert your message below <br><span>(press Enter to send)</span>');
});
Template.messageForm.events({
		'click #userMessage' : function(event){
			event.preventDefault();
			if($('#message-form').text() == 'Type a 140 character message here...'){
				$('#message-form').text('');
			}
		},
		'keydown #message-form': function(event){
			event.stopPropagation();
			if(event.which===13){
				if($('#message-form').text().length <= charLim){
		        	var message = $('#message-form').text();
		        	$('#message-form').prop('contenteditable','false');
		        	$('#charCount').css('visibility','hidden');
		        	$('#messagePrompt').html('Waiting for other players... <br><span style = "visibility:hidden">placeholder</span>');
		        	var currentUser = Meteor.userId();
		        	var game = Games.findOne();
		        	var delay = 5000;
		        	var nextState;
		        	var autoStates;
		        	var messageField;
		        	if (game.state == 'pSendMess1'){
		        		nextState = 'pReceiveMess1';
		        		autoStates = ['pSendMess2'];
		        		messageField = 'firstMessages';
		        	}
		        	else if(game.state == 'pSendMess2'){
		        		nextState = 'pReceiveMess2';
		        		messageField = 'secondMessages';
		        		if(game.round < numRounds){
		        			autoStates = ['gOut','pChoose'];
		        		}
		        		else{
		        			autoStates = ['gOut','playerRatings'];
		        		}		        		
		        	}
		        	Meteor.call('addPlayerRoundData',game._id,currentUser,[messageField,message,nextState,autoStates],delay);
		    	}
		    }
		}, 
		'keyup #message-form': function(event){
			var charLen = charLim - $('#message-form').text().length;
			$('#charCount').text(charLen + '/140');
			if(charLen < 0){
				$('.activeBubble').addClass('overCharLimit');
			}else{
				$('.activeBubble').removeClass('overCharLimit');		
			}
		}

});

//Player/Group earnings template
Template.playerEarnings.helpers({
	earnings: function(){
		var game = Games.findOne();
		var currentUser = Meteor.userId();
		var round = game.round-2; //Executes after round counter is updated so -2 
		var roundEarnings;
		var totalEarnings;
		//Underscore js map reduce magic
		var pot = _.reduce(_.map(_.pluck(game.players,'contributions'),function(list){return list[round];}),function(a,b){return a+b;});
		//var pot = 0;
		//_.forEach(game.players,function(c){
		//	pot += c.contributions[round];
		//});
		roundEarnings = Math.round((pot * 1.5)/groupSize);
		totalEarnings = 100 - game.players[currentUser].contributions[round] + roundEarnings;
		return {
			round: roundEarnings,
			total: totalEarnings
		};
	}

});

//Player ratings template
Template.playerRatings.inheritsHelpersFrom('game');
Template.playerRatings.helpers({
    limit: function () {
      return "50";
    },
    players: function(){
    	var currentUser = Meteor.userId();
    	var game = Games.findOne();
    	var otherPlayers = _.omit(game.players,currentUser);
    	var others = [];
		var data = {};
		for (var p in otherPlayers){
			data.icon = otherPlayers[p].icon;
			data.user = p;
			others.push(data);
			data = {};
		}
		return others;
	}
  });

Template.playerRatings.events({
	'submit .ratings': function(event){
		event.stopPropagation();
		event.preventDefault();
		$('#messagePrompt').html('Waiting for other players... <br><span style = "visibility:hidden">placeholder</span>');
		$('#submitRatings').text("Waiting for other players... ").removeClass('btn-primary').addClass('btn-warning').prop('disabled',true);
		var form = event.target;
		var currentUser = Meteor.userId();
		var game = Games.findOne();
		var otherPlayers = _.omit(game.players,currentUser);
		var ratings = {};
		for (var p in otherPlayers){
			ratings[p] = form[p].value;
		}
		console.log(ratings);
		var nextState = 'finalOut';
		var autoStates = ['ended'];
		var delay = 7000;
		Meteor.call('addPlayerStaticData',game._id,currentUser,['playerRatings',ratings,nextState,autoStates],delay);
	}
});

//Final outcomes template
Template.finalOutcomes.inheritsHelpersFrom('game');
Template.finalOutcomes.helpers({
	'totals': function(){
		var currentUser = Meteor.userId();
		var game = Games.findOne();
		return sumsortEarnings(game,currentUser);
	}

});
//Computes the earning per player, per round by computing the pot and bonus for each round for each player			
function sumsortEarnings(game,currentUser){
	var pot;
	var bonuses = [];
	var playerTotals = [];
	for(var i = 0; i<numRounds; i++){
		pot = _.reduce(_.map(_.pluck(game.players,'contributions'),function(list){return list[i];}),function(a,b){return a+b;});
		bonuses.push(Math.round(pot*1.5/groupSize));
	}
	var data = {};
	for(var p in game.players){
		data.name = p;
		data.icon = game.players[p].icon;
		data.total = _.reduce(_.map(game.players[p].contributions,function(elem){return 100-elem+bonuses[_.indexOf(game.players[p].contributions,elem)];}),function(a,b){return a+b;});
		data.isplayer = p==currentUser;
		playerTotals.push(data);
		data={};
	}
	return _.sortByOrder(playerTotals,'total','desc');
}			

