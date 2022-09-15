# Validate Callback Request event from Sinch Verification using python3

This project validates a Verification Event callback received from the Sinch platform when using the [Sinch Verification](https://dashboard.sinch.com/verification/overview) product with callbacks enabled.

## Requirements

- [python3](https://www.python.org/downloads/)
- [pipenv](https://www.activestate.com/blog/how-to-manage-dependencies-in-python/)
- [ngrok](https://ngrok.com)

## Install

- ensure that `pipenv` is installed, if not run `pip install pipenv`
- start a virtual environment by running `pipenv ENV` in the current directory
- install all the required dependencies by running `pipenv install`
- replace the required values in the `./app.py` file
- run the server using `flask run --port 8000`
- start ngrok `ngrok http 8000`
  - copy the ngrok url to the Verification App that you will receive Verification Events from to your [Sinch Dashboard](https://dashboard.sinch.com/verification/apps)
  - make sure to append the following URI at the end of the URL, `/api/verification/events`
  - example `https://df6a-143-177-206-33.ngrok.io/api/verification/events`
- test using the SMS PIN Verification script found in the project
  - replace the required values in the `./test-sms-verification-signed-request.py` file
  - `python3 test-sms-verification-signed-request.py`
  - the alternative is to use the other script file that makes a request using basic-auth, `python3 test-sms-verification-basic-auth.py` 
