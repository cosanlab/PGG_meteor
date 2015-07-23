//Configuring the iron router avoiding rendering all routes without control
Router.configure({
	autoRender: false
});

//Mapping all routes
Router.map(function(){
	//Defining the root route listing all games and allowing users to create new ones
	this.route('home', {
		path: '/',		
		data: { 
			games: Games.find({started: false, ended: false}),
			cgames: Games.find({started: true , ended: false})
		}
	});
	//The game room route, gets the id as a parameter for the game to show
	this.route('game', {
		path: '/game/:_id',		
		data: function() {
			return Games.findOne({_id: this.params._id});
		}
	});
});
