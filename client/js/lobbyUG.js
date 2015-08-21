Template.lobbyUG.helpers({
  
  lobby:function(){
    var lobby = LobbyStatus.find();
    return{
      count: lobby.count(),
      info: lobby
    };
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