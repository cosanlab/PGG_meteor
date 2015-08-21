Template.navigation.events({
    'click .logout': function(event){
        event.preventDefault();
        var currentUser = Meteor.userId();
        Meteor.call('removePlayer',currentUser);
        Meteor.logout();
    }
});