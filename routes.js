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

//External submit route to end experiment template
Router.route('/mturk/externalSubmit',{
  name: 'endExperiment',
  template: 'endExperiment'
});

//Lobby template, make sure we see the players db for matching
Router.route('/lobbyUG',{
  name: 'lobbyUG',
  template: 'lobbyUG',
  waitOn: function(){
    Meteor.subscribe('Players');
  },
  action: function(){
      this.render('lobbyUG');
  },
  onBeforeAction: function() {
    if (!Meteor.user()) {
      return this.render("tsUserAccessDenied");
    } else{
      return this.next();
    }

  }
}); 

//Instructions template, that sends players to the game if the game state is ready
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
  }
});

//End survey
Router.route('/endSurvey',{

  name: 'endSurvey',

  waitOn: function(){
    //Might need to change this depending on where we store survye data
    Meteor.subscribe('Games');
  },
  action: function(){
    this.render('endSurvey');
  }
});



