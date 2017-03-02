var express = require('express');
var router = express.Router();

var localAuthRoutes = function(client, codes, smsApiUrl){

  /*

  params: phone
  returns 200 {otpSessionId} if ok
          500 {error: "server error"} if server error

  */

  router.get('/request/:phone', function(req, res, next) {
    var phone = req.params.phone;

    var req = client.get(smsApiUrl + "/SMS/" + phone + "/AUTOGEN", function (data, response) {
      if(data.Status === "Success"){
        res.status(codes.OK).send({otpSessionId: data.Details});
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

  params: phone, otpSessionId, input
  returns: 200 {} if ok
           404 {error: "You are dead to me"} if no response from otp server
           401 {error: "You are dead to me"} if otp verification failed

  */

  router.get('/verify/:phone/session/:otpSessionId/otp/:input', function(req, res, next){
    var phone = req.params.phone;
    var input = req.params.input;
    var otpSessionId = req.params.otpSessionId;

    var req = client.get(smsApiUrl + "/SMS/VERIFY/" + otpSessionId + "/" + input, function (data, response) {

        if(!data){
          res.status(codes.NOT_FOUND).send({error: "You are dead to me"});
        } else {
          if(data.Status === "Success" && data.Details === "OTP Matched") {
            res.status(codes.OK).send({});
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
    
  });

  return router;

}



module.exports = localAuthRoutes;
