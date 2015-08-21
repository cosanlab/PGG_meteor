Players = new Mongo.Collection('players');
Games = new Mongo.Collection('games');

//Try out collection partitioning
TurkServer.partitionCollection(Games);