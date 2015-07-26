// Routing Info
Router.configure({
  layoutTemplate: 'main',
  loadingTemplate: 'loading'
});
Router.route('/', {
  name: 'home',
  template: 'home',
  waitOn: function(){
    return Meteor.subscribe('Players');
  }
});
Router.route('/gameTree');
Router.route('/register');
Router.route('/login');
Router.route('/lobby');
/*
Router.route('/lobby/:_id', {
    name: 'lobby',
    template: 'lobby',
    data: function(){
        var currentUser = Meteor.userId();
        return Players.findOne({ _id: currentUser});
    },
    onBeforeAction: function(){
        var currentUser = Meteor.userId();
        if(currentUser){
            this.next();
        } else {
            this.render("login");
        }
    },
    waitOn: function(){
      var currentList = this.params._id;
      return [Meteor.subscribe('lists'), Meteor.subscribe('todos', currentList)];
    }
});
*/