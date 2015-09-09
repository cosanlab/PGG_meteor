// Routing Info
Router.configure({
  layoutTemplate: 'main',
  loadingTemplate: 'loading'
});

//Router.route('/register');
//Router.route('/login');

//Home template, no data required
Router.route('/', {
  name: 'home',
  template: 'home',
});

//Disconnect template
Router.route('/userDisconnect',{
  name: 'userDisconnect',
  template: 'userDisconnect',
  waitOn: function(){
return [Meteor.subscribe('Games'), Meteor.subscribe('userStatus')];
  },
  action:function(){
    this.render('userDisconnect');
    Meteor.call('updatePlayerState', Meteor.userId(), 'partnerDisconnect');
    var game = Games.findOne();
    var currentUser = Meteor.userId();
    var rematch;
    endGameTimer = Meteor.setTimeout(function(){
      if(game.state == 'instructions'){
        rematch = true;    
      } else {
        rematch = false;
      }
      Meteor.call('partnerDisconnected',rematch, currentUser, game._id);
    },10000);
    this.next();
  }
});

//Handles which version of lobby players should see based on rematch status
Router.route('/lobbyUG',{
  name: 'lobbyUG',
  template: 'lobbyUG',
  waitOn: function(){
    return Meteor.subscribe('Players');
  },
  action: function(){
    if(Players.findOne(Meteor.userId()).needRematch){
      this.render('altLobbyUG');
    } else{
      this.render('lobbyUG');
    }
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
    return [Meteor.subscribe('Games'), Meteor.subscribe('Players')];
  },
  action: function(){
      var gameState = Games.findOne().state;
      if(gameState== 'instructions'){
        this.render('instructionsInteractive');
      }
      else if(gameState == 'assignment'){
        this.render('assignmentInteractive');
        Meteor.call('updatePlayerState', Meteor.userId(), 'roleAssignment');
      }
      else if(gameState == 'playerBmessaging' || gameState == 'playerAdeciding' || gameState == 'playerBdeciding'){
        console.log('Game ready');
        Router.go('game');
        //Each client updates their own status
        Meteor.call('updatePlayerState', Meteor.userId(),'playing');
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
  template: 'endSurvey',
  
  waitOn: function(){
    return Meteor.subscribe('Players');
  },
  action: function(){
    var playerStatus = Players.findOne(Meteor.userId()).status;
    if(playerStatus=='failedQuiz'){
      this.render('endSurveyFailedQuiz');
    } else if(playerStatus == 'partnerDisconnect'){
      this.render('endSurveyDisconnect');
    }
    else{
      this.render('endSurvey');
    }
  }


});



