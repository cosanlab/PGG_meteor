# Experiment Logic

##Current todos

- if < numRounds/2 rematch otherwise give bonus and submit
    + If rematched add fixed rematching bonus


- Use session variables about time on each page, that auto makes a meteor call for the player if they take too long
- Game states in db:
    + assignment (initially only)
    + pChoose (click then)
    + pDisp (auto advance then)
    + pSendMess1 (click then)
    + pReceiveMess1 (auto advance then)
    + pSendMess2 (click then)
    + pReceiveMess2 (auto advance then)
    + gOut (auto advance, next round)
    + playerRatings (if on last round)
    + finalOut OR connectionError
        * lostUser


###Assigner
- ~~Is user associate with an experiment?~~
    + Yes: Does user need rematch?
        * Yes: Leave them in lobby, don't add to players db
        * No: Push them to exit survey
    + No: Add them to players db + leave them in lobby
- ~~Are there 5 people in the lobby?~~
    + Yes: create a new experiment instance and add them all
    + No: do nothing
- ~~Start a lobby timer for the user~~
    + If 5 minutes have passed send them to the exit survey
- Has the user completed the instructions?
    + Implement functionallity to have users complete the instructions before they get matched with other players
    + If the successfully understand the game, i.e. pass the quizz, change their status and then try to match them


###Game Instructions
- Explain game
- Role assignment (Players A - E)

###Game
- Start round
    1. Set all endowments to be equal
    2. Players make contributions
    3. Players see contribution outcomes
        - Low, Med, High hidden information (e.g. only neighbors' contributions)
    4. Players send gossip/message to assigned partner
        - Could also do anonymous to group as a variant
    5. Players make punishment decisions?
    6. Pot multiplier applied
    7. Players see own, each others' and group payoff
    8. Rinse and repeat
- One outcome finally enacted as bonus
    + Alternatively no endowment resetting and *all* earnings are bonus payment


###Exit Survey
- Age
- Gender
- Feedback

###Database organization
- Hierarchical dict structure
- Players:
    + name: meteorID
    + joinTime: datenow
    + quizAttempts: int
    + passedQuiz: boolean
    + needRematch: boolean
    + status: waiting/instructions/playing/disconnected/finished
Games:
- Condition: high/med/low + gossip/nogossip
- Game start
- Round
- State: initialized/
- Players
    + MeteorID:
        * name: A
        * readyStatus: boolean
        * rematched: boolean
        * instrComp: boolean (maybe don't need)
        * age: int
        * gender: str
        * feedback: str
        * contribution:
            - Round 1
            - Round 2...
            - Round N
        * message:
            - Round 1
            - Round 2...
            - Round N
    + MeteorID: ...

###Attrition considerations
- Tell people upfront that if they idle they won't get paid the full amount
- If people drop:
    + Ask them if they want to be rematched (best option)
        * Pay them for their time
        * Tell them they will earn extra money if they play again
    + Impute what they would have done from previous round or use norm from previous round (ok option)
    + End game (least ideal)
- Run in batches with one condition at a time instead of randomizing
    + Repeat participants doesn't really matter because their is no *fixed* demand characteristic
- Keep lobby re-entry counter for rematching players
    + If they have been in the lobby multiple times (e.g. due to other players not understanding or connection drops) send them to exit survey
- Timer for making a decision = 30s, then middle contribution gets made for them, tell them they lose money for rounds they dont make a decision

###Analysis
- New pandas df for each player dict
- Then concatenate them
- Visualize player decisions correlations as similarity matrix per round (see Evernote)

###Extra notes
- When a person drops, start the disconnection timer. If the timer goes off then ask the remaining players if they want to get re-matched or cash out.

###Changes
- 6 people
- People know num players but not network structure
- See neighbors contributions, send a message, see how much they earned:
    - Communication: 140 char question, followed by 140 char answer
- As about average group contribtion after last round, also ask about network structure
- Alterate versions:
    + public shaming: message to the group to enforce descriptive norm
