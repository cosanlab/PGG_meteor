//Start the disconnect timer once when the page first renders 
Template.userDisconnect.onRendered(function(){
	var game = Games.findOne();
    var currentUser = Meteor.userId();
    var userInst = Meteor.users.findOne({_id:currentUser}).group;
    console.log(userInst);
    var rematch;
    if(game.state == 'instructions'){
        rematch = true;    
    } else {
        rematch = false;
    }
    //After a delay end the experiment either rematching the user or not
    endGameTimer = Meteor.setTimeout(function(){
      Meteor.call('partnerDisconnected',rematch, currentUser, userInst, game._id);
    },10000);
});

//Tell user they will be rematched or sent to the submit HIT screen
Template.userDisconnect.helpers({
	disconnectText: function(){
		var gameState = Games.findOne();
		if(gameState == 'instructions'){
      		return 'Otherwise we will try to rematch you with another player.';
	    } else {
      		return 'Otherwise you will be able to submit this HIT for full payment.';
    	}
	}
});