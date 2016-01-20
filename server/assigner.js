//Function to allow easy subclassing, in coffee script this is just extend
var extend = function(child, parent) { for (var key in parent) { if (hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor(); child.__super__ = parent.prototype; return child; },
  hasProp = {}.hasOwnProperty;

TurkServer.Assigners.PGGAssigner = (function(superClass) {
  extend(PGGAssigner, superClass);

  function PGGAssigner(groupSize){
    this.groupSize = groupSize;
    this.lobbyTimers = {};
    this.disconnectTimers = {};
    return PGGAssigner.__super__.constructor.apply(this, arguments);
  }

  PGGAssigner.prototype.initialize = function(){
    //Call the parent's initialize method first
    PGGAssigner.__super__.initialize.apply(this, arguments);
    //Player matching occurs by a lobby event triggered by a client after they pass the comprehension quiz (or rejoin the lobby after passing the quiz, disconnecting and reconnecting)
    //This event puts users into a game with other users who have passed the quiz and clears their lobby timebombs, otherwise it does nothing
    this.lobby.events.on("match-players", (function(_this){
      return function() {
        var connectedUsers = _this.lobby.getAssignments();
        var lobbyUsers = [];
        var player;
        _.each(connectedUsers, function(usr){
            player = Players.findOne(usr.userId);
            if(player.passedQuiz && player.status == 'waiting'){
              lobbyUsers.push(usr);
            }
          });
        if (lobbyUsers.length == _this.groupSize)  {
          var treatment = _.sample(_this.batch.getTreatments());
          _this.instance = _this.batch.createInstance([treatment]);
          _this.instance.setup();
          results = []; playerIds = [];
          for (i = 0, len = lobbyUsers.length; i < len; i++) {
            asst = lobbyUsers[i];
            Meteor.clearTimeout(_this.lobbyTimers[asst.userId]);
            _this.lobby.pluckUsers([asst.userId]);
            Meteor.call('updatePlayerInfo', asst.userId,{'status': 'matched'},'set');
            results.push(_this.instance.addAssignment(asst));
            playerIds.push(asst.userId);
          }
          //Clear the of the users that just joined a game to free memory
          _this.lobbyTimers = _.omit(_this.lobbyTimers,_.pluck(lobbyUsers,'userId'));
          Meteor.call('createGame', _this.instance.groupId, playerIds, treatment);
          console.log('ASSIGNER: Match the players! New game created: '+ _this.instance.groupId +'\n');
          return results;
        } else {
          console.log("ASSIGNER: Match the players! There are only " + lobbyUsers.length + "/" + groupSize + " players!\n");
        }
      };
    })(this)); 
  };

  PGGAssigner.prototype.userJoined = function(asst){
    /*Accounts for 3 possible ways a user ends up in the lobby
    1) They've been part of an experiment instance before = send them to the exit survey
    2) They've just accepted the HIT (they're not in the Players db) = add them to the players db and keep them in the lobby
    3) They're reconnecting after accepting the HIT but have not started a game or have not passed the quiz = leave them in the lobby and console log the new wait count, or do nothing
  */
    var currentUser = Players.findOne(asst.userId);
    if(asst.getInstances().length > 0){
      if(!currentUser.needRematch){
        this.lobby.pluckUsers([asst.userId]);
        return asst.showExitSurvey();
      }
    } 
    else{
        if(!currentUser){
          Meteor.call('addPlayer', asst.userId, asst.workerId);  
        } else if(currentUser.passedQuiz && currentUser.status == 'waiting'){
            console.log('TURKER: '+ Date() + ': ' + asst.workerId + ' rejoined the queue!\n');
            var batch = TurkServer.Batch.getBatch(this.lobby.batchId);
            var emitter = batch.lobby.events;
            emitter.emit('match-players');
        } else{
          //We'll only get here if they're reconnecting to the instructions
        }
    } 
  };

  PGGAssigner.prototype.userLeft = function(asst){
    var currentUser = Players.findOne(asst.userId);
    if(currentUser.passedQuiz){
      var connectedUsers = this.lobby.getAssignments();
      var lobbyUsers = [];
          _.each(connectedUsers, function(usr){
              if(Players.findOne(usr.userId).passedQuiz){
                lobbyUsers.push(usr);
              }
            });
      console.log('TURKER: '+ Date() + ': ' + asst.workerId + ' left the queue!\n');
      console.log("ASSIGNER: Now there are only " + lobbyUsers.length + "/" + groupSize + " players!\n");
    }
  };

  return PGGAssigner;

})(TurkServer.Assigner);




