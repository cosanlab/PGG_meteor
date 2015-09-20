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
		//var currentUser = Meteor.userId();
		var feedback = $('#feedback').val();
		var results = {Feedback: feedback};
		//Meteor.call('addPlayerFeedback',currentUser,feedback);
		TurkServer.submitExitSurvey(results);
		//Need this once HIT is submitted
		//Router.go('endExperiment');	
	}
});


