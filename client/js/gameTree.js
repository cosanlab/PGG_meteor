//D3 SVG element rendering
Template.gameTree.rendered = function () {

	var svg = d3.select('#tree')
	.append("div")
	.classed("svg-container", true) //container class to make it responsive
	.append("svg")
	//responsive SVG needs these 2 attributes and no width and height attr
	.attr("preserveAspectRatio", "xMinYMin meet")
	.attr("viewBox", "0 0 800 800")
	//class to make it responsive
	.classed("svg-content-responsive", true);    

	var playerALeftData = [[{ x: 400,   y: 100},  { x: 300,  y: 200}]];
	var playerARightData = [[{ x: 400,   y: 100},  { x: 500,  y: 200}]];
	var playerBLeftData = [[{ x: 500,   y: 240},  { x: 400,  y: 340}]];
	var playerBRightData = [[{ x: 500,   y: 240},  { x: 600,  y: 340}]];

	//Coordinate based line interpolation function	
	var line = d3.svg.line()
		.x(function(d) { return d.x; })
		.y(function(d) { return d.y; })
		.interpolate("linear");

	//Player A group
	var playerA = svg.append("g");

	//Player A title
	playerA.append("text")	
		.attr("x",400)
		.attr("y",90)
		.text("A")
		.attr("font-size",30)
		.attr("text-anchor","middle")
		.attr("font-weight" ,"bold");

	//Left part of Player A's decision tree
	var playerALeft = playerA.append("g");
		playerALeft.attr("class","playerALeft");

	//Line aesthetic defaults in css file	
	var playerALeftLine = playerALeft.selectAll("line")
		.data(playerALeftData)
		.enter()
		.append("path")
		.attr("d",line);

	//Text aesthetic defaults defined here, override CSS
	playerALeft.append("text")	
		.attr("x",300)
		.attr("y",230)
		.text("A gets: $1")
		.attr("font-size",28)
		.attr("text-anchor","middle")
		.attr("stroke-width",1);

	playerALeft.append("text")	
		.attr("x",300)
		.attr("y",260)
		.text("B gets: $3")
		.attr("font-size",28)
		.attr("text-anchor","middle")
		.attr("stroke-width",1);

	//Right part of Player A's decision tree
	var playerARight = playerA.append("g");
		playerARight.attr("class","playerARight");

	var playerARightLine = playerARight.selectAll("line")
		.data(playerARightData)
		.enter()
		.append("path")
		.attr("d",line);

	//Player B title
	playerARight.append("text")
		.attr("x",500)
		.attr("y",230)
		.text("B")
		.attr("font-size",30)
		.attr("text-anchor","middle")
		.attr("font-weight","bold")
		.attr("stroke-width",1);	

	//Player B group
	var playerB = svg.append("g");	

	//Left part of Player B's decision tree
	var playerBLeft = playerB.append("g");
		playerBLeft.attr("class","playerBLeft");

	var playerBLeftLine = playerBLeft.selectAll("line")
		.data(playerBLeftData)
		.enter()
		.append("path")
		.attr("d",line);

	playerBLeft.append("text")
		.attr("x",400)
		.attr("y",370)
		.text("A gets: $0")
		.attr("font-size",28)
		.attr("text-anchor","middle")
		.attr("stroke-width",1);


	playerBLeft.append("text")	
		.attr("x",400)
		.attr("y",400)
		.text("B gets: $0")
		.attr("font-size",28)
		.attr("text-anchor","middle")
		.attr("stroke-width",1);

	//Right part of Player B's decision tree
	var playerBRight = playerB.append("g");
		playerBRight.attr("class","playerBRight");

	var playerBRightLine = playerBRight.selectAll("line")
		.data(playerBRightData)
		.enter()
		.append("path")
		.attr("d",line);

	playerBRight.append("text")	
		.attr("x",600)
		.attr("y",370)
		.text("A gets: $2")
		.attr("font-size",28)
		.attr("text-anchor","middle")
		.attr("stroke-width",1);

	playerBRight.append("text")	
		.attr("x",600)
		.attr("y",400)
		.text("B gets: $2")
		.attr("font-size",28)
		.attr("text-anchor","middle")
		.attr("stroke-width",1);

};

//Click event handlers for game tree
Template.gameTree.events({
	'click .playerALeft': function(event){
		event.preventDefault();
		console.log("Player A chose Left!");
	},
	'click .playerARight': function(event){
		event.preventDefault();
		console.log("Player A chose Right!");
	},
	'click .playerBLeft': function(event){
		event.preventDefault();
		console.log("Player B chose Left!");
	},
	'click .playerBRight': function(event){
		event.preventDefault();
		console.log("Player B chose Right!");
	}
});
