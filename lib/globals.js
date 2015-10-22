//End game timer
endGameTimer = false;

//Alphabeter for user assignment
letters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('');

//PGG group size
groupSize = 2;

//PGG number of rounds
numRounds = 10;

//lobby timebomb function defined globally for compatibility with all browsers using anonymous function call in checkPlayerEligibility method
timebomb = function timebomb(asstId){
	Meteor.call('lobbyTimeBomb',asstId);
};