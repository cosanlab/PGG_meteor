//Tell user they will be rematched or sent to the submit HIT screen
Template.userDisconnect.helpers({
    gameTooEarly: function(){
        var game = Games.findOne();
        var tooEarly = _.any(_.pluck(game.players,'contributions',_.isEmpty));
        if (game.round == 1 && tooEarly){
            return true;
        } else {
            return false;
        }
    },
    disconnectPay: function(){
        return disconnectPay;
    },
    disconnectTimeout: function (){
        return disconnectTimeout;
    },
    clock: function(){
    Meteor.setInterval(function(){
      var sTime = Session.get('sTime');
      var elapsed = (new Date() - sTime)/1000;
      var m = Math.floor(elapsed/60);
      var s = Math.floor(elapsed % 60);
      s = s < 10 ? "0" + s: s;
      Session.set('min',m);
      Session.set('sec',s);
    },900);

    return {
      min: Session.get('min'),
      sec: Session.get('sec')
    };
  }
});

Template.userDisconnect.onCreated(function(){
  Session.set('sTime',new Date());
});
