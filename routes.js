// Router configuration
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

//When users accept the HIT they're either shown the instructions (if they haven't passed the quiz) or they are show the main lobby screen
Router.route('/lobby',{
  name: 'lobby',
  template: 'lobby',
  waitOn: function(){
    return Meteor.subscribe('Players');
  },
  action: function(){
    var player = Players.findOne(Meteor.userId());
    if (player.status == 'instructions'){
      this.render('instructions');
    } else{
      this.render('lobby');
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


//Core game logic, maybe better to have templating logic in game template instead?
Router.route('/game',{
  name: 'game',
  waitOn: function(){
    return [Meteor.subscribe('Games'), Meteor.subscribe('Players')];
  },
  action: function(){
    var game = Games.findOne();
    if(game.state == 'assignment'){
      this.render('assignment');
    } else{
        this.render('game');
      }
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
      this.render('endSurvey');
  }
});

/*
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
*/

