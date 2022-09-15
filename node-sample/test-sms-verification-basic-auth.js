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
    The number that will receive the SMS. Test accounts are limited to verified numbers.
    The number must be in E.164 Format, e.g. Netherlands 0639111222 -> +31639111222
*/
const TO_NUMBER = "<REPLACE_WITH_YOUR_NUMBER>";

const SINCH_URL = "https://verificationapi-v1.sinch.com/verification/v1/verifications";

const basicAuthentication = APPLICATION_KEY + ":" + APPLICATION_SECRET;

const payload = {
    identity: {
        type: 'number',
        endpoint: TO_NUMBER
    },
    method: 'sms'
};

const headers = {
    'Authorization': 'Basic ' + Buffer.from(basicAuthentication).toString('base64'),
    'Content-Type': 'application/json; charset=utf-8'
};

axios.post(SINCH_URL, payload, { headers })
    .then(response =>
        console.log(response.data)
    ).catch(error =>
    console.error('There was an error!', error)
);
