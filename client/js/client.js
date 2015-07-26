Meteor.subscribe('Players');
Meteor.subscribe('Games');

// Login validation
$.validator.setDefaults({
    rules: {
        email: {
            required: true,
            email: true
        },
        password: {
            required: true,
            minlength: 6
        }
    },
    messages: {
        email: {
            required: "You must enter an email address.",
            email: "You've entered an invalid email address."
        },
        password: {
            required: "You must enter a password.",
            minlength: "Your password must be at least {0} characters."
        }
    }
});


Template.register.onRendered(function(){
    var validator = $('.register').validate({
        submitHandler: function(event){
            var email = $('[name=email]').val();
            var password = $('[name=password]').val();
            Accounts.createUser({
                email: email,
                password: password
            }, function(error){
                if(error){
                    if(error.reason == "Email already exists."){
                        validator.showErrors({
                            email: "That email already belongs to a registered user."   
                        });
                    }
                } else {
                    Meteor.call('addPlayer', {});
                    Router.go("lobby");
                }   
            });
        }
    });
});

Template.login.onRendered(function(){
    var validator = $('.login').validate({
        submitHandler: function(event){
            var email = $('[name=email]').val();
            var password = $('[name=password]').val();
            Meteor.loginWithPassword(email, password, function(error){
              if(error){
                  if(error.reason == "User not found"){
                      validator.showErrors({
                        email: "That email doesn't belong to a registered user."   
                      });
                  }
                  if(error.reason == "Incorrect password"){
                      validator.showErrors({
                        password: "You entered an incorrect password."    
                      });
                  }
              } else {
                Meteor.call('addPlayer', {});
              	Router.go("lobby");
              }
            });
        }
    });
});

Template.navigation.events({
    'click .logout': function(event){
        event.preventDefault();
        Meteor.call('removePlayer',{})
        Meteor.logout();
        Router.go('login');
    }
});

