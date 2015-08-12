Template.lobby.helpers({
  
  lobby:function(){
    /* Initially queries db for waiting players. If client is matched and not in lobby, 
    then routes to instructions. Also routes to instructions if no one is in the lobby 
    (empty db query) */
    var lobbyUsers = Players.find({status:'waiting'},{sort:{enterTime:1}});
    var playerCount = lobbyUsers.count();
    return{
      players: lobbyUsers,
      count: playerCount
    };

    } 
});

Template.lobby.onRendered(function(){
  Meteor.call('matchPlayers');
});

