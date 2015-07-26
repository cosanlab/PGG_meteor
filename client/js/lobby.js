/*Template.lobby.onCreated(function() {
	var self=this;
	self.autorun(function(){
		self.subscribe("Players")
	});
});
*/

Template.lobby.helpers({
  players:function(){
  	return Players.find({status:"waiting"},{sort:{enterTime:-1}});
  },
  countPlayers:function(){
  	return Players.find({status:"waiting"},{}).count();
  }
});