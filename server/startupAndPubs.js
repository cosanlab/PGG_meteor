Meteor.startup(function(){
	try{
		//Create a test batch for now and give it an assigner
		Batches.upsert({name: 'Test_1'}, {name: 'Test_1', active: true});
		TurkServer.ensureTreatmentExists({name: 'withMessaging'});
    	TurkServer.ensureTreatmentExists({name: 'noMessaging'});
		Batches.update({name: 'Test_1'}, {$addToSet: {treatments: '2G'}});
		//var batch = TurkServer.Batch.getBatchByName('Test_1');
		//batch.setAssigner(new TurkServer.Assigners.UGAssigner(2));
		 Batches.find().forEach(function(batch) {
		 	TurkServer.Batch.getBatch(batch._id).setAssigner(new TurkServer.Assigners.PGGAssigner(groupSize,'2G'));
    		});
	} 
	catch(e){
		console.log(e);
		return;
	}
});

///Subjects DB
Meteor.publish('Players', function(){
	return Players.find();
});

//Games DB
Meteor.publish('Games', function(){
   return Games.find();
});

//Users for disconnection
Meteor.publish('userStatus',function(){
	return Meteor.users.find({"status.online":true});
});