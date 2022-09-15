const crypto = require('crypto');
const hash = crypto.createHash('md5');
const utf8 = require('utf8');
const axios = require('axios');

/*
    The key from one of your Verification Apps, found here https://dashboard.sinch.com/verification/apps
*/
const APPLICATION_KEY = "<REPLACE_WITH_APP_KEY>";

/*
    The secret from the Verification App that uses the key above, found here https://dashboard.sinch.com/verification/apps
*/
const APPLICATION_SECRET = "<REPLACE_WITH_APP_SECRET>";

/*
    The number that will receive the SMS. Test accounts a`re limited to verified numbers.
    The number must be in E.164 Format, e.g. Netherlands 0639111222 -> +31639111222
*/
const TO_NUMBER = "<REPLACE_WITH_YOUR_NUMBER>";

const SINCH_URL = "https://verificationapi-v1.sinch.com/verification/v1/verifications";

const payload = JSON.stringify({
    identity: {
        type: 'number',
        endpoint: TO_NUMBER
    },
    method: 'sms'
});

let hmac = crypto.createHmac('sha256', Buffer.from(APPLICATION_SECRET, 'base64'));
let contentMD5 = hash.update(utf8.encode(payload)).digest('base64');
let timeStampISO = new Date().toISOString();

let stringToSign = 'POST'
    + '\n' + contentMD5
    + '\n' + 'application/json; charset=utf-8'
    + '\n' + 'x-timestamp:'+ timeStampISO
    + '\n' + '/verification/v1/verifications';

hmac.update(stringToSign);
let signature = hmac.digest('base64');

const headers = {
    'Authorization': 'application ' + APPLICATION_KEY + ':' + signature,
    'Content-Type': 'application/json; charset=utf-8',
    'x-timestamp': timeStampISO
};

axios.post(SINCH_URL, payload, { headers })
    .then(response =>
        console.log(response.data)
    ).catch(error =>
    console.error('There was an error!', error)
);
