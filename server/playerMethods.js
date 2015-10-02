Meteor.methods({
	//Adds a new player document to the database
	'addPlayer': function(currentUser){
		var data = {
			_id: currentUser,
			name: currentUser,
			enterTime: new Date(),
			status: 'waiting',
			quizAttempts: 0,
			passedQuiz: false,
			needRematch: false,
		};
		Players.insert(data);
	},
	//Remove a player document from the database
	'removePlayer': function(currentUser){
		return Players.remove(currentUser);
	},
	//General purpose document modification function
	updatePlayerInfo: function(currentUser,data){
		return Players.update(currentUser, {$set: data});

	},
	//Lobby timer so players don't wait too long to get match
	lobbyTimeBob: function(currentUser){
		asst = TurkServer.Assignment.getCurrentUserAssignment(currentUser);
		ass.showExitSurvey();
	}
});