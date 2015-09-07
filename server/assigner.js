//Function to allow easy subclassing, in coffee script this is just extend
var extend = function(child, parent) { for (var key in parent) { if (hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor(); child.__super__ = parent.prototype; return child; },
  hasProp = {}.hasOwnProperty;

//Our assigner which takes in a group size and handles making sure new users are put into a game with another person or otherwise sent to the exit survey 
TurkServer.Assigners.UGAssigner = (function(superClass) {
  extend(UGAssigner, superClass);

  function UGAssigner(groupSize){
    this.groupSize = groupSize;
    //return UGAssigner.__super__constructor.apply(this,arguments);
  }

  UGAssigner.prototype.initialize = function(){
    //Call the parent's initialize method first
    UGAssigner.__super__.initialize.apply(this, arguments);
  };

  UGAssigner.prototype.userJoined = function(asst){
    //If a user has been in an experiment before check to see if they need to be rematched, if so leave them in the lobby otherwise take them to the exit survey
    if(asst.getInstances().length > 0){
      if(!Players.findOne(asst.userId).needRematch){
        this.lobby.pluckUsers([asst.userId]);
        return asst.showExitSurvey();
      }
    } else{
      Meteor.call('addPlayer', asst.userId);
    }

    //If there are 2 people in the lobby create a new instance and send both players there
    //Also change their status in the players db and create a game
    var lobbyUsers = this.lobby.getAssignments();
    
    if (lobbyUsers.length == this.groupSize)  {
      var treatment = _.sample(this.batch.getTreatments());
      this.instance = this.batch.createInstance([treatment]);
      this.instance.setup();
      results = []; playerIds = [];
      for (i = 0, len = lobbyUsers.length; i < len; i++) {
        asst = lobbyUsers[i];
        this.lobby.pluckUsers([asst.userId]);
        Meteor.call('updatePlayerState', asst.userId,'instructions');
        results.push(this.instance.addAssignment(asst));
        playerIds.push(asst.userId);
      }
      Meteor.call('createGame', playerIds, treatment);
      return results;
    } else{
      return; //do nothing cause there's not enough players
    }
  };

  return UGAssigner;

})(TurkServer.Assigner);




