//Allow client to submit HIT to mturk, but take them back to lobby
//Need to figure out how to handle new subjects in the lobby who want to get matched 
Template.endSurvey.events({
	'click button': function(){
		TurkServer.submitExitSurvey({});
	}
});