// Routing Info
Router.configure({
  layoutTemplate: 'main',
  loadingTemplate: 'loading'
});

//Loading template hook
Router.onBeforeAction('loading');
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
    return Meteor.subscribe('Games');
  },
  action:function(){
      this.render('userDisconnect');
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
    return [Meteor.subscribe('Games'), Meteor.subscribe('Players')];
  },
  action: function(){
      var gameState = Games.findOne().state;
      var currentUser = Meteor.userId();
      var needRematch = Players.findOne(currentUser).needRematch;
      if(gameState== 'instructions'){
        this.render('instructionsInteractive');
      }
      else if(gameState == 'assignment'){
        this.render('assignmentInteractive');
        Meteor.call('updatePlayerState', currentUser, 'roleAssignment');
      }
      else if(needRematch){
        Router.go('failedQuiz');
      }
      else if(gameState == 'playerBmessaging' || gameState == 'playerAdeciding' || gameState == 'playerBdeciding' || gameState == 'beliefs'){
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
    if (game.state == 'beliefs'){
      this.render('beliefsPopup');
    }
    else if (game.state== 'finalChoices'){
        setTimeout(function(){
          Router.go('payoffs'); 
        },5000);
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

//If partner fails quiz
Router.route('/failedQuiz',{
  name: 'failedQuiz',
  template: 'partnerfailedQuiz'
});

//End survey
Router.route('/endSurvey',{
  name: 'endSurvey',
  template: 'endSurvey',
  
  waitOn: function(){
    return Meteor.subscribe('Players');
  },
  action: function(){
      this.render('endSurvey');
  }
});