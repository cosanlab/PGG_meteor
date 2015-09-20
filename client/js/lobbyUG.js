Template.lobbyUG.helpers({
  
  rematch:function(){
    return Players.findOne(Meteor.userId()).needRematch;
  },
  
  //Scope of this is lobbyinfo
  identifier: function() {
    var user = Meteor.users.findOne(this._id);
    return(user != null ? user._id : "<i>unnamed user</i>");
  },

  desiredNumPlayers: function(){
    return 2;
  }
});