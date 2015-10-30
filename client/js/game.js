Template.game.helpers({
	game: function(){
		var game = Games.findOne();
		var messageSend = false;
		var messageReceive = false;
		if(game.state == 'pSendMess1' || game.state == 'pSendMess2'){
			messageSend = true;
		}
		if(game.state == 'pReceiveMess1' || game.state == 'pReceiveMess2'){
			messageReceive = true;
		}

		return {
			condition: game.condition,
			state: game.state,
			round: game.round,
			messageSend: messageSend,
			messageReceive: messageReceive

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
		var autoState = 'pSendMess1';
		var delay = 5000;
		Meteor.call('addPlayerRoundData',gameId,currentUser,['contributions',contribution,nextState,autoState,delay]);
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
		var neighborContributions = [];
		if(game.condition == '2G'){
			var neighborName;
			var neighborId;
			var idx;
			for(var i = -1; i < 2; i+=2){
				idx = (pIdx+i) < allNames.length ? (pIdx+i) : (pIdx+i-allNames.length);
				neighborName =  allNames.slice(idx)[0];
				neighborId = _.findKey(game.players,
					function(key){return key.name == neighborName;});
				neighborContributions.push(game.players[neighborId].contributions[round]);
			}
		}
		return neighborContributions;
	}

});


//Messaging Templates
Template.messageForm.inheritsHelpersFrom('game');
Template.messageForm.events({
		'click .bubbleUser' : function(event){
			event.preventDefault();
			if($('.message-form').text() == 'Type a 140 character message here...'){
				$('.message-form').text('');
			}
		},
		/*'keydown p.message-form': function(event){
			if(event.which===13){
	        	var message = $('.message-form').text();
	        	var gameId = Games.findOne()._id;
	        	//Meteor.call('addMessage',gameId, message);
	        	//Meteor.call('updateGameState', gameId, 'beliefs');
	        //	Meteor.setTimeout(function(){
	      	//		Meteor.call('updateGameState',gameId, 'beliefs');
	    	//	},10000);
	        	event.target.value = "";
	    	}
		}, */
		'keyup p.message-form': function(event){
			var charLen = 140 - $('.message-form').text().length;
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

