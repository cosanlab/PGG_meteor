Template.instructions.events({

  'click .ready-to-start': function(event){
    var gameId = Games.find().fetch()[0]._id;
    Meteor.call('playerReady',gameId);

  }

});
