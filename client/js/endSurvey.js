//Submit the HIT, calculate the bonuses and tear down the experiment 
Template.endSurvey.events({
	'click button': function(){
		TurkServer.submitExitSurvey({});	
	}
});


