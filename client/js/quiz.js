Template.quiz.helpers({
	feedback: function(){
		var currentUser = Meteor.userId();
		var player = Players.findOne(currentUser);
		var text = 'Correct! '.repeat(5).slice(0,5);
		var dispClass; 
		if(player.quizAcc){
			dispClass = 'correct '.repeat(5).slice(0,5);
			for(q=0; q<player.quizAcc.length; q++){
				if(!player.quizAcc[q]){
					text[q] = 'Incorrect!';
					dispClass[q] = 'incorrect';
				} 
			}
		} else{
			dispClass = 'noresp '.repeat(5).slice(0.5);
		}
		return{
			text: text,
			dispClass: dispClass
		};
	}
});

Template.quiz.events({
	//Quiz event handler. Computes accuracy and calls meteor method with pass/fail
	'click button':function(event){
		event.stopPropagation();
		event.preventDefault();
		$("#submitQuiz").css("visibility","hidden");
		var qs;
		var accs = Array.apply(null,{length:5}).map(function(){return 0;});
		var ansStr = ['fifty','forty','more','one','two'];
		var ansInt = ['50','40','more','1','2'];
		var ACC = 0;
		var result;

		for(i=0; i<5; i++) {
			qs[i] = $('#q' + String(i+1)).val();
			if(!qs[i]){
				qs[i] = 'NA';
			} else{
				qs[i] = qs[i].toLowerCase();
			}
			if((qs[i] == ansStr[i]) || (qs[i] == ansInt[i])){
				accs[i] = 1;
			}
			ACC += accs[i];
		}
		ACC = ACC/accs.length;
		if(ACC >= 0.8){
			result = true;
		} else{
			result = false;
		}
		Meteor.call('updateQuiz',Meteor.userId(),result,accs);
		emitter.emit('submittedQuiz');
		
	}
});
