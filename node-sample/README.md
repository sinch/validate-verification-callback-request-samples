# Validate Callback Request event from Sinch Verification using Node.js and Express

This project validates a Verification Event callback received from the Sinch platform when using the [Sinch Verification](https://dashboard.sinch.com/verification/overview) product with callbacks enabled.

## Requirements

- [node v16.*](https://nodejs.org/en/)
- [ngrok](https://ngrok.com)

## Install

- replace the required values in the `./index.js` file
- run `npm install` to install the required dependencies
- run the server using `node index.js`
- start ngrok `ngrok http 8000`
    - copy the ngrok url to the Verification App that you will receive Verification Events from to your [Sinch Dashboard](https://dashboard.sinch.com/verification/apps)
    - make sure to append the following URI at the end of the URL, `/api/verification/events`
    - example `https://df6a-143-177-206-33.ngrok.io/api/verification/events`
- test using the SMS PIN Verification script found in the project
    - replace the required values in the `./test-sms-verification-basic-auth.js` file
    - run `node test-sms-verification-basic-auth.js` to trigger an sms verification pin
    - alternatively, use make use of the `test-sms-verification-signed-request.js`, which signs the request instead
