// Routing Info
Router.configure({
  layoutTemplate: 'main',
  loadingTemplate: 'loading'
});

Router.route('/register');
Router.route('/login');

//Home template, no data requires
Router.route('/', {
  name: 'home',
  template: 'home',
});

//Lobby template, make sure we see the players db for matching
Router.route('/lobby',{
  waitOn: function(){
    return Meteor.subscribe('Players');
  },
  action: function(){
    this.render('lobby');
  }
}); 

//Instructions template, make sure we can see the games db for routing forward
//to the game
Router.route('/instructions',{
  waitOn: function(){
    return Meteor.subscribe('Games');
  },
  action: function(){
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
}); 

//Games template, make sure we can see the games db for properly rendering
//the game tree and forward routing to payoffs
Router.route('/game',{
  waitOn: function(){
    return Meteor.subscribe('Games');
  },

  action: function(){
    var game = Games.findOne();
    if (game.state== 'finalChoices'){
        setTimeout(function(){
          Router.go('payoffs');
          //Update all client statuses
          Meteor.call('playerFinished', game._id); 
        },
        5000);
                
      }
      else{
        this.render('game');
      }
  }
});

//Payoffs template, make sure we can see the games db for rendering amounts
Router.route('/payoffs',{
  waitOn: function(){
    return Meteor.subscribe('Games');
  },
  action: function(){
    this.render('payoffs');
  }
});


