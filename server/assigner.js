//Function to allow easy subclassing, in coffee script this is just extend
var extend = function(child, parent) { for (var key in parent) { if (hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor(); child.__super__ = parent.prototype; return child; },
  hasProp = {}.hasOwnProperty;

//Our assigner which takes in a group size and handles making sure new users are put into a game with another person or otherwise sent to the exit survey 
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
  };

  PGGAssigner.prototype.userJoined = function(asst){
    //If a user has been in an experiment before check to see if they need to be rematched, if so leave them in the lobby otherwise take them to the exit survey
    //When leaving users in the lobby also start and store a 5 minute lobby timers
    var asstId = asst.userId;
    if(asst.getInstances().length > 0){
      if(!Players.findOne(asstId).needRematch){
        this.lobby.pluckUsers([asstId]);
        return asst.showExitSurvey();
      }
    } else if(!Players.findOne(asstId)){
        Meteor.call('addPlayer', asstId);
        var userlobbyBomb = Meteor.setTimeout(function(){
          Meteor.call('lobbyTimeBomb',asstId);
    },300000);
        this.lobbyTimers.push({asstId:userLobbyBomb});

    }

    //If there are 5 people in the lobby create a new instance and send both players there
    //Also change their status in the players db and create a game
    //Also clear their lobby timers 
    var lobbyUsers = this.lobby.getAssignments();
    
    if (lobbyUsers.length == this.groupSize)  {
      this.instance = this.batch.createInstance([this.treatment]);
      this.instance.setup();
      results = []; playerIds = [];
      for (i = 0, len = lobbyUsers.length; i < len; i++) {
        asst = lobbyUsers[i];
        Meteor.clearTimeout(this.lobbyTimers[i].asst.userId);
        this.lobby.pluckUsers([asst.userId]);
        Meteor.call('updatePlayerState', asst.userId,'instructions');
        results.push(this.instance.addAssignment(asst));
        playerIds.push(asst.userId);
      }
      Meteor.call('createGame',this.instance._id, playerIds, treatment);
      return results;
    } else{
      return; //do nothing cause there's not enough players
    }
  };

  return PGGAssigner;

})(TurkServer.Assigner);




