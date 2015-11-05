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
			var round = game.round;

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
				}
			return {
				state: game.state,
				pChoose: pChoose,
				pDisp: pDisp,
				mess: mess,
				gOut: gOut,
				round: round
			};
		}
	}
});

