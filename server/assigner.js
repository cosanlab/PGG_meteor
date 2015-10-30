//Function to allow easy subclassing, in coffee script this is just extend
var extend = function(child, parent) { for (var key in parent) { if (hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor(); child.__super__ = parent.prototype; return child; },
  hasProp = {}.hasOwnProperty;

TurkServer.Assigners.PGGAssigner = (function(superClass) {
  extend(PGGAssigner, superClass);

  function PGGAssigner(groupSize){
    this.groupSize = groupSize;
    this.lobbyTimers = [];
    return PGGAssigner.__super__.constructor.apply(this, arguments);
  }

  PGGAssigner.prototype.initialize = function(){
    //Call the parent's initialize method first
    PGGAssigner.__super__.initialize.apply(this, arguments);
    //Player matching occurs by a lobby event triggered by a client after they pass the comprehension quiz
    //This event puts users into a game with other users who have passed the quiz and clears their lobby timebombs, otherwise it does nothing
    this.lobby.events.on("match-players", (function(_this){
      return function() {
        var lobbyUsers = _this.lobby.getAssignments({'passedQuiz':true});
        if (lobbyUsers.length == _this.groupSize)  {
          var treatment = _.sample(_this.batch.getTreatments());
          _this.instance = _this.batch.createInstance([treatment]);
          _this.instance.setup();
          results = []; playerIds = [];
          for (i = 0, len = lobbyUsers.length; i < len; i++) {
            asst = lobbyUsers[i];
            Meteor.clearTimeout(_this.lobbyTimers[i][asst.userId]);
            _this.lobby.pluckUsers([asst.userId]);
            Meteor.call('updatePlayerInfo', asst.userId,{'status': 'matched'},'set');
            results.push(_this.instance.addAssignment(asst));
            playerIds.push(asst.userId);
          }
          Meteor.call('createGame', _this.instance.groupId, playerIds, treatment);
          console.log('ASSIGNER: Match the players! New game created: '+ _this.instance.groupId);
          return results;
        } else {
          console.log("ASSIGNER: Match the players! There aren't enough!");
        }
      };
    })(this)); 
  };

  PGGAssigner.prototype.userJoined = function(asst){
    //If a user has been in an experiment before check to see if they need to be rematched, if so leave them in the lobby otherwise take them to the exit survey
    
    var currentUser = asst.userId; //Same as Meteor.userId
    var workerId = asst.workerId; //Mturk Id displayed in admin
    if(asst.getInstances().length > 0){
      if(Players.findOne(currentUser).needRematch){
        this.lobby.pluckUsers([currentUser]);
        return asst.showExitSurvey();
      }
    } else if(!Players.findOne(currentUser)){
        Meteor.call('addPlayer', currentUser, workerId);
        
    }
  };

  return PGGAssigner;

})(TurkServer.Assigner);




