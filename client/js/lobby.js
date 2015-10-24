Template.lobby.helpers({
  
  rematch: function(){
    return Players.findOne(Meteor.userId()).needRematch;
  },
  
  desiredNumPlayers: function(){
    return groupSize;
  },

  numWaiting: function(){
    var currentUser = Meteor.userId();
    return Players.find({$and:[{'passedQuiz':true},{'status':'waiting'},{'_id':{$ne:currentUser}}]}).count();
  },

  clock: function(){
    Meteor.setInterval(function(){
      var sTime = Session.get('sTime');
      var elapsed = (new Date() - sTime)/1000;
      var m = Math.floor(elapsed/60);
      var s = Math.floor(elapsed % 60);
      s = s < 10 ? "0" + s: s;
      Session.set('min',m);
      Session.set('sec',s);
    },5000);

    return {
      min: Session.get('min'),
      sec: Session.get('sec')
    };
  },

  lobbyTimeout:function(){
    return lobbyTimeout;
  }

});
Template.lobby.onCreated(function(){
  Session.set('sTime',new Date());
});
