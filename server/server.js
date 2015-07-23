//Subjects DB
Meteor.publish('Subjects', function(){
	return Subjects.find();
});

//Games DB
Meteor.publish('Games', function(){
	return Subjects.find();
});

//DB Methods
Meteor.methods({
	
	//PLAYER METHODS
	'addPlayer':function(){
		//insert subject
	},

	'addDecision':function(){
		//insert subject decision
	},

	'addMessage':function(){
		//insert subject message
	},


	//GAME METHODS
	'createGame':function(){
		//insert game
	},

	'joinGame':function(){
		//look for game by status
	},

	'endGame':function(){
		//change game status
	},

	

	

	

});