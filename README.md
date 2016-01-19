### Public Goods Game

This is a paradigm in which users from Amazon's mechanical turk are matched in groups of arbitrary size to play a modified PGG. Several different experimental conditions exist.

This paradigm is built using [Meteor.js](https://www.meteor.com/) a Javascript framework with fast easy-to-use client-server communication enabling effective realtime applications. This enables multi-user experimental design, aka real-time interactions over the internet.

This paradigm uses the excellent [TurkServer](https://github.com/HarvardEconCS/turkserver-meteor) for handling all Mturk requests (e.g. HIT creation, Assignment handling, Worker Payment, etc).

This version integrates all other branches (which are based on separate experimental conditions). The caveat is now condition is set both on the game level and on Turkserver's ["Batch" level](https://virtuallab.github.io/). This allows different instruction sets for participants prior to games being started.

The caveat is that upon intitial deployment, the default Batch will have *no treatments* assigned to it. It's up to the user to assign treatments (one per batch) through the Turkserver admin interface, prior to launching HITs.

More details to follow.

Author: [Eshin Jolly](http://eshinjolly.com)