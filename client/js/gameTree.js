// requires d3
Template.gameTree.rendered = function () {
		var svg, width = 800, height = 800;

		svg = d3.select('#tree').append('svg')
			.attr('width', width)
			.attr('height',height);

		var playerALeftData = [[{ x: 400,   y: 100},  { x: 300,  y: 200}]];
		var playerARightData = [[{ x: 400,   y: 100},  { x: 500,  y: 200}]];
		var playerBLeftData = [[{ x: 500,   y: 240},  { x: 400,  y: 340}]];
		var playerBRightData = [[{ x: 500,   y: 240},  { x: 600,  y: 340}]];

		var line = d3.svg.line()
		    .x(function(d) { return d.x; })
		    .y(function(d) { return d.y; })
		    .interpolate("linear");

		var playerA = svg.append("g");
		
		playerA.append("text")	
				.attr("x",400)
				.attr("y",90)
				.text("A")
				.attr("font-size",30)
				.attr("text-anchor","middle")
				.attr("font-weight" ,"bold");

		var playerALeft = playerA.append("g");

		var playerALeftLine = playerALeft.selectAll("line")
			.data(playerALeftData)
			.enter()
			.append("path")
			.attr("d",line)
			.attr("stroke-width",3)
			.attr("stroke","black")
			.on("mouseover", function(d) {
  				d3.select(this)
  					.attr("stroke-width",5)
					.attr("stroke", "red");
			})
			.on("mouseout", function(d) {
				d3.select(this)
					.attr("stroke-width",3)
					.attr("stroke", "black");
			});

		playerALeft.append("text")	
				.attr("x",300)
				.attr("y",230)
				.text("A gets: $1")
				.attr("font-size",28)
				.attr("text-anchor","middle");

		playerALeft.append("text")	
				.attr("x",300)
				.attr("y",260)
				.text("B gets: $3")
				.attr("font-size",28)
				.attr("text-anchor","middle");

		var playerARight = playerA.append("g");

		var playerARightLine = playerA.selectAll("line")
			.data(playerARightData)
			.enter()
			.append("path")
			.attr("d",line)
			.attr("stroke-width",3)
			.attr("stroke","black")
			.on("mouseover", function(d) {
  				d3.select(this)
  					.attr("stroke-width",5)
					.attr("stroke", "red");
			})
			.on("mouseout", function(d) {
				d3.select(this)
					.attr("stroke-width",3)
					.attr("stroke", "black");
			});

		var playerB = playerARight.append("g");	

		playerB.append("text")	
			.attr("x",500)
			.attr("y",230)
			.text("B")
			.attr("font-size",30)
			.attr("text-anchor","middle")
			.attr("font-weight","bold");	

		var playerBLeft = playerB.append("g");

		var playerBLeftLine = playerBLeft.selectAll("line")
			.data(playerBLeftData)
			.enter()
			.append("path")
			.attr("d",line)
			.attr("stroke-width",3)
			.attr("stroke","black")
			.on("mouseover", function(d) {
  				d3.select(this)
  					.attr("stroke-width",5)
					.attr("stroke", "red");
			})
			.on("mouseout", function(d) {
				d3.select(this)
					.attr("stroke-width",3)
					.attr("stroke", "black");
			});

		playerBLeft.append("text")	
				.attr("x",400)
				.attr("y",370)
				.text("A gets: $0")
				.attr("font-size",28)
				.attr("text-anchor","middle");

		playerBLeft.append("text")	
				.attr("x",400)
				.attr("y",400)
				.text("B gets: $0")
				.attr("font-size",28)
				.attr("text-anchor","middle");

		var playerBRight = playerB.append("g");

		var playerBRightLine = playerBRight.selectAll("line")
			.data(playerBRightData)
			.enter()
			.append("path")
			.attr("d",line)
			.attr("stroke-width",3)
			.attr("stroke","black")
			.on("mouseover", function(d) {
  				d3.select(this).attr("stroke-width",5)
					.attr("stroke", "red");
			})
			.on("mouseout", function(d) {
				d3.select(this).attr("stroke-width",3)
					.attr("stroke", "black");
			});

		playerBRight.append("text")	
				.attr("x",600)
				.attr("y",370)
				.text("A gets: $2")
				.attr("font-size",28)
				.attr("text-anchor","middle");

		playerBRight.append("text")	
				.attr("x",600)
				.attr("y",400)
				.text("B gets: $2")
				.attr("font-size",28)
				.attr("text-anchor","middle");

};
