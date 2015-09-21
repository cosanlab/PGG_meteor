//Setup up a reactive computation that monitors when clients' states changes
Meteor.startup(function(){
    Meteor.defer(function(){
        Tracker.autorun(function(){
            //When a user is in the lobby clear any experiment-ending timers, make sure they have access to the lobby db, and show them the lobby template
            if(TurkServer.inLobby()){
                if(endGameTimer){
                        Meteor.clearTimeout(endGameTimer);
                    }
                var batch = TurkServer.batch();
                Meteor.subscribe('lobby', batch && batch._id);
                Router.go('lobbyUG');
            }
            //When a user is in an experiment, if their partner has disconnected show them the disconnection template and start a timer that will eventually end the experiment. Otherwise, clear any experiment-ending timers (e.g. if everyone reconnects) and show them the instructions template which has it's own logic about which state of the game they should see
            else if (TurkServer.inExperiment()){
                if(Meteor.users.findOne({_id: {$ne:Meteor.userId()}}) == undefined){
                    Router.go('userDisconnect');
                } else {
                    if(endGameTimer){
                        Meteor.clearTimeout(endGameTimer);
                    } 
                    Router.go('instructionsInteractive');
                }
            } 
            //If a user is in the exit survey clear any experiment-ending times and render the end survey template
            else if (TurkServer.inExitSurvey()){
                if(endGameTimer){
                        Meteor.clearTimeout(endGameTimer);
                    }
                Router.go('endSurvey');
            }
        });
    });
});

//Reactively subscribe to the partitioned databases\

Tracker.autorun(function(){
    var group = TurkServer.group();
    if (group == null){
        return;
    }
    Meteor.subscribe('Players');
    Meteor.subscribe('Games',group);
    Meteor.subscribe('userStatus');
});


/*
//When a player joins a game start a reactive computation monitoring their partner's connection status
if(Players.findOne(Meteor.userId()).status != 'waiting'){
    Tracker.autorun(function(){


    });

}
//Keep tabs on the other user being played with

Tracker.autorun(function(){
    var group = Turkserver.group();
});
*/

//New Spacebars function that should work on all templates
Template.registerHelper("equals", function(a,b){
    return (a==b);
});
Template.registerHelper("notequals", function(a,b){
    return (a!=b);
});

// Login validation
$.validator.setDefaults({
    rules: {
        email: {
            required: true,
            email: true
        },
        password: {
            required: true,
            minlength: 6
        }
    },
    messages: {
        email: {
            required: "You must enter an email address.",
            email: "You've entered an invalid email address."
        },
        password: {
            required: "You must enter a password.",
            minlength: "Your password must be at least {0} characters."
        }
    }
});

/*
//Registration handler
Template.register.onRendered(function(){
    var validator = $('.register').validate({
        submitHandler: function(event){
            var email = $('[name=email]').val();
            var password = $('[name=password]').val();
            Accounts.createUser({
                email: email,
                password: password
            }, function(error){
                if(error){
                    if(error.reason == "Email already exists."){
                        validator.showErrors({
                            email: "That email already belongs to a registered user."   
                        });
                    }
                } else {
                     Meteor.call('addPlayer', {}, function(){
                        Router.go('lobby');
                });
                }   
            });
        }
    });
});

*/
//Login handler
/*
Template.login.onRendered(function(){
    var validator = $('.login').validate({
        submitHandler: function(event){
            var email = $('[name=email]').val();
            var password = $('[name=password]').val();
            Meteor.loginWithPassword(email, password, function(error){
              if(error){
                  if(error.reason == "User not found"){
                      validator.showErrors({
                        email: "That email doesn't belong to a registered user."   
                      });
                  }
                  if(error.reason == "Incorrect password"){
                      validator.showErrors({
                        password: "You entered an incorrect password."    
                      });
                  }
              } else {
                Meteor.call('addPlayer', {}, function(){
                    Router.go('lobby');
                });
              }
            });
        }
    });
}); */
/*
Template.navigation.events({
    'click .logout': function(event){
        event.preventDefault();
        Meteor.logout();
        Router.go('login');
    }
});
*/