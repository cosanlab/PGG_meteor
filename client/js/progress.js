Template.progress.helpers({
	game: function(){
		var game= Games.findOne();
		if(!game){
			return false;
		} else{ 
			var mess = "";
			var pA = "";
			var pB = "";
			var over = ""; 
			var summ = "";

			switch(game.state){
				case 'playerBmessaging':
					mess = 'active';
					break;
				case 'playerAdeciding':
					pA = 'active';
					break;
				case 'playerBdeciding':
					pB = 'active';
					break;
				case 'finalChoices':
					over = 'active';
					break;
				case 'ended':
					summ = 'active';
					break;
				}
			return {
				state: game.state,
				condition: game.condition,
				mess: mess,
				pA: pA,
				pB: pB,
				over: over,
				summ: summ
			};
		}
	}
});

