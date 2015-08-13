Template.navigation.events({
    'click .logout': function(event){
        event.preventDefault();
        Meteor.call('removePlayer',{});
        Meteor.logout();
    }
});