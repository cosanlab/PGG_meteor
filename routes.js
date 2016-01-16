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
    return [Meteor.subscribe('Batches'), Meteor.subscribe('Players')];
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


//When users get matched they should enter the game. If the game hasn't started yet this should be a refresher template (assignment). If it has started it should be the game template (which has the core logic). If a user disconnects it should respond reactively (userDisconnect template).
Router.route('/game',{
  name: 'game',
  waitOn: function(){
    return [Meteor.subscribe('Games'), Meteor.subscribe('Players')];
  },
  action: function(){
    var game = Games.findOne();
    if(game.state == 'assignment'){
      this.render('assignment');
    } else if(game.state == 'lostUser'){
      this.render('userDisconnect');
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