//Display partner disconnect and bonus payment info
Template.endSurvey.helpers({
	playerStatus: function(){
		return Players.findOne(Meteor.userId()).status;
	},
	ineligibilityPay: function(){
		return ineligibilityPay;
	},
	lobbyTimeoutPay: function(){
		return lobbyTimeoutPay;
	},
	lobbyTimeout: function(){
		return lobbyTimeout;
	},
	disconnectPay: function(){
		return disconnectPay;
	},
	bonus: function(){
		var currentUser = Meteor.userId();
		return Players.findOne(currentUser).bonus/100;
	}
});

//Submit the HIT
Template.endSurvey.events({
	'click button': function(event){
		event.preventDefault();
		var currentUser = Meteor.userId();
		var player = Players.findOne(currentUser);
		var feedback = $('#feedback').val();
		var age = $('#age').val();
		var results;
		//For the sake no blank fields fill NAs if client doesn't complete exit survey
		if(!feedback){
			if(player.status == 'failedQuiz'){
				feedback = 'FAILED QUIZ';
			} else{
				feedback = 'NA';
			}
		}
		if (!age){
			age = 'NA';
		}
		var gender;
		if($('#male').is(':checked')){
			gender = 'male';
		} else if($('#female').is(':checked')){
			gender = 'female';
		} else{
			gender = 'NA';
		}
		Meteor.call('updatePlayerInfo',currentUser,{'feedback':feedback},'set');
		//Meteor.call('addPlayerExitInfo...')
		results = {Feedback: feedback};
		TurkServer.submitExitSurvey(results);
	}
});


