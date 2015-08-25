/*
//Function to allow easy subclassing, in coffee script this is just extend
var extend = function(child, parent) { for (var key in parent) { if (hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor(); child.__super__ = parent.prototype; return child; },
  hasProp = {}.hasOwnProperty;

//Our assigner. Currently it's a combination between the TestAssigner and the sequential assigner and we'll likely have to do some super-classing to get it to work properly
//It differs from other assigners in that in doesn't expect users to ever be in the lobby more than once 
//Still need to figure out a solution so that each person can submit their HIT independenly and the instance is torn down only when EVERYONE in it has submitted their hit. Maybe something with check if all assignments in instance have submitted status?
TurkServer.Assigners.UGAssigner = (function(superClass) {
  extend(UGAssigner, superClass);

  //Construct the class based on the parent, taking in an arbitrary number of arguments?
  function UGAssigner(groupSize){
    this.groupSize = groupSize;
    //Not sure we need this line so it's commented
    //return UGAssigner.__super__constructor.apply(this,arguments);
  }

  UGAssigner.prototype.initialize = function(){
    //Call the parent's initialize method with an arbitrary number of arguments?
    UGAssigner.__super__.initialize.apply(this, arguments);

    //First find out if there are any instances that already exist for this batch
    if((exp = Experiments.findOne({batchId: this.batch.batchId})) != null){
      //If so set save that instance in the assigner 
      this.instance = TurkServer.Instance.getInstance(exp._id);
    } else{
      //Otherwise create a new instance with a random treatment
      var treatment = _.sample(this.batch.getTreatments());
      this.instance = this.batch.createInstance([treatment]);
      return this.instance.setup();

    }
  };

  //How to treat users when they enter the lobby
  UGAssigner.prototype.userJoined = function(asst){
    //When a user joins check the current instance to see if it is full
    if (this.instance.users().length >= this.groupSize) {
      //If it is create a new instance with a randomly sampled treatment
      var treatment = _.sample(this.batch.getTreatments());
      this.instance = this.batch.createInstance([treatment]);
      this.instance.setup();
    }
    //Then despite whether or not we've created a new instance, put the user into that instance, i.e. this will be old non-full instance if a new one wasn't created, or it will be a brand spanking new one if the old one was full
    this.lobby.pluckUsers([asst.userId]);
    return this.instance.addAssignment(asst);
  };

  return UGAssigner;

})(TurkServer.Assigner);

//Parent Assigner object 
TurkServer.Assigners = {};

TurkServer.Assigner = (function() {
  function Assigner() {}

  Assigner.prototype.initialize = function(batch) {
    this.batch = batch;
    this.lobby = batch.lobby;
    this.lobby.events.on("user-join", this.userJoined.bind(this));
    this.lobby.events.on("user-status", this.userStatusChanged.bind(this));
    this.lobby.events.on("user-leave", this.userLeft.bind(this));
  };

  Assigner.prototype.assignToNewInstance = function(assts, treatments) {
    var asst, i, instance, len;
    this.lobby.pluckUsers(_.pluck(assts, "userId"));
    instance = this.batch.createInstance(treatments);
    instance.setup();
    for (i = 0, len = assts.length; i < len; i++) {
      asst = assts[i];
      instance.addAssignment(asst);
    }
    return instance;
  };

  Assigner.prototype.userJoined = function() {};

  Assigner.prototype.userStatusChanged = function() {};

  Assigner.prototype.userLeft = function() {};

  return Assigner;

})();






//Most basic assigner that makes sure each assignment gets their own experiment instance, i.e. all assignments have unique group ids
TurkServer.Assigners.SimpleAssigner = (function(superClass) {
  extend(SimpleAssigner, superClass);

  function SimpleAssigner() {}
  //Takes in an assignment and checks to see if it is part of any instances
  //If it is, take the assignment to the exit survey
  //if it isn't take it to a new instance 
  SimpleAssigner.prototype.userJoined = function(asst) {
    var treatment;
    if (asst.getInstances().length > 0) {
      this.lobby.pluckUsers([asst.userId]);
      return asst.showExitSurvey();
    } else {
      treatment = _.sample(this.batch.getTreatments() || []);
      //Parent method which calls, createInstance followed by instance.addAssignment
      return this.assignToNewInstance([asst], [treatment]);
    }
  };

  return SimpleAssigner;
})(TurkServer.Assigner);

//Slightly more complicated assigner that puts each assignment into a single experiment instance, i.e. all assignments share the same group id
TurkServer.Assigners.TestAssigner = (function(superClass) {
  extend(TestAssigner, superClass);

  //Construct the class based on the parent, taking in an arbitrary number of arguments?
  function TestAssigner() {
    return TestAssigner.__super__.constructor.apply(this, arguments);
  }

  TestAssigner.prototype.initialize = function() {
    var exp;
    //Call the parent's initialize method with an arbitrary number of arguments?
    TestAssigner.__super__.initialize.apply(this, arguments);
    //See if there is an experiment/instance associated with that batch
    if ((exp = Experiments.findOne({
      batchId: this.batch.batchId
    })) != null) {
      //If so set save that instance in the assigner 
      return this.instance = TurkServer.Instance.getInstance(exp._id);
    } else {
      //if not make a new instance and internalize it
      this.instance = this.batch.createInstance(this.batch.getTreatments());
      return this.instance.setup();
    }
  };
  //Take in an assignment and sees if it's part of any instances
  //If it is, take the assignment to the exit survey
  TestAssigner.prototype.userJoined = function(asst) {
    if (asst.getInstances().length > 0) {
      this.lobby.pluckUsers([asst.userId]);
      return asst.showExitSurvey();
    } else {
      //If not add it to the internal instance, by basically adding a group fields to the Meteor.users and andy partioned data bases
      try {
        this.instance.addAssignment(asst);
      } catch (_error) {}
      return this.lobby.pluckUsers([asst.userId]);
    }
  };

  return TestAssigner;

})(TurkServer.Assigner);



//Lili's Pair assigner (not sure how it works totally yet)
//Something along the lines of using a game counter to see whether a user should be kept in an experiment instance or not
TurkServer.Assigners.PairAssigner = (function(superClass) {
  extend(PairAssigner, superClass);

  function PairAssigner() {
    return PairAssigner.__super__.constructor.apply(this, arguments);
  }

  PairAssigner.prototype.initialize = function() {
      PairAssigner.__super__.initialize.apply(this, arguments);

      this.counter = this.setCounter();

      this.lobby.events.on("reset-lobby", (function(_this) {
	  return function() {
	      _this.counter = 0;
	      console.log('Reset counter.');
	  }
      })(this));

      this.lobby.events.on("exit-survey", (function(_this) {
	  return function() {
	      var lobbyAssts = _this.lobby.getAssignments();
	      for (var i=0; i<lobbyAssts.length; i++) {
		  var asst = lobbyAssts[i];
		  _this.lobby.pluckUsers([asst.userId]);
		  asst.showExitSurvey();
	      }
	  }
      })(this));

      this.lobby.events.on("next-game", (function(_this) {
	  return function() {
	      assignFunc(_this);
	  };
      })(this));

      this.lobby.events.on("end-games", (function(_this) {
	  return function() {
	      endGamesFunc(_this);
	  };
      })(this));

  };
    
 
  PairAssigner.prototype.setCounter = function() {
      var assts = Assignments.find({status: 'assigned'}).fetch();
      if (assts.length == 0) { return 0; }
      var counts = _.map(assts, function(asst) {
	  return (asst.instances && asst.instances.length) || 0;
      });
      return _.max(counts);
  }

  PairAssigner.prototype.userJoined = function(asst) {
      if (asst.getInstances().length > 0) {
	  LobbyStatus.update({_id: asst.userId}, {$set: {status: true}});
      }
      if (this.counter >= numGames) { 
	  this.lobby.pluckUsers([asst.userId]);
	  asst.showExitSurvey();
      }
  };

  return PairAssigner;

})(TurkServer.Assigner);

//Andrew's sequential assigner 
TurkServer.Assigners.SequentialAssigner = (function(superClass) {
  extend(SequentialAssigner, superClass);

  //Setup with additional group size and first instance properties
  function SequentialAssigner(groupSize, instance1) {
    this.groupSize = groupSize;
    this.instance = instance1;
  }

  SequentialAssigner.prototype.userJoined = function(asst) {
    var treatment;
    //If the number of people in this instance is greater than or equal to the group size desired for the instance, create a new instance of randomly sampled treatment type and put them in it
    if (this.instance.users().length >= this.groupSize) {
      treatment = _.sample(this.batch.getTreatments());
      this.instance = this.batch.createInstance([treatment]);
      this.instance.setup();
    }
    this.lobby.pluckUsers([asst.userId]);
    return this.instance.addAssignment(asst);
  };

  return SequentialAssigner;

})(TurkServer.Assigner);

*/