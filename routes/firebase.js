var express = require('express');
var router = express.Router();


var firebaseRoutes = function(database, admin, client, codes, smsApiUrl){

  /*

  params: phone
  returns 200 {}

  */
  router.get('/request/:phone', function(req, res, next) {
    var phone = req.params.phone;
    res.status(codes.OK).send({});

    client.get(smsApiUrl + "/SMS/" + phone + "/AUTOGEN", function (data, response) {

        if(data){
          var user = database.ref('users/' + phone);
          user.set({
            otpSessionId: data.Details
          });
        }
    });
  });


  /*

  params: phone, input
  returns: 200 {customToken} if ok
           404 {error: "You are dead to me"} if no session found
           401 {error: "You are dead to me"} if unauthorized

  */

  router.get('/verify/:phone/otp/:input', function(req, res, next){
    var phone = req.params.phone;
    var input = req.params.input;

    var user = database.ref('users/' + phone);

    user.once('value').then(function(snapshot){

      if(snapshot && snapshot.val()) {
        client.get(smsApiUrl + "/SMS/VERIFY/" + snapshot.val().otpSessionId + "/" + input, function (data, response) {

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
      } else {
        res.status(codes.NOTES_FOUND).send({error: "not found"})
      }
    });
  });

  return router;
}

module.exports = firebaseRoutes;
