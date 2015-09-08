Meteor.startup(function(){
	try{
		//Create a test batch for now and give it an assigner
		Batches.upsert({name: 'Test_1'}, {name: 'Test_1', active: true});
		TurkServer.ensureTreatmentExists({name: 'withMessaging'});
    	TurkServer.ensureTreatmentExists({name: 'noMessaging'});
		Batches.update({name: 'Test_1'}, {$addToSet: {treatments: 'withMessaging'}});
		Batches.update({name: 'Test_1'}, {$addToSet: {treatments: 'noMessaging'}});
		//var batch = TurkServer.Batch.getBatchByName('Test_1');
		//batch.setAssigner(new TurkServer.Assigners.UGAssigner(2));
		 Batches.find().forEach(function(batch) {
		 	TurkServer.Batch.getBatch(batch._id).setAssigner(new TurkServer.Assigners.UGAssigner(2));
    		});
	} 
	catch(e){
		console.log(e);
		return;
	}
});

//User connection functions
/*
var endGame = false;
UserStatus.events.on('connectionLogin', function(fields){
	console.log(fields);
	//var reconnUser = fields.userId;
});
UserStatus.events.on('connectionLogout', function(fields){
	console.log(fields);
	//var reconnUser = fields.userId;
});
//If any user ever disconnects 

UserStatus.events.on('connectionLogout', function(fields){
	//Find out who they are
	var disconUser = fields.userId;
	//And the game they belong to
	var game = Partitioner.bindUserGroup(disconUser,function(){
		return Games.find({"$or": [{"playerA":disconUser},{"playerB":disconUser}]});
	});
	//If that game hasn't ended return the start timeout flag
	if (game.state != 'ended'){
		return true;
	}
});

*/

///Subjects DB
Meteor.publish('Players', function(){
	return Players.find({},{fields: {name:1, enterTime:1, status:1, quizAttempts:1, passedQuiz:1,needRematch:1}});
});

//Games DB
Meteor.publish('Games', function(){
    // Security
    var currentUser = this.userId;		
	return Games.find({"$or": [{"playerA":currentUser},{"playerB":currentUser}]});
});


Meteor.methods({
	//PLAYERS DB METHODS
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
		if(!currentUser){
			throw new Meteor.Error("not-logged-in", "You're not logged in.");
		}
		Players.insert(data);
	},
	'passedQuiz':function(currentUser){
		Players.update(currentUser, {$set: {passedQuiz: true}}, {$inc: {quizAttempts: 1}});
	},
	'incQuizAttempts':function(currentUser){
		return Players.update(currentUser, {$inc: {quizAttempts: 1}});
	},
	'removePlayer': function(currentUser){
		var data = {
			_id: currentUser,
		};
		if(!currentUser){
			throw new Meteor.Error("not-logged-in", "You're not logged in.");
		}
		return Players.remove(data);
	},
	'updatePlayerState': function(playerId, state){
		check(state,String);
		return Players.update(playerId, {$set: {status: state}});
	},
	'playerFinished': function(gameId){
	//Update the clients's status in the Players DB then check both players status in the Players DB based on player ids in the game they were playing. If both are finished, change the game status to ended
		var currentUser = Meteor.userId();
		Meteor.call('updatePlayerState', currentUser,'finished');
		var game = Games.findOne({_id:gameId});
		var playerAstatus = Players.findOne({_id:game.playerA}).status;
		var playerBstatus = Players.findOne({_id:game.playerB}).status;
		
		if(playerAstatus == 'finished' && playerBstatus == 'finished'){
			Meteor.call('updateGameState',game._id,'ended');
		}		
	},
	
	'resetPlayerRematch':function(playerId){
	//When a player is matched in the lobby ensure their rematch state is reset
		return Players.update(playerId, {$set: {'needRematch':false}});
	},	

	//GAMES DB METHODS
	'createGame': function(playerIds, condition){
		var clientId = playerIds[0];
		var partnerId = playerIds[1];
		//Randomize condition; with or without messaging enabled
		var messageCond = condition;
		var cond;
		if(messageCond){
			cond = 'withMessaging';
		} else {
			cond = 'noMessaging';
		}
		//Find out if any players have been matched before
		var clientRematched = Players.findOne(clientId).needRematch;
		var partnerRematched = Players.findOne(partnerId).needRematch;
		// Randomize role
		var isPlayerA = Math.random() > 0.5;
		var data;
		if(isPlayerA){
			data = {
				playerA: clientId,
				playerArematched: clientRematched,
				playerB: partnerId,
				playerBrematched: partnerRematched,
				state: "instructions",
				playerAReady:false,
				playerBReady:false,
				condition: messageCond
			};
		} else {
			data = {
				playerA: partnerId,
				playerArematched: partnerRematched,
				playerB: clientId,
				playerBrematched: clientRematched,
				state: "instructions",
				playerAReady:false,
				playerBReady:false,
				condition: messageCond
			};
		}
		//Since the server is doing a db instance and isn't a user, need to pretend to be a user who has access to the slice of the db in order to insert
		Partitioner.bindUserGroup(clientId,function(){
			Games.insert(data);
		});
		Meteor.call('resetPlayerRematch', clientId);
		Meteor.call('resetPlayerRematch', partnerId);
	},

	'updateGameState': function(gameId, state) {
		return Games.update(gameId, {$set: {'state':state}});
	},
	'updatePlayerChoice': function(gameId, player, choice) {
		var gameState = Games.findOne({_id:gameId}).state;
		if(player == 'A'){
			return Games.update(gameId, {$set: 
				{'PlayerAChoice':choice, 'PlayerART':Date.now()}});
		} else if(player == 'B'){
			return Games.update(gameId, {$set: 
				{'PlayerBChoice':choice, 'PlayerBRT':Date.now()}});
		} else {
			throw new Meteor.error ('playerError','Unrecognized player. Games not updated with player decision!');
		}
	},
	'playerInstrComplete':function(gameId, currentUser){
	//Update players status in Games db if they passed the quiz and if both have change game state to assignment
		var game = Games.findOne(gameId);
		if(currentUser == game.playerA){
			data = {
				playerAInstrComp: true
			};
		}
		else if(currentUser == game.playerB){
			data= {
				playerBInstrComp: true
			};
		}
		Games.update(gameId, {$set: data});

		//Update game status to assignment if both players have passed the quiz 
		game = Games.findOne({_id:gameId}); //Re-query the db after the update

		if(game.playerAInstrComp && game.playerBInstrComp){
			return Games.update(gameId, {$set: {'state':"assignment"}});
		} 

	},
	'playerReady': function(gameId, currentUser){
		// update player status to ready for games matching gameId
		var game = Games.findOne({_id:gameId});
		if(currentUser == game.playerA){
			data = {
				playerAReady:true
			};
		}
		else if (currentUser == game.playerB) {
			data = {
				playerBReady:true
			};
		}
		Games.update(gameId, {$set: data});

		//Update game status to playing if both player statuses are ready
		//Make sure we take game condition into account
		//Also record the time the experiment started to calculate RTs
		game = Games.findOne({_id:gameId}); //Re-query the db after the update

		if(game.playerAReady && game.playerBReady && game.condition == 'withMessaging'){
			return Games.update(gameId, {$set: {'state':"playerBmessaging", 'GameStart': Date.now()}});
			//Meteor.call('updateGameState',game._id, "playerBmessaging");
		} else if(game.playerAReady && game.playerBReady && game.condition == 'noMessaging'){
			return Games.update(gameId, {$set: {'state':"playerAdeciding", 'GameStart': Date.now()}});
		}
	},
	'addMessage':function(gameId, message){
		Games.update(gameId, {$set:
			{'message':message, 'messageRT':Date.now()}});
	},
	'addPlayerFeedback': function(playerId, feedback){
		var game = Games.findOne({"$or": [{"playerA":playerId},{"playerB":playerId}]});
		var gameId = game._id;
		if(playerId == game.PlayerA){
			Partitioner.bindUserGroup(playerId,function(){
				Games.update(gameId,{$set:{'playerAfeedback': feedback}});
			});
		}
		else if(playerId == game.PlayerB){
			Partitioner.bindUserGroup(playerId,function(){
				Games.update(gameId,{$set:{'playerBfeedback': feedback}});
			});		}
	},

	//TURKSERVER METHODS

	//Calculate bonuses and shut down the instance, which both users won't be in anymore because this only gets call from the exit survey
	'calcBonuses': function(gameId, currentUser){
		//First calculate bonuses
		var asst = TurkServer.Assignment.currentAssignment();
		var game = Games.findOne({_id:gameId});
		if(game.PlayerAChoice == 'Left'){
			Abonus = 0.10;
			Bbonus = 0.30;
		} else if(game.PlayerBChoice == 'Left'){
			Abonus = 0;
			Bbonus = 0.1;
		} else if(game.PlayerBChoice == 'Right'){
			Abonus = 0.2;
			Bbonus = 0.2;
		}
		if(currentUser == game.playerA){
			asst.addPayment(Abonus);
		} else if(currentUser == game.playerB){
			asst.addPayment(Bbonus);
		}

		Meteor.call('endExperiment');
	},

	'endExperiment': function(){
	//Shut down experiment instance
		var exp = TurkServer.Instance.currentInstance();
		if (exp != null){
			exp.teardown(returnToLobby = false);	
		} else{
			console.log("Could not teardown instance. Does not exist!");
		}
	},

	goToLobby: function(currentUser){
	//Send a user back to the lobby
		var inst = TurkServer.Instance.currentInstance();
		if (inst == null){
			console.log('No instance for ' + currentUser + '. User not sent to lobby!');
			return;
		}
		inst.sendUserToLobby(currentUser);
	},

	failedQuiz: function(currentUser,gameId){
	//If a user fails the quiz set the game status to failed, send the user to the alternate exit survey, and send the other user back to the lobby with a rematch needed flag
		Games.update(gameId,{$set:{'state': 'failedQuiz'}});
		var game = Games.findOne(gameId);
		var partner;
		if(currentUser == game.playerA){
			partner = game.playerB;
		} else{
			partner = game.playerA;
		}
		Players.update(partner,{$set:{'status':'waiting','quizAttempts':0,'passedQuiz':false, 'needRematch':true}});
		Meteor.call('goToLobby', partner);

		Players.update(currentUser,{$set:{'status':'failedQuiz'}});
		asst = TurkServer.Assignment.currentAssignment();
		asst.showExitSurvey();
		Meteor.call('endExperiment');


	}
});

