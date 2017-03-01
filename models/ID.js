var mongoose = require('mongoose');


var Schema = mongoose.Schema;
var ObjectId = Schema.ObjectId;

// define the schema for our user model
var userSchema = mongoose.Schema({

    name: {type: String, unique: false, required: true},
    phone: {type: Number, unique: true, required: true, min: 100000000, max: 9999999999}



});


var Login = mongoose.model('login', userSchema);

module.exports = Login;
