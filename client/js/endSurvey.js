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
	}
});

//Submit the HIT
Template.endSurvey.events({
	'click button': function(event){
		event.preventDefault();
		var currentUser = Meteor.userId();
		var feedback = $('#feedback').val();
		var results = {Feedback: feedback};
		var age = $('#age').val();
		//For the sake no blank fields fill NAs if client doesn't complete exit survey
		if(!feedback){
			feedback = 'NA';
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
		Meteor.call('addPlayerExitInfo',currentUser,feedback, age, gender);
		TurkServer.submitExitSurvey(results);
	}
});


