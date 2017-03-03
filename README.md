# jelli-otp-module
A module that implements otp using 2factor, local and firebase databases.

# Usage:

Download the admin json file from firebase and put it in the root directory jelli-otp-module-mean-server. Reference it in the app.js file:

```javascript
var serviceAccount = require("./jelli-firebase-admin-file-name.json");
```

Set the database url of your firebase project in app.js as follows:

```javascript
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://<YOUR-FIREBASE-DATABASE-URL>"
});
```

Create a json file in the root directory of jelli-otp-module-mean-server and set your 2factor base url as follows:

```json
{
  "smsApiUrl": "http://2factor.in/API/V1/<YOUR-API-KEY-HERE>"
}
```

Reference it in the app.js file as follows:

```javascript
var smsApiUrlObject = require('./path-to-the-json-file.json');
var smsApiUrl = smsApiUrlObject.smsApiUrl;
```

Add the library to your android project and create an intent:

```java
Intent intent = new Intent(this, OTPActivity.class);
```

Pass the base url where jelli-otp-module-mean-server is running:

```java
intent.putExtra(OTPActivity.ARG_BASE_URL, "http://192.168.0.105:3000/");
```

Pass the countries:

```java
ArrayList<Country> countries = new ArrayList<>();
countries.add(Country.getInstance("INDIA", 91));
countries.add(Country.getInstance("PAKISTAN", 92));
countries.add(Country.getInstance("UNITED STATES", 1));
intent.putParcelableArrayListExtra(OTPActivity.ARG_COUNTRIES, countries);
```

Start the activity:

```java
//Using firebase
startActivityForResult(intent, OTPActivity.TYPE_FIREBASE);
//Using local database
startActivityForResult(intent, OTPActivity.TYPE_LOCAL);
```
Handle the response:
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if(resultCode == RESULT_OK){
        if(requestCode == OTPActivity.TYPE_FIREBASE){
            String token = data.getStringExtra(OTPActivity.ARG_TOKEN);
            //Use this firebase token to login the user to firebase
        } else {
            //Use custom token to login to local server
        }
        } else if(resultCode == RESULT_CANCELED) {
            //User canceled
        } else {
            //Something went terribly wrong
        }
    }
}
```

Arguments to OTPActivity:

| Argument | Usage | Type | Mandatory |
| -------- | ----- | ---- | --------- |
| ARG_AUTH_TYPE | Specifies the database type to be used | TYPE_FIREBASE or TYPE_LOCAL | No, defaults to TYPE_LOCAL
| ARG_INPUT_PHONE | A message to the user suggesting why the app needs his/her phone number | String | No
| ARG_INPUT_OTP | A message to the user suggesting what he/she has to do with the otp code she received on his/her phone | String | No
| ARG_BASE_URL | The base url where jelli-otp-module-mean-server is running | String | Yes, throws exception if not set
| ARG_COUNTRIES | An array list of countries you want to support | ArrayList<Country> | Yes, throws exception if not set
| ARG_PHONE_HINT | The hint for phone edit text | String | No
| ARG_OTP_HINT | The hint for otp code edit text | String | No
| ARG_SUBMIT_SMS | The text on the 'send sms' button | String | No
| ARG_SUBMIT_VERIFY | The text on the 'verify my code' button | String | No
| ARG_CODE_RESENT | The message that appears when the code is successfully resent | String | No
| ARG_RESEND_CODE_MESSAGE | The text on 'resend otp code' button | String | No
| ARG_WAIT_MESSAGE | The message that appears when waiting for server response | String | No
| ARG_SERVER_ERROR_MESSAGE | The message that is displayed when server returns 500 | String | No
| ARG_VERIFICATION_FAILED_MESSAGE | The message that is displayed when code wasn't correct | String | No
| ARG_EMPTY_PHONE_ERROR_MESSAGE | The error message to be put on phone edit text when it is empty | String | No
| ARG_PHONE_LENGTH_ERROR_MESSAGE | The error message to be put on phone edit text when phone number length is not equal to 10 | String | No
| ARG_EMPTY_OTP_ERROR_MESSAGE | The error message to be put on otp code edit text when it is empty | String | No

Arguments from OTPActivity:

| Argument | Usage | Value |
| ------ | ------ | ------ |
| ARG_TOKEN | The firebase token | String |

Supported screens configurations:

* layout
* layout-land
* layout-large
* layout-large-land
* layout-xlarge
* layout-xlarge-land

Screenshots

| Layout | Screenshot |
|--------|------------|
|phone layout|<img src="https://raw.github.com/jelliroo/jelli-otp-module/master/shots/phone-layout.jpeg" width="250">|
|phone layout land|<img src="https://raw.github.com/jelliroo/jelli-otp-module/master/shots/phone-layout-land.jpeg" height="250">|
|otp layout|<img src="https://raw.github.com/jelliroo/jelli-otp-module/master/shots/otp-layout.jpeg" width="250">|
|otp layout land|<img src="https://raw.github.com/jelliroo/jelli-otp-module/master/shots/otp-layout-land.jpeg" height="250">|
|phone layout large|<img src="https://raw.github.com/jelliroo/jelli-otp-module/master/shots/phone-layout-large.jpeg" width="250">|
|phone layout land large|<img src="https://raw.github.com/jelliroo/jelli-otp-module/master/shots/phone-layout-large-land.jpeg" height="250">|
|otp layout large|<img src="https://raw.github.com/jelliroo/jelli-otp-module/master/shots/otp-layout-large.jpeg" width="250">|
|otp layout land large|<img src="https://raw.github.com/jelliroo/jelli-otp-module/master/shots/otp-layout-large-land.jpeg" height="250">|
|phone layout xlarge|<img src="https://raw.github.com/jelliroo/jelli-otp-module/master/shots/phone-layout-xlarge.jpeg" width="250">|
|phone layout xland large|<img src="https://raw.github.com/jelliroo/jelli-otp-module/master/shots/phone-xlayout-large-land.jpeg" height="250">|
|otp layout xlarge|<img src="https://raw.github.com/jelliroo/jelli-otp-module/master/shots/otp-layout-xlarge.jpeg" width="250">|
|otp layout xland large|<img src="https://raw.github.com/jelliroo/jelli-otp-module/master/shots/otp-layout-xlarge-land.jpeg" height="250">|
