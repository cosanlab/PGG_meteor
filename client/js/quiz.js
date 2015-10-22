//Pretty cool quiz solution based on local meteor collections
//Took some insipiration from Lili's code
var Questions = new Mongo.Collection(null);
var questions = [];
questions[0] = {
	text: '1) If you decide to contribute 50 points to the group account, how many points will be left in your private account?',
	answer: ['fifty','50'],
	correct: false,
	answered: false
};
questions[1] = {
	text: '2) If the total in the group account after the multiplier is applied is 200, how many points will each player receive?',
	answer: ['forty','40'],
	correct: false,
	answered: false
};
questions[2] = {
	text: '3) If you contribute 50 points, and both players visible to you contribute 10 points each, and you receive 75 points at the end of the round, did the other two players contribute more or less than you did?',
	answer: ['more'],
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

for(var q = 0; q<questions.length; q++){
	Questions.insert(questions[q]);
}


Template.quiz.helpers({
	questions: function(){
		return Questions.find();
	},
	quizAttempts: function(){
		return Players.findOne(Meteor.userId()).quizAttempts;
	}
});

Template.question.helpers({
	incorrect: function(){
		return this.answered && !this.correct;
	}
});

Template.quiz.events({
	'submit .quiz': function(event){
		//Only allow clients to attempt quiz twice before preventing them from doing so. Because server and client databases don't update instantly, increasing the quiz attempt counter and immediately querying it usually results in the local mongo being behind the server at the time of query. So to remedy that just look to see if they've done the quiz at least once before (i.e. got something wrong) then look to see how they did when they submitted this time, and pass or fail them.
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
				Meteor.call('updatePlayerInfo',currentUser,{'status':'failedQuiz'},'set');
				quizEmitter.emit('submittedQuiz');
			}
		} else{
			Meteor.call('updatePlayerInfo',currentUser,{'passedQuiz':true}, 'set');
			quizEmitter.emit('submittedQuiz');
		}
	}
});