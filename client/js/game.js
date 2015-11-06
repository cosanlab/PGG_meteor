Template.game.helpers({
	game: function(){
		var game = Games.findOne();
		var messaging = false;
		var messagePrompt;
		var messageSubPromptDisplay = "";
		var messageSubPrompt = "Placeholder text";
		var messageSubPrompt2 = "Placeholder text";
		if(game.state == 'pSendMess1' || game.state == 'pSendMess2'){
			messaging = true;
			messagePrompt = 'What do you want to say to another player?';
			messageSubPrompt = "Insert your message below:";
			messageSubPrompt2 = "(press Enter to send)";
		}
		if(game.state == 'pReceiveMess1' || game.state == 'pReceiveMess2'){
			messaging = true;
			messagePrompt = 'Your conversation:';
			messageSubPromptDisplay = 'visibility:hidden';
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
		var delay = 5000;
		Meteor.call('addPlayerRoundData',gameId,currentUser,['contributions',contribution,nextState,autoStates],delay);
	}
});

//Other's contributions Display Template
Template.playerDisplay.helpers({
	'contributions': function(){
		var currentUser = Meteor.userId();
		var game = Games.findOne();
		var allNames = letters.slice(0,groupSize);
		var pName = game.players[currentUser].name;
		var pIdx = _.indexOf(allNames,pName);
		var round = game.round - 1;
		var contributions = [];
		var data = {};
		if(game.condition == '2G'){		
			var neighbors = game.players[currentUser].neighbors;	
			for (var n=0, nLen = neighbors.length; n<nLen; n++){
				data.name = game.players[neighbors[n]].name;
				data.amount = game.players[neighbors[n]].contributions[round];
				contributions.push(data);
				data = {};
			}
		} else{
			for (var p in _.keys(game.players)){
				data.name = game.players[p].name;
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
			partnerName: game.players[partner].name
		};
	}
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
		        		autoStates = ['gOut','pChoose'];
		        		messageField = 'secondMessages';
		        	}
		        	Meteor.call('addPlayerRoundData',game._id,currentUser,[messageField,message,nextState,autoStates],delay);
		    	}
		    }
		}, 
		'keyup #message-form': function(event){
			var charLen = charLim - $('#message-form').text().length;
			$('#charCount').text(charLen + '/140');
			if(charLen < 0){
				$('.bubbleUser').css('background','#FF8080');
				$('.bubbleTriangleUser').css('border-color','#FF8080');
			}else{
				$('.bubbleUser').css('background','#99FFFF');
				$('.bubbleTriangleUser').css('border-color','#99FFFF');
				
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
		roundEarnings = Math.round((pot * 1.5)/_.pluck(game.players, 'name').length);
		totalEarnings = 100 - game.players[currentUser].contributions[round] + roundEarnings;
		return {
			round: roundEarnings,
			total: totalEarnings
		};
	}

});

