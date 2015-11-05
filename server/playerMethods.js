Meteor.methods({
	//Adds a new player document to the database
	'addPlayer': function(currentUser, workerId){
		var data = {
			_id: currentUser,
			workerId: workerId,
			enterTime: new Date(),
			status: 'instructions',
			passedQuiz: false,
			quizAttempts: 0,
			needRematch: false,
		};
		Players.insert(data);
	},
	//Updates the Assignments db with a boolean about whether user passed the comprehension quiz. If so emits an event (which is tied to the current users's batch) that tells the assigner to try and match lobby users, otherwise shows them the exit survey 
	'checkPlayerEligibility': function(currentUser,passedQuiz){
		var asst = TurkServer.Assignment.getCurrentUserAssignment(currentUser);
		var workerId = asst.workerId;
		var asstId = asst.asstId;
		var batchId = asst.batchId;
		var batch = TurkServer.Batch.getBatch(batchId);
		LobbyStatus.update(currentUser,{$set:{'passedQuiz':passedQuiz}});
		if(passedQuiz){
			Meteor.call('updatePlayerInfo',currentUser,{'status':'waiting'},'set');
			var userLobbyBomb = Meteor.setTimeout(function(){
				Meteor.call('lobbyTimeBomb',currentUser);
			},lobbyTimeout*60*1000);
        	batch.assigner.lobbyTimers[currentUser] = userLobbyBomb;
        	var emitter = batch.lobby.events;
        	console.log('TURKER: ' + workerId + ' passed Quiz! Told Assigner and set them up the bomb!');
        	emitter.emit('match-players');
      	} else{
      		Meteor.call('updatePlayerInfo',currentUser,{'status':'failedQuiz'},'set');
      		asst.showExitSurvey();
      		console.log('TURKER: ' + workerId + ' failedQuiz!	Sent to exit survey!');
      	}	
	},
	//Remove a player document from the database
	'removePlayer': function(currentUser){
		return Players.remove(currentUser);
	},
	//General purpose document modification function
	'updatePlayerInfo': function(currentUser,data,operation){
		if(operation == 'set'){
			Players.update(currentUser, {$set: data});
		} else if(operation == 'inc'){
			Players.update(currentUser, {$inc: data});
		} else if(operation == 'dec'){
			Players.update(currentUser, {$dec: data});
		}

	},
	//Lobby timer so players don't wait too long to get match
	'lobbyTimeBomb': function(currentUser){
		asst = TurkServer.Assignment.getCurrentUserAssignment(currentUser);
		var workerId = asst.workerId;
		Meteor.call('updatePlayerInfo',currentUser,{'status':'lobbyTimeout'}, 'set');
		LobbyStatus.remove(currentUser);
		asst.addPayment(lobbyTimeoutPay);
		asst.showExitSurvey();
		console.log('TURKER: ' + workerId + ' went boom! Sent to exit survey!');
	}
});