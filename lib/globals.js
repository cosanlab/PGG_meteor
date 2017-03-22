//Conditions
CONDS = ['2G','6G','2NG','6NG'];
//End game timer
endGameTimer = false;

//Alphabeter for user assignment
letters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('');

//PGG group size (should be an even number)
groupSize = 6;

//Character limit for communication
charLim = 140;

//PGG number of rounds
numRounds = 10;

//Max wait time for user matching (in minutes)
lobbyTimeout = 10;

//Max wait time for user disconnection (in minutes)
disconnectTimeout = 1;

//How much of a bonus to add if people lose their connection during a game before the first round is played
disconnectPay = 1.00;

//Change namespace of lodash to invoke like underscore.js
_ = lodash;

//Bonus payment conversion rate to be multiplied by points earned in a round
bonusPaymentConversion = 2;

//Avatars (probably a more modular way to do this)
avatars =[];
avatars[0] = '/avatars/bee.png';
avatars[1] = '/avatars/bird.png';
avatars[2] = '/avatars/cat.png';
avatars[3] = '/avatars/cow.png';
avatars[4] = '/avatars/lion.png';
avatars[5] = '/avatars/pig.png';