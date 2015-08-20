//When a client clicks the submit button it tears down the experiment and submits their hit
Template.endSurvey.events({
	'click button': function(){
		Router.go('endExperiment');
		var gameId = Games.findOne()._id;
		Meteor.call('endExperiment',gameId);
		
	}
});
