Meteor.subscribe('Subjects');
Meteor.subscribe('Games');

//Create game template
Template.createGame.events({
	'click #create_game':function(){
		Meteor.call('CreateGame');
		Router.go(Router.path('game'));
		//...
	}

});

//Game template
Template.game.helpers({
	'messages':function(){
		//return messages if any in Subjects db
	}
});

Template.game.events({


});