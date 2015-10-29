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
                Router.go('lobby');
            }
            //When a user is in an experiment, if their partner has disconnected show them the disconnection template and start a timer that will eventually end the experiment. Otherwise, clear any experiment-ending timers (e.g. if everyone reconnects) and show them the instructions template which has it's own logic about which state of the game they should see
            else if (TurkServer.inExperiment()){
                if(Meteor.users.findOne({_id: {$ne:Meteor.userId()}}) == undefined){
                    Router.go('userDisconnect');
                } else {
                    if(endGameTimer){
                        Meteor.clearTimeout(endGameTimer);
                    } 
                    Router.go('game');
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


//New Spacebars function that should work on all templates
Template.registerHelper('Cond', function (v1, operator, v2) {
    switch (operator) {
        case '==':
            return (v1 == v2);
        case '!=':
            return (v1 != v2);
        case '<':
            return (v1 < v2);
        case '<=':
            return (v1 <= v2);
        case '>':
            return (v1 > v2);
        case '>=':
            return (v1 >= v2);
        case '&&':
            return (v1 && v2);
        case '||':
            return (v1 || v2);
    }
});