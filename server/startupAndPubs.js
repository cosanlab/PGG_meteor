Meteor.startup(function(){
	try{
		//Create an experiment batch
		Batches.upsert({name: 'allConds'}, {name: 'allConds', active: true});
		//Add all treatments from globals.js
		for (i = 0, len = CONDS.length; i < len; i++) {
			TurkServer.ensureTreatmentExists({name: CONDS[i]});
		}
		//Treatments should be added from the admin menu as the app will not initialize with any treatments!
  		Batches.find().forEach(function(batch) {
		 	TurkServer.Batch.getBatch(batch._id).setAssigner(new TurkServer.Assigners.PGGAssigner(groupSize));
    		});
  		Meteor.users.find({"admin": {$exists:false},"status.online":true}).observe({
  			added: function(usr){
  				if(_.has(usr,'group')){
  					Meteor.call('connectionChange',usr._id,usr.group,'reconnect');
  				}
  			},
  			removed: function(usr){
				if(_.has(usr,'group')){
					Meteor.call('connectionChange',usr._id,usr.group,'disconnect');				
  				}
  			}
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

//Batch info for condition before getting matched 
Meteor.publish('Batches', function(){
	return Batches.find();
});

//Users for disconnection
Meteor.publish('userStatus',function(){
	return Meteor.users.find({"status.online":true});
});