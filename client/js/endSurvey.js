//Submit the HIT
Template.endSurvey.events({
	'click button': function(){
		TurkServer.submitExitSurvey({});
		//Need this once HIT is submitted
		Router.go('endExperiment');	
	}
});


