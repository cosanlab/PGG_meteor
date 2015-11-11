//Display partner disconnect and bonus payment info
Template.endSurvey.helpers({
	status: function(){
		var status =  Players.findOne(Meteor.userId()).status;
		var failedQuiz = false;
		var failedMatch = false;
		var connectionError = false;
		var completed = false;
		switch (status){
			case 'failedQuiz':
				failedQuiz = true;
				break;
			case 'lobbyTimout':
				failedMatch = true;
				break;
			case 'userDisconnect':
				connectionError = true;
				break;
			case 'finished':
				completed = true;
				break;
		}
		return{
			failedQuiz: failedQuiz,
			failedMatch: failedQuiz,
			connectionError: connectionError,
			completed: completed
		};

	},
	ineligibilityPay: function(){
		return ineligibilityPay;
	},
	lobbyTimeout: function(){
		return lobbyTimeout;
	},
	disconnectPay: function(){
		return disconnectPay;
	},
	bonus: function(){
		var currentUser = Meteor.userId();
		var bonus = Players.findOne(currentUser).bonus;
		return bonus.toFixed(2);
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
			} else if(player.status == 'lobbyTimeout'){
				feedback = 'LOBBY TIMEOUT';
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


