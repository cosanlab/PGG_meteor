//Get the batch condition outside a template helper to determine quiz questions
//Pretty cool quiz solution based on local meteor collections
//Took some insipiration from Lili's code
var Questions = new Mongo.Collection(null);
var questions = [];
var potSize = groupSize*40;

questions[0] = {
	text: '1) If you decide to contribute 50 points to the group account, how many points will be left in your private account?',
	answer: ['fifty','50'],
	correct: false,
	answered: false
};
questions[1] = {
	text: '2) If the total in the group account after the multiplier is applied is ' + potSize + ', and there are ' + groupSize + ' players, many points will each player receive?',
	
	answer: ['forty','40'],
	correct: false,
	answered: false
};
questions[2] = {
	text: '3) If there are ' + groupSize + ' players, and each player contributes 50 points, what is the total in the group account after the multiplier (1.5x) is applied? (enter a number)',
	answer: [String(50*groupSize*1.5)],
	correct: false,
	answered: false
};
questions[3] = {
	text: '4) How many other players will you be communicating with during the game?',
	answer: ['one','1'],
	correct: false,
	answered: false
};
questions[4] = {
	text: '5) How many messages will you be able to send and receive during communication?',
	answer: ['two','2'],
	correct: false,
	answered: false
};


//Initially just setup the core game questions
for(var q = 0; q<questions.length-2; q++){
	Questions.insert(questions[q]);
}

Template.quiz.helpers({
	questions: function(){
		var batchCond = Batches.findOne().treatments[0];
		if (batchCond == '2G' || batchCond == '6G'){
			for(var q = 3; q<questions.length; q++){
				Questions.insert(questions[q]);
			}
		}
		return Questions.find();
	},
	quizAttempts: function(){
		return Players.findOne(Meteor.userId()).quizAttempts;
	},
	passedQuiz: function(){
		return Players.findOne(Meteor.userId()).passedQuiz;
	}
});

Template.question.helpers({
	incorrect: function(){
		return this.answered && !this.correct;
	}
});

Template.quiz.events({
	'submit .quiz': function(event){
		//Only allow clients to attempt quiz twice before preventing them from doing so
		event.stopPropagation();
		event.preventDefault();
		var currentUser = Meteor.userId();
		var quizAttempts = Players.findOne(currentUser).quizAttempts;
		var form = event.target;
		Questions.find().forEach(function(q){
			var answer = $.trim(form[q._id].value.toLowerCase());
			var correct = $.inArray(answer,q.answer) >= 0 ? true: false;
			Questions.update({_id: q._id}, {$set: {correct: correct, answered: true}});
		});
		var result = Questions.find({correct:true}).count() == Questions.find().count();
		if(!result){
			Meteor.call('updatePlayerInfo',currentUser,{'quizAttempts':1},'inc');
			if(quizAttempts == 1){
				//End the quiz if they've submitted answers once before
				quizEmitter.emit('submittedQuiz');
			}
		} else{
			Meteor.call('updatePlayerInfo',currentUser,{'passedQuiz':true}, 'set');
			quizEmitter.emit('submittedQuiz');
		}
	}
});