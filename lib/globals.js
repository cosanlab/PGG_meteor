//End game timer
endGameTimer = false;

//Change TS assignment status
//Get assignment id
Assignments.update(asst.userId,{$set:{status:'passedQuiz'}});