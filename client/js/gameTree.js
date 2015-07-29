// requires d3
Template.gameTree.rendered = function () {
		var svg, width = 800, height = 800;

		svg = d3.select('#tree').append('svg')
		.attr('width', width)
		.attr('height',height);

		var tree1 = [ 
		[{ x: 400,   y: 100},  { x: 500,  y: 200}],
		[{ x: 400,   y: 100},  { x: 300,  y: 200}]];

		var tree2 = [ 
		[{ x: 500,   y: 240},  { x: 600,  y: 340}],
		[{ x: 500,   y: 240},  { x: 400,  y: 340}]];

		var line = d3.svg.line()
		    .x(function(d) { return d.x; })
		    .y(function(d) { return d.y; })
		    .interpolate("linear");

		svg.selectAll("line")
				.data(tree1)
				.enter()
				.append("path")
				.attr("d",line)
				.attr("stroke-width",2)
				.attr("stroke","black");

		svg.selectAll("line")
				.data(tree2)
				.enter()
				.append("path")
				.attr("d",line)
				.attr("stroke-width",2)
				.attr("stroke","black");
	
		svg.append("text")	
				.attr("x",400)
				.attr("y",90)
				.text("A")
				.attr("font-size",30)
				.attr("text-anchor","middle")
				.attr("font-weight" ,"bold");

		svg.append("text")	
				.attr("x",500)
				.attr("y",230)
				.text("B")
				.attr("font-size",30)
				.attr("text-anchor","middle")
				.attr("font-weight","bold");

		svg.append("text")	
				.attr("x",300)
				.attr("y",230)
				.text("A gets: $1")
				.attr("font-size",28)
				.attr("text-anchor","middle");

		svg.append("text")	
				.attr("x",300)
				.attr("y",260)
				.text("B gets: $3")
				.attr("font-size",28)
				.attr("text-anchor","middle");

		svg.append("text")	
				.attr("x",400)
				.attr("y",370)
				.text("A gets: $0")
				.attr("font-size",28)
				.attr("text-anchor","middle");

		svg.append("text")	
				.attr("x",400)
				.attr("y",400)
				.text("B gets: $0")
				.attr("font-size",28)
				.attr("text-anchor","middle");

		svg.append("text")	
				.attr("x",600)
				.attr("y",370)
				.text("A gets: $2")
				.attr("font-size",28)
				.attr("text-anchor","middle");

		svg.append("text")	
				.attr("x",600)
				.attr("y",400)
				.text("B gets: $2")
				.attr("font-size",28)
				.attr("text-anchor","middle");

};
