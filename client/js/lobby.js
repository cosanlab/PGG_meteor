Template.lobby.helpers({
  players:function(){
    return Players.find({status:"waiting"},{sort:{enter_time:-1}});
  }
});