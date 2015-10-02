## Experiment Logic

###Assigner
- Is user associate with an experiment?
    + Yes: Does user need rematch?
        * Yes: Leave them in lobby, don't add to players db
        * No: Push them to exit survey
    + No: Add them to players db + leave them in lobby
- Are there 5 people in the lobby?
    + Yes: create a new experiment instance and add them all
    + No: do nothing
- Start a lobby timer for the user
    + If 5 minutes have passed send them to the exit survey

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
- State
- Players
    + Player A:
        * name: meteorID
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
    + Player B: ...

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