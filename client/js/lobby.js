Template.lobby.helpers({
  players:function(){
    return Players.find({status:"waiting"},{sort:{enterTime:-1}});
  },
  countPlayers:function(){ 
  	return Players.find({status:"waiting"},{}).count();
  }
});

Template.lobby.onRendered(function(){
  Meteor.call('matchPlayers');
});

