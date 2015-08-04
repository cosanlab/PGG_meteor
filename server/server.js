//Subjects DB
Meteor.publish('Players', function(){
	return Players.find({},{fields: {name:1, status:1}});
});

//Games DB
Meteor.publish('Games', function(){
    // Security
    var currentUser = this.userId;		
	return Games.find({"$or": [{"playerA":currentUser},{"playerB":currentUser}]});
	// return Games.find( {} );
});

/*
Meteor.publish('gameAttendees', function(ids) {  
  return Meteor.users.find({_id: {$in: ids}}, {fields: {'profile.pictureUrl': 1, username: 1}});
});
*/

Meteor.methods({
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
	'createGame': function(clientId, partnerId){
		/* Security
		var currentUser = Meteor.userId();
		if(!currentUser){
			throw new Meteor.Error("not-logged-in", "You're not logged in.");
		}
		*/

		// Randomize role
		var isPlayerA = Math.random() > 0.5;
		var data;
		if(isPlayerA){
			data = {
				playerA: clientId,
				playerB: partnerId,
				state: "instructions",
				playerAReady:false,
				playerBReady:false
			};
		} else {
			data = {
				playerA: partnerId,
				playerB: clientId,
				state: "instructions",
				playerAReady:false,
				playerBReady:false
			};
		}

		// update each player's status
		Meteor.call('updatePlayer', clientId, 'instructions');
		Meteor.call('updatePlayer', partnerId, 'instructions');

		// Add Game to DB
		return Games.insert(data);
	},
	'matchPlayers': function(){
		var clientId = Meteor.userId();
		var numPlayers = Players.find({status:'waiting'},{}).count();
		// The way to extract just the id from the mongo query
		var partnerId = Players.find({}, {fields: {'_id':1}, sort:{enterTime:1},limit:1}).fetch()[0]._id;
		if (numPlayers >= 2){
			// Create a new game with each player
			return Meteor.call('createGame', clientId, partnerId);
		}
	},
	'updateGameState': function(gameId, state) {
		return Games.update(gameId, {$set: {'state':state}});
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
		if(Games.find({_id:gameId}).fetch()[0].playerAReady && Games.find({_id:gameId}).fetch()[0].playerBReady){
			Meteor.call('updateGameState',gameId, "playerBmessaging");
		}
	},
	'addMessage':function(gameId, message){
		Games.update(gameId, {$set:{'message':message}});
	}
});
