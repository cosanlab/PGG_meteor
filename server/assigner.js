//Function to allow easy subclassing, in coffee script this is just extend
var extend = function(child, parent) { for (var key in parent) { if (hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor(); child.__super__ = parent.prototype; return child; },
  hasProp = {}.hasOwnProperty;

TurkServer.Assigners.PGGAssigner = (function(superClass) {
  extend(PGGAssigner, superClass);

  function PGGAssigner(groupSize,treatment){
    this.groupSize = groupSize;
    this.treatment = treatment;
    this.lobbyTimers = [];
  }

  PGGAssigner.prototype.initialize = function(){
    //Call the parent's initialize method first
    PGGAssigner.__super__.initialize.apply(this, arguments);
    //Player matching occurs by a lobby event triggered by a client after they pass the comprehension quiz
    //This event puts users into a game with other users who have passed the quiz and clears their lobby timebombs, otherwise it does nothing
    this.lobby.events.on("match-players", (function(_this){
      return function() {
        var lobbyUsers = this.lobby.getAssignments({'passedQuiz':true});
        if (lobbyUsers.length == this.groupSize)  {
          this.instance = this.batch.createInstance([this.treatment]);
          this.instance.setup();
          results = []; playerIds = [];
          for (i = 0, len = lobbyUsers.length; i < len; i++) {
            asst = lobbyUsers[i];
            Meteor.clearTimeout(this.lobbyTimers[i].asst.userId);
            this.lobby.pluckUsers([asst.userId]);
            Meteor.call('updatePlayerInfo', asst.userId,{'status': 'playing'});
            results.push(this.instance.addAssignment(asst));
            playerIds.push(asst.userId);
          }
          Meteor.call('createGame',this.instance._id, playerIds, treatment);
          console.log('Lobby event triggered and new game successfully started!');
          return results;
        } else {
          console.log('Lobby event triggered but not enough users!');
        }
      };
    })(this));
  };

  PGGAssigner.prototype.userJoined = function(asst){
    //If a user has been in an experiment before check to see if they need to be rematched, if so leave them in the lobby otherwise take them to the exit survey
    var asstId = asst.userId;
    if(asst.getInstances().length > 0){
      if(Players.findOne(asstId).needRematch){
        this.lobby.pluckUsers([asstId]);
        return asst.showExitSurvey();
      }
    } else if(!Players.findOne(asstId)){
        Meteor.call('addPlayer', asstId);
        
    }
  };

  return PGGAssigner;

})(TurkServer.Assigner);




