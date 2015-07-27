/* // draw decision tree
	var textA = new PointText (new Point(85,30));
	textA.fillColor = 'black';
	textA.content = 'A';
	textA.fontSize = 20;
	
	var textB = new PointText (new Point(135,125));
	textB.fillColor = 'black';
	textB.content = 'B';
	textB.fontSize = 20;
	
	var offerA1 = new PointText (new Point(20,115));
	offerA1.fillColor = 'black';
	offerA1.content = ["A: 2\nB: 2"];
	offerA1.fontSize = 20;
	
	var offerB1 = new PointText (new Point(70,215));
	offerB1.fillColor = 'black';
	offerB1.content = ["A: 0\nB: 0"];
	offerB1.fontSize = 20;
	
	var offerB2 = new PointText (new Point(175,215));
	offerB2.fillColor = 'black';
	offerB2.content = ["A: 3\nB: 1"];
	offerB2.fontSize = 20; 
	
	var pathA1 = new Path();
	pathA1.strokeColor = 'black';
	pathA1.add(new Point(40,90));
	pathA1.add(new Point(90,40));
	
	var pathA2 = new Path();
	pathA2.strokeColor = 'black';
	pathA2.add(new Point(90,40));
	pathA2.add(new Point(140,90));
	
	var pathB1 = new Path();
	pathB1.strokeColor = 'black';
	pathB1.add(new Point(140,140));
	pathB1.add(new Point(90,190));
	
	var pathB2 = new Path();
	pathB2.strokeColor = 'black';
	pathB2.add(new Point(140,140));
	pathB2.add(new Point(190,190));
	
function onResize(event) {
    // Whenever the window is resized, recenter the path:
    textA.position = view.center - [0,40];
    textB.position = view.center + [50,50];
    offerA1.position = view.center - [50,-50];
    offerB1.position = view.center + [0,150];
    offerB2.position = view.center + [100,150];
    
    pathA1.position = view.center - [25,0];
    pathA2.position = view.center + [25,0];
    pathB1.position = view.center + [25,100];
    pathB2.position = view.center + [75,100];
}
*/