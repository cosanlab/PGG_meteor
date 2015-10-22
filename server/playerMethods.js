Meteor.methods({
	//Adds a new player document to the database
	'addPlayer': function(currentUser){
		var data = {
			_id: currentUser,
			name: currentUser,
			enterTime: new Date(),
			status: 'instructions',
			passedQuiz: false,
			quizAttempts: 0,
			needRematch: false,
		};
		Players.insert(data);
	},
	'checkPlayerEligibility': function(currentUser,passedQuiz){
		//Updates the Assignments db with a boolean about whether user passed the comprehension quiz. If so emits an event (which is tied to the current users's batch) that tells the assigner to try and match lobby users, otherwise shows them the exit survey 
		var asst = TurkServer.Assignment.getCurrentUserAssignment(currentUser);
		var asstId = asst.asstId;
		var batchId = asst.batchId;
		var batch = TurkServer.Batch.getBatch(batchId);
		Assignments.update(asstId,{$set:{'passedQuiz':passedQuiz}});
		console.log("I did something!");
		if(passedQuiz){
			Meteor.call('updatePlayerInfo',currentUser,{'status':'waiting'},'set');
			var userLobbyBomb = Meteor.setTimeout(function(){
          		Meteor.call('lobbyTimeBomb',asstId);
        		},5000);
        	batch.assigner.lobbyTimers.push({currentUser:userLobbyBomb});
        	var emitter = batch.lobby.events;
        	emitter.emit('match-players');
        	console.log('Meteor ID: ' + currentUser + ' passed Quiz! Triggering lobby match event and starting timebomb.');
      	} else{
      		Meteor.call('updatePlayerInfo',currentUser,{'status':'failedQuiz'},'set');
      		asst.showExitSurvey();
      	}	
	},
	//Remove a player document from the database
	'removePlayer': function(currentUser){
		return Players.remove(currentUser);
	},
	//General purpose document modification function
	updatePlayerInfo: function(currentUser,data,func){
		if(func == 'set'){
			return Players.update(currentUser, {$set: data});
		} else if(func == 'inc'){
			return Players.update(currentUser, {$inc: data});
		} else if(func == 'dec'){
			return Players.update(currentUser, {$dec: data});
		}

	},
	//Lobby timer so players don't wait too long to get match
	lobbyTimeBomb: function(currentUser){
		asst = TurkServer.Assignment.getCurrentUserAssignment(currentUser);
		var asstId = asst.asstId;
		Meteor.call('updatePlayerInfo',asstId,{'status':'lobbyTimeout'}, 'set');
		asst.showExitSurvey();
		console.log('Meteor ID: ' + currentUser + ' sent to exit survey because lobby timer went off!');
	}
});