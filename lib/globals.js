//End game timer
endGameTimer = false;

//Alphabeter for user assignment
letters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('');

//PGG group size
groupSize = 4;

//Character limit for communication
charLim = 140;

//PGG number of rounds
numRounds = 10;

//How much to pay people if they fail the quiz
ineligibilityPay = 0.05;

//Max wait time for user matching (in minutes)
lobbyTimeout = 2;

//How much toe pay if people don't get matched
lobbyTimeoutPay = 0.25;

//How much of a bonus to add if people lose their connection during a game
disconnectPay = 1.00;

//Change namespace of lodash to invoke like underscore.js
_ = lodash;