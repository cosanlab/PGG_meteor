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
Router.route('/lobbyUG',{
  name: 'lobbyUG',
  template: 'lobby',
  waitOn: function(){
    return Meteor.subscribe('Players');
  },
  action: function(){
    var currentUser = Meteor.userId();
    if(Players.findOne({_id:currentUser}).status == 'instructions'){
      Router.go('instructionsInteractive');
    } else{
      this.render('lobby');
    }
  }
}); 

//Instructions template, make sure we can see the games db for routing forward
//to the game
/*
Router.route('/instructions',{
  waitOn: function(){
    return Meteor.subscribe('Games');
  },
  action: function(){
      var gameState = Games.findOne().state;
      if(gameState== 'instructions'){
        this.render('instructions');
      }
      else{
        Router.go('game');
        //Each client updates their own status
        Meteor.call('updatePlayer', Meteor.userId(),'playing');
      }
  }
}); */

//Instructions template, make sure we can see the games db for routing forward
//to the game

Router.route('/instructionsInteractive',{
  waitOn: function(){
    return Meteor.subscribe('Games');
  },
  action: function(){
      var gameState = Games.findOne().state;
      if(gameState== 'instructions'){
        this.render('instructionsInteractive');
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
        this.next();
                
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
    this.next();
    var gameId = Games.findOne()._id;
    setTimeout(function(){
          //Send all clients to end survey and calculate bonuses
          Meteor.call('goToEndSurvey', gameId);
        },  
        5000);
  }
});

//End survey
Router.route('/endSurvey',{
  waitOn: function(){
    //Might need to change this depending on where we store survye data
    Meteor.subscribe('Players');
  },
  action: function(){
    this.render('endSurvey');
  }
});


