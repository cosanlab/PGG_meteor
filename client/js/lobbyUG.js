Template.lobby.helpers({
  
  lobby:function(){
    var lobby = LobbyStatus.find();
    return{
      count: lobby.count(),
      info: lobby
    };
  },
  //Scope of this is lobbyinfo
  identifier: function() {
    var ref3;
    return ((ref3 = Meteor.users.findOne(this._id)) != null ? ref3.username : void 0) || "<i>unnamed user</i>";
  }
});