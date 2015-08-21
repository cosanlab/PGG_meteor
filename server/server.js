Meteor.startup(function(){
	try{
		//Create a test batch for now and give it an assigner
		Batches.upsert({name: 'Test_1'}, {name: 'Test_1', active: true});
		var batch = TurkServer.Batch.getBatchByName('Test_1');
		batch.setAssigner(new TurkServer.Assigners.UGAssigner());
	} 
	catch(e){
		console.log(e);
		return;
	}
});

///Subjects DB
Meteor.publish('Players', function(){
	return Players.find({},{fields: {name:1, enterTime:1, status:1}});
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
			status: 'waiting'
		};
		if(!currentUser){
			throw new Meteor.Error("not-logged-in", "You're not logged in.");
		}
		Players.insert(data);
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
	'updatePlayer': function(playerId, state){
		check(state,String);
		return Players.update(playerId, {$set: {status: state}});
	},
	'playerFinished': function(gameId){
		//Update the clients's status in the Players DB
		//then check both players status in the Players DB based on player ids 
		//in the game they were playing. If both are finished, change the game
		//status to ended
		var currentUser = Meteor.userId();
		Meteor.call('updatePlayer', currentUser,'finished');
		var game = Games.findOne({_id:gameId});
		var playerAstatus = Players.findOne({_id:game.playerA}).status;
		var playerBstatus = Players.findOne({_id:game.playerB}).status;
		
		if(playerAstatus == 'finished' && playerBstatus == 'finished'){
			Meteor.call('updateGameState',game._id,'ended');
		}		
	},	

	//GAMES DB METHODS
	'createGame': function(playerIds){
		var clientId = playerIds[0];
		var partnerId = playerIds[1];
		//Randomize condition; with or without messaging enabled
		var messageCond = Math.random() > 0.5;
		var cond;
		if(messageCond){
			cond = 'withMessaging';
		} else {
			cond = 'noMessaging';
		}
		// Randomize role
		var isPlayerA = Math.random() > 0.5;
		var data;
		if(isPlayerA){
			data = {
				playerA: clientId,
				playerB: partnerId,
				state: "instructions",
				playerAReady:false,
				playerBReady:false,
				condition: cond
			};
		} else {
			data = {
				playerA: partnerId,
				playerB: clientId,
				state: "instructions",
				playerAReady:false,
				playerBReady:false,
				condition: cond
			};
		}
		//Since the server is doing a db instance and isn't a user, need to pretend to be a user who has access to the slice of the db in order to insert
		Partitioner.bindUserGroup(clientId,function(){
			Games.insert(data);
		});
		return;
	},

	'updateGameState': function(gameId, state) {
		return Games.update(gameId, {$set: {'state':state}});
	},
	'updatePlayerChoice': function(gameId, player, choice) {
		var gameState = Games.findOne({_id:gameId}).state;
		if(player == 'A'){
			return Games.update(gameId, {$set: {'PlayerAChoice':choice}});
		} else if(player == 'B'){
			return Games.update(gameId, {$set: {'PlayerBChoice':choice}});
		} else {
			throw new Meteor.error ('playerError','Unrecognized player. Games not updated with player decision!');
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
		game = Games.findOne(); //Re-query the db after the update

		if(game.playerAReady && game.playerBReady && game.condition == 'withMessaging'){
			Meteor.call('updateGameState',game._id, "playerBmessaging");
		} else if(game.playerAReady && game.playerBReady && game.condition == 'noMessaging'){
			Meteor.call('updateGameState',game._id, "playerAdeciding");
		}
	},
	'addMessage':function(gameId, message){
		Games.update(gameId, {$set:{'message':message}});
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
		}
	},

	//Send a user back to the lobby
	goToLobby: function(currentUser){
		var inst = TurkServer.Instance.currentInstance();
		if (inst == null){
			console.log('No instance for ' + currentUser + '. User not sent to lobby!');
			return;
		}
		inst.sendUserToLobby(currentUser);
	}
});

