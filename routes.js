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
Router.route('/instructions',{
  action: function(){
    if (this.ready){
      var gameState = Games.findOne().state;
      if(gameState== 'instructions'){
        this.render('instructions');
        console.log('Game not ready');
      }
      else{
        console.log('Game ready');
        Router.go('gameTree');
      }
    }
  }
}); 
//Router.route('/instructions');
Router.route('/gameTree');
Router.route('/register');
Router.route('/login');

Router.route('/lobby',{
  waitOn: function(){
    return Meteor.subscribe('Players');
  },
  action: function(){
    if(this.ready){
      this.render();
    }
  }
}); 

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