import requests

# The key from one of your Verification Apps, found here https://dashboard.sinch.com/verification/apps
applicationKey = "<REPLACE_WITH_APP_KEY>"

# The secret from the Verification App that uses the key above, found here https://dashboard.sinch.com/verification/apps
applicationSecret = "<REPLACE_WITH_APP_SECRET>"

# The number that will receive the SMS. Test accounts are limited to verified numbers.
# The number must be in E.164 Format, e.g. Netherlands 0639111222 -> +31639111222
toNumber = '<REPLACE_WITH_YOUR_NUMBER>'

sinchVerificationUrl = "https://verificationapi-v1.sinch.com/verification/v1/verifications"

payload = {
    "identity": {
        "type": "number",
        "endpoint": toNumber
    },
    "method": "sms"
}

headers = {"Content-Type": "application/json"}

response = requests.post(sinchVerificationUrl, json=payload, headers=headers, auth=(applicationKey, applicationSecret))

data = response.json()
print(data)
