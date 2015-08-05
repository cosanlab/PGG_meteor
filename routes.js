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
        Router.go('game');
        //Each client updates their own status
        Meteor.call('updatePlayer', Meteor.userId(),'playing');
      }
    }
  }
}); 
//Router.route('/instructions');
Router.route('/game');
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
