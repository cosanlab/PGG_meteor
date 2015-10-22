Template.lobby.helpers({
  
  rematch:function(){
    return Players.findOne(Meteor.userId()).needRematch;
  },
  
  desiredNumPlayers: function(){
    return groupSize;
  },

  numWaiting: function(){
    return Players.find({},{'passedQuiz':true}).count();
  }
});