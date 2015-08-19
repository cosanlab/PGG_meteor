//Allow client to submit HIT to mturk
Template.endSurvey.events({
	'click button': function(){
		TurkServer.submitExitSurvey({});
	}
});