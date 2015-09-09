//Submit the HIT
Template.endSurveyDisconnect.events({
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
