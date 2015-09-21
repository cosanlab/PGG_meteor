//Wait 5 seconds to send a user back to the lobby
Template.partnerfailedQuiz.onRendered(function(){
    var currentUser = Meteor.userId();
    var userInst = Meteor.users.findOne(currentUser).group;
    Meteor.setTimeout(function(){
        Meteor.call('goToLobby', currentUser, userInst);
    },7000);

});
    