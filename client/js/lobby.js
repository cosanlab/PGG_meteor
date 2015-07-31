Template.lobby.helpers({
  
  players:function(){
    /* Initially queries db for waiting players. If client is matched and not in lobby, 
    then routes to instructions. Also routes to instructions if no one is in the lobby 
    (empty db query) */
    var currentUser = Meteor.userId();
    var lobbyUsers = Players.find({status:'waiting'},{sort:{enterTime:1}});
    console.log(jQuery.isEmptyObject(lobbyUsers.fetch()[0]));
    if(jQuery.isEmptyObject(lobbyUsers.fetch()[0]) || currentUser != lobbyUsers.fetch()[0]._id){
      Router.go('instructions');

    } else {
       return lobbyUsers;
    } 
  },

  countPlayers:function(){ 
  	return Players.find({status:'waiting'},{}).count();
  } 
});

Template.lobby.onRendered(function(){
  Meteor.call('matchPlayers');
});

