Template.progress.helpers({
	game: function(){
		var game= Games.findOne();
		if(!game){
			return false;
		} else{ 
			var pChoose = "";
			var pDisp = "";
			var mess = "";
			var gOut = "";
			var gameEnd = "";
			var round = game.round;
			var displayEnd = false;

			switch(game.state){
				case 'pChoose':
					pChoose = 'active';
					break;
				case 'pDisp':
					pDisp = 'active';
					break;
				case 'pSendMess1':
					mess = 'active';
					break;
				case 'pReceiveMess1':
					mess = 'active';
					break;
				case 'pSendMess2':
					mess = 'active';
					break;
				case 'pReceiveMess2':
					mess = 'active';
					break;
				case 'gOut':
					gOut = 'active';
					round = game.round -1;
					break;
				case 'playerRatings':
					round = game.round -1;
					displayEnd = true;
					gameEnd = 'active';
					break;
				case 'finalOut':
					round = game.round -1;
					displayEnd = true;
					gameEnd = 'active';
				}
			return {
				state: game.state,
				pChoose: pChoose,
				pDisp: pDisp,
				mess: mess,
				gOut: gOut,
				round: round,
				displayEnd: displayEnd,
				gameEnd: gameEnd
			};
		}
	}
});

