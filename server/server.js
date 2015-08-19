//We're going to have to create our own assigner. Currently Simple and Test Assigner both push clients directly to the experiment, which I've handled for now by simply starting our experiment from within TurkServers.inExperiment, effectively by-passing their lobby entirely

//Share instance with other users and go straight to expermient
Meteor.startup(function(){
	try{
		//Create a test batch for now and give it an assigner
		Batches.upsert({name: 'Test_1'}, {name: 'Test_1', active: true});
		var batch = TurkServer.Batch.getBatchByName('Test_1');
		batch.setAssigner(new TurkServer.Assigners.TestAssigner);
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
	// return Games.find( {} );
});



Meteor.methods({
	
	//PLAYERS DB METHODS
	'addPlayer': function(){
		var currentUser = Meteor.userId();
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
	'removePlayer': function(){
		var currentUser = Meteor.userId();
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
	'matchPlayers': function(){
		var clientId = Meteor.userId();
		var numPlayers = Players.find({status:'waiting'},{}).count();
		if (numPlayers >= 2){
			// Grab the earliest waiting player
			var partnerId = Players.findOne({status:'waiting'},{sort:{enterTime:1}})._id;
			// update each player's status
			Meteor.call('updatePlayer', clientId, 'instructions');
			Meteor.call('updatePlayer', partnerId, 'instructions');
			// Create a game
			Meteor.call('createGame', clientId, partnerId);
		}
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
	'createGame': function(clientId, partnerId){
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

		// Add Game to DB
		return Games.insert(data);
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
			throw new Meteor.error ('playerError','Unrecognized player. Games not updated with player deciision!');
		}
	},
	'playerReady': function(gameId){
		// update player status to ready for games matching gameId
		var currentUser = Meteor.userId();
		if(Games.find({_id:gameId}).fetch()[0].playerA == currentUser){
			data = {
				playerAReady:true
			};
		}
		if (Games.find({_id:gameId}).fetch()[0].playerB == currentUser) {
			data = {
				playerBReady:true
			};
		}
		Games.update(gameId, {$set: data});
		//Update game status to playing if both player statuses are ready
		//Make sure we take game condition into account
		if(Games.find({_id:gameId}).fetch()[0].playerAReady && Games.find({_id:gameId}).fetch()[0].playerBReady && Games.find({_id:gameId}).fetch()[0].condition == 'withMessaging'){
			Meteor.call('updateGameState',gameId, "playerBmessaging");
		} else if(Games.find({_id:gameId}).fetch()[0].playerAReady && Games.find({_id:gameId}).fetch()[0].playerBReady && Games.find({_id:gameId}).fetch()[0].condition == 'noMessaging'){
			Meteor.call('updateGameState',gameId, "playerAdeciding");
		}
	},
	'addMessage':function(gameId, message){
		Games.update(gameId, {$set:{'message':message}});
	},

	//TURKSERVER METHODS

	//End the experiment sending the client back to the lobby
	//This needs to be modified to only act on the client who called the method
	'goToEndSurvey': function(gameId){
		//First calculate bonuses
		var asst = TurkServer.Assignment.currentAssignment();
		var currentUser = Meteor.userId();
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
		var exp = TurkServer.Instance.currentInstance();
		exp.teardown();
	}
});

