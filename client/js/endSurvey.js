//Submit the HIT, calculate the bonuses and tear down the experiment 
Template.endSurvey.events({
	'click button': function(){
		var gameId = Games.findOne()._id;
		var currentUser = Meteor.userId();
		Meteor.call('calcBonuses', gameId, currentUser);
		TurkServer.submitExitSurvey({});	
	}
});


