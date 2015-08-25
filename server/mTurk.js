/*
//HELPER FUNCTIONS
function createRequest() {
//Cross browser way to create XML request object 
  var result = null;
  if (window.XMLHttpRequest) {
    //FireFox, Safari, etc.
    result = new XMLHttpRequest();
    if (typeof xmlhttp.overrideMimeType != 'undefined') {
    	//force xml data type return
      	result.overrideMimeType('text/xml'); 
    }
  }
  else if (window.ActiveXObject) {
    //Internet Explorer
    result = new ActiveXObject("Microsoft.XMLHTTP");
  } 
  else {
    console.log('No known mechanism -- consider aborting the application');
  }
  return result;
}

function makeSignature(service,operation,timeStamp,secretKey){
//Create AWS signature based on requesting service, operation requested, system timestamp and AWS secret key. Adheres to the method AWS wants.
	//Grab just the values of each arg since they're strings
	var serviceVal = service.slice(service.indexOf('=')+1);
	var operationVal = operation.slice(operation.indexOf('=')+1);
	var timeStampVal = timeStamp.slice(timeStamp.indexOf('=')+1);
	//Hash them by HMAC then convert to base64
	var toHash = serviceVal+operationVal+timeStampVal;
	var hash = CryptoJS.HmacSHA256(toHash, secretKey);
  	var hashInBase64 = CryptoJS.enc.Base64.stringify(hash);
  	return hashInBase64;
}

//Cosanlab key id info in csv
var keyID = '';
var secretKey = '';

//EXAMPLE OF CALLING API
//SETUP API CALL PARAMETERS

//Configurable parameters
var operationName = 'CreateHIT';

//Parameterization of api values

//Basic url
var mainURL = 'https://mechanicalturk.amazonaws.com/?';

//Requested service
var service = 'Service=AWSMechanicalTurkRequester';

//Key
var AWSkey = 'AWSAccessKeyId=' + keyID;

//Version: look up WSDL based on service requested, this for the API
var version = 'Version=2014-08-15'; 

//Operation: what function call you're doing
var operation = 'Operation=' + operationName;

//Timestamp: in intl dateTime format
var timeStamp = 'Timestamp=' + new Date().toISOString();

//Signature: needs to be generated based on key
var signature = 'Signature=' + makeSignature(service, operation, timeStamp, secretKey);

//Response group: what should the server send back in addtion to the request
//Bare minimum here
var response = 'ReponseGroup.0=Minimal';

//Parameters for the operation being called, just an example below
//Parameters with subparameters are set using: Param.N.subParam=value
//If there can be multiple of the same param (and same subparams), just increment N
var title = 'Title=My%20Title';
var rewards = 'Reward.1.Amount=5&Reward.1.CurrencyCode=USD';
var duration = 'lifetimeInSecond=604800';
var params = [title, reward, duration, lifetime].join('&');

//MAKE API CALL

//Create a single API call
var url = [mainURL, service, AWSkey, version, operation, signature, timeStamp, response, params].join('&');	


//New request object
var request = createRequest();
//Make call asyncly
request.open("GET",url, true);
request.send();
var xmlDoc = request.responseXML;

//DO STUFF WITH SPECIFIC RETURN ELEMENTS, E.G. 
var workerID = xmlDoc.getElementsByTagName("WorkerId")[0].childNodes[0].nodeValue;
Meteor.call('addPlayer',workerID);


function Request(operation, options){
	var url = 'https://mechanicalturk.amazonaws.com/?';
	this.AWSAccessKeyId = '';
	this.Version = '2014-08-15';
	this.Operation = operation;
	
	var AWSkey = '';
	var secretKey = '';
	var mainURL = 'https://mechanicalturk.amazonaws.com/?';
	var service = 'Service=AWSMechanicalTurkRequester';
	var version = 'Version=2014-08-15'; 
	var timeStamp = 'Timestamp=' + new Date().toISOString();
	var signature = 'Signature=' + makeSignature(service, operation, timeStamp, secretKey);
	var response = 'ReponseGroup.0=Minimal';

	return $.param(request);

}
//RESOURCES

Examples of http calls with different synch/asynch timing setups:

https://developer.mozilla.org/en-US/docs/Web/API/XMLHttpRequest/Synchronous_and_Asynchronous_Requests

http://blogs.msdn.com/b/wer/archive/2011/08/03/why-you-should-use-xmlhttprequest-asynchronously.aspx

Grabbing xml elements:
http://www.w3schools.com/dom/dom_methods.asp

//LOGIC
1) Worker views hit with external link
2) Worker accepts HIT, then clicks our link
3) Meteor queries the referral link (document.referrer), parses it and grabs their workerID, and assignmentID, calls addPlayer method and adds them with their workerID
4) They sit in lobby and wait for another player
5) After the game is over call ApproveAssignment, which circumvents submitting assignment along with GrantBonus


*/