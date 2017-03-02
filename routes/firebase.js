var express = require('express');
var router = express.Router();


var firebaseRoutes = function(database, admin, client, codes, smsApiUrl){

  /*

  params: phone
  returns 200 {} if ok
          500 {error: "server error"} if server error

  */
  router.get('/request/:phone', function(req, res, next) {
    var phone = req.params.phone;


    var req = client.get(smsApiUrl + "/SMS/" + phone + "/AUTOGEN", function (data, response) {

        if(data){
          var user = database.ref('users/' + phone);
          user.set({
            otpSessionId: data.Details
          });
          res.status(codes.OK).send({});
        } else {
          res.status(codes.SERVER_ERROR).send({error: "server error"});
        }
    });

    req.on('requestTimeout', function(reqs){
      res.status(codes.SERVER_ERROR).send({error: "server error"});
    });

    req.on('responseTimeout', function(ress){
      res.status(codes.SERVER_ERROR).send({error: "server error"});
    });

    req.on('error', function(err){
      res.status(codes.SERVER_ERROR).send({error: "server error"});
    });
  });


  /*

  params: phone, input
  returns: 200 {customToken} if ok
           404 {error: "You are dead to me"} if no session found
           401 {error: "You are dead to me"} if unauthorized
           500 {error: "server error"} if server error

  */

  router.get('/verify/:phone/otp/:input', function(req, res, next){
    var phone = req.params.phone;
    var input = req.params.input;

    var user = database.ref('users/' + phone);

    user.once('value').then(function(snapshot){

      if(snapshot && snapshot.val()) {
        var req = client.get(smsApiUrl + "/SMS/VERIFY/" + snapshot.val().otpSessionId + "/" + input, function (data, response) {

            if(!data){
              res.status(codes.NOT_FOUND).send({error: "You are dead to me"});
            } else {
              if(data.Status === "Success" && data.Details === "OTP Matched") {
                admin.auth().createCustomToken(phone)
                  .then(function(customToken) {
                      console.log(customToken);
                      res.status(codes.OK).send({customToken: customToken});
                  })
                  .catch(function(error) {
                    console.log("Error creating custom token:", error);
                  });
              } else {
                res.status(codes.UNAUTHORIZED).send({error: "You are dead to me"});
              }
            }
        });

        req.on('requestTimeout', function(reqs){
          res.status(codes.SERVER_ERROR).send({error: "server error"});
        });

        req.on('responseTimeout', function(ress){
          res.status(codes.SERVER_ERROR).send({error: "server error"});
        });

        req.on('error', function(err){
          res.status(codes.SERVER_ERROR).send({error: "server error"});
        });
        
      } else {
        res.status(codes.NOTES_FOUND).send({error: "not found"})
      }
    });
  });

  return router;
}

module.exports = firebaseRoutes;
