const crypto = require('crypto');
const express = require('express');
const app = express();
app.use(express.raw({ type: "*/*" }))
const port = 8000;

/*
    The key from one of your Verification Apps, found here https://dashboard.sinch.com/verification/apps
*/
const APPLICATION_KEY = "<REPLACE_WITH_APP_KEY>";

/*
    The secret from the Verification App that uses the key above, found here https://dashboard.sinch.com/verification/apps
*/
const APPLICATION_SECRET = "<REPLACE_WITH_APP_SECRET>";

app.post('/api/verification/events', (req, res) => {
    const hash = crypto.createHash('md5');
    const authHeader = req.header('Authorization');
    let authValue = authHeader.split(/[ :]/);
    let callbackKey = authValue[1];
    let callbackSignature = authValue[2];

    if (callbackKey !== APPLICATION_KEY) {
        console.log("The keys do not match, the HTTP request did not originate from Sinch!");
        res.sendStatus(403);
        return;
    }

    const requestBody = req.body
    const requestMethod = req.method;
    const contentMD5 = hash.update(requestBody).digest('base64');
    const requestContentType = req.header('Content-Type');
    const requestTimestamp = req.header('x-timestamp');
    const requestPath = req.baseUrl + req.path;

    let stringToSign = requestMethod
        + '\n' + contentMD5
        + '\n' + requestContentType
        + '\n' + 'x-timestamp:'+ requestTimestamp
        + '\n' + requestPath;

    let hmac = crypto.createHmac('sha256', Buffer.from(APPLICATION_SECRET, 'base64'));
    hmac.update(stringToSign);
    let calculatedSignature = hmac.digest('base64');

    if (calculatedSignature !== callbackSignature) {
        console.log("The hashes do not match, the HTTP request did not originate from Sinch!");
        res.sendStatus(403);
        return;
    }

    console.log("Verification Callback validation was successful, the hashes match!");

    // Continue processing the data...

    res.status(200).send({ action: 'allow' }) // or 'deny'
})

app.listen(port, () => {
    console.log(`Listening on port ${port}, the app will start listening to Verification Callback Events from Sinch`)
})
