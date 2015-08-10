Template.progress.helpers({
	game: function(){
		var game= Games.findOne();
		return {
			state: game.state,
			condition: game.condition
		};
	}
});