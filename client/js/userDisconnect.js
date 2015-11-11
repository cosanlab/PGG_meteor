//Tell user they will be rematched or sent to the submit HIT screen
Template.userDisconnect.helpers({
    gameTooEarly: function(){
        var game = Games.findOne();
        if (game.round == 1 && (game.state == 'pChoose' || game.state == 'assignment')){
            return true;
        } else {
            return false;
        }
    },
    disconnectTimeout: function (){
        return disconnectTimeout;
    },

});
