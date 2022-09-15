import base64
import hashlib
import hmac
import http
import re

from flask import Flask, jsonify, make_response, request

from logging.config import dictConfig

# Enabled logging to stdout
dictConfig({
    'version': 1,
    'formatters': {'default': {
        'format': '[%(asctime)s] %(levelname)s in %(module)s: %(message)s',
    }},
    'handlers': {'wsgi': {
        'class': 'logging.StreamHandler',
        'stream': 'ext://sys.stdout',
        'formatter': 'default'
    }},
    'root': {
        'level': 'INFO',
        'handlers': ['wsgi']
    }
})

app = Flask(__name__)

# The key from one of your Verification Apps, found here https://dashboard.sinch.com/verification/apps
applicationKey = "<REPLACE_WITH_APP_KEY>"

# The secret from the Verification App that uses the key above, found here https://dashboard.sinch.com/verification/apps
applicationSecret = "<REPLACE_WITH_APP_SECRET>"


@app.route('/api/verification/events', methods=['POST'])
def handleEvent():
    requestAuthHeader = request.headers.get('authorization')

    authSplit = re.split('[ :]', requestAuthHeader)
    callbackKey = authSplit[1]
    callbackSignature = authSplit[2]

    if callbackKey != applicationKey:
        app.logger.info("The keys do not match, the HTTP request did not originate from Sinch!")
        return make_response(jsonify(''), http.HTTPStatus.FORBIDDEN)

    requestMethod = request.method
    requestContentType = request.content_type
    requestTimestamp = request.headers.get('x-timestamp')
    requestUriPath = request.path

    md5EncodedRequestBody = hashlib.md5(request.get_data())
    md5Base64EncodedRequestBody = base64.b64encode(md5EncodedRequestBody.digest())

    stringToSign = (requestMethod + "\n"
                    + md5Base64EncodedRequestBody.decode() + "\n"
                    + requestContentType + "\n"
                    + "x-timestamp:" + requestTimestamp + "\n"
                    + requestUriPath)

    b64DecodedApplicationSecret = base64.b64decode(applicationSecret)

    calculatedSignature = base64.b64encode(
        hmac.new(b64DecodedApplicationSecret, stringToSign.encode(), hashlib.sha256).digest()).decode()

    if calculatedSignature != callbackSignature:
        app.logger.info("The hashes do not match, the HTTP request did not originate from Sinch!")
        return make_response(jsonify(''), http.HTTPStatus.FORBIDDEN)

    app.logger.info("Verification Callback validation was successful, the hashes match!")

    # Continue processing the data...

    return make_response(
        jsonify({"action": "allow"}),  # or "deny"
        http.HTTPStatus.OK
    )
