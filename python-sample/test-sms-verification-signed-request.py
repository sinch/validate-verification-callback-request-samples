import hashlib
import hmac
import base64
from datetime import datetime, timezone
import requests
import json

# The key from one of your Verification Apps, found here https://dashboard.sinch.com/verification/apps
applicationKey = "<REPLACE_WITH_APP_KEY>"

# The secret from the Verification App that uses the key above, found here https://dashboard.sinch.com/verification/apps
applicationSecret = "<REPLACE_WITH_APP_SECRET>"

# The number that will receive the SMS. Test accounts are limited to verified numbers.
# The number must be in E.164 Format, e.g. Netherlands 0639111222 -> +31639111222
toNumber = '<REPLACE_WITH_YOUR_NUMBER>'

sinchVerificationUrl = "https://verificationapi-v1.sinch.com/verification/v1/verifications"

smsRequest = {
    "identity": {
        "type": "number",
        "endpoint": toNumber
    },
    "method": "sms"
}

encodedRequest = json.dumps(smsRequest).encode()
md5RequestHash = hashlib.md5(encodedRequest)
md5HashBase64EncodedRequest = base64.b64encode(md5RequestHash.digest())

httpVerb = 'POST'
requestContentType = 'application/json; charset=UTF-8'
timeNow = datetime.now(timezone.utc).isoformat()
requestTimestamp = "x-timestamp:" + timeNow
requestUriPath = '/verification/v1/verifications'

stringToSign = (httpVerb + "\n"
                + md5HashBase64EncodedRequest.decode() + "\n"
                + requestContentType + "\n"
                + requestTimestamp + "\n"
                + requestUriPath)

b64DecodedApplicationSecret = base64.b64decode(applicationSecret)

authorizationSignature = base64.b64encode(
    hmac.new(
        b64DecodedApplicationSecret,
        stringToSign.encode(),
        hashlib.sha256
    ).digest()
).decode()

headers = {
    "content-type": requestContentType,
    "authorization": f"application {applicationKey}:{authorizationSignature}",
    "x-timestamp": timeNow
}

response = requests.post(sinchVerificationUrl, json=smsRequest, headers=headers)

data = response.json()
print(data)
