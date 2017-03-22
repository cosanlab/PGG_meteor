### Group Gossip Game

This is a paradigm in which users from Amazon's mechanical turk are matched in groups of arbitrary size (default 6) to play a modified PGG. Several different experimental conditions exist.

This paradigm is built using [Meteor.js](https://www.meteor.com/) a Javascript framework with fast easy-to-use client-server communication enabling effective realtime applications. This enables multi-user experimental design, aka real-time interactions over the internet.

This paradigm uses the excellent [TurkServer](https://github.com/HarvardEconCS/turkserver-meteor) for handling all Mturk requests (e.g. HIT creation, Assignment handling, Worker Payment, etc). You will need an MTurk requester account with AWS access and secret access keys.

In order to run this experiment follow the directions below:

**Installation**

1. Clone and install this repository 
	1. `git clone https://github.com/cosanlab/PGG_meteor.git`
	2. `cd PGG_meteor`
2. Clone and setup Turkserver
	3. `git clone https://github.com/TurkServer/turkserver-meteor.git packages/turkserver`
	4. `meteor add mizzao:turkserver`
3. Edit the settings-example.json file 
	1. Create a new adminPassword for logging into the administrative interface
	2. add an accessKeyId (requested from Amazon)
	3. add a secretAccessKey (requested from Amazon)
	4. add an externalURL for where your app is going to be hosted (only matter when you launch this for real in the sandbox or main market place, but not for local testing) 
	5. (Optional) rename `settings-example.json` to `settings.json`

**Running**

1. Launch the experiment with the settings file
	1. `meteor --settings settings.json`
2. Apply the treatment you want to run from the admin interface
	1. Navigate to `localhost:3000/turkserver` in you browser and login using the password from your `settings.json` file.
	2. Select the allConds batch from the drop down menu under the Filter Batch heading on the bottom left
	3. Click Manage
	4. Select the allConds batch under Manage Batches
	5. On the right use the drop down menu to apply 1 treatment and click the + to apply it (you can only run one treatment at time based on how randomization works)
	6. To instead run a different treatment click the gray X next to the applied treatment's name and select a different one
3. Open another browser and navigate to `localhost:3000` to simulate participation
4. You can edit globals like group size, avatar pictures, and treatments from the lib/globals.js file


Author: [Eshin Jolly](http://eshinjolly.com)