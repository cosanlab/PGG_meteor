Template.lobby.helpers({
  
  rematch: function(){
    return Players.findOne(Meteor.userId()).needRematch;
  },
  
  desiredNumPlayers: function(){
    return groupSize;
  },

  numWaiting: function(){
    var connectedUsers = LobbyStatus.find().fetch();
    var lobbyUsers = [];
    var player;
    _.each(connectedUsers, function(usr){
      player = Players.findOne(usr._id);
      // Guard against empty queries
      if (player){ 
        if(player.passedQuiz && player.status=='waiting'){
          lobbyUsers.push(usr);
        }
      }
    });
    return lobbyUsers.length;
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
    },900);

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
