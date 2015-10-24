//Display partner disconnect and bonus payment info
Template.endSurvey.helpers({
	player: function(){
		var player = Players.findOne(Meteor.userId());
		var partnerDisconnected = false;
		var failedQuiz = false;
		if(player.status == 'partnerDisconnected'){
			partnerDisconnected = true;
		} else if (player.status == 'failedQuiz') {
			failedQuiz = true;
		}
		return {
			partnerDisconnected: partnerDisconnected,
			failedQuiz: failedQuiz
		};
	},
	ineligibilityPay: function(){
		return ineligibilityPay;
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
			if(player.status != 'failedQuiz'){
				feedback = 'NA';
			} else{
				feedback = 'FAILED QUIZ';
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


