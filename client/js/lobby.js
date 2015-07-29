Template.lobby.helpers({
  players:function(){
    return Players.find({status:'waiting'},{sort:{enterTime:-1}});
  },
  countPlayers:function(){ 
  	return Players.find({status:'waiting'},{}).count();
  },
  //Return the status of the current client
  
  status:function(){
    var currentUser = Meteor.userId();
    return Players.find({"_id" :currentUser}, {fields:{"status":1}});
    //return Players.find({"_id" :currentUser}).fetch()[0].status;
  }
    

    /*if (currentUserStatus === 'waiting'){
      return true;
    } else if (typeof currentUserStatus === 'undefined'){
      return true;
    } else{
      return false;
    }
  } */
});

Template.lobby.onRendered(function(){
  Meteor.call('matchPlayers');
});

