# Validate Callback Request event from Sinch Verification using dotnet

This project validates a Verification Event callback received from the Sinch platform when using the [Sinch Verification](https://dashboard.sinch.com/verification/overview) product with callbacks enabled.

## Requirements

- dotnet 6.*
- ngrok

## Install

- replace the required values in the `./VerificationEventHandler/Program.cs` file
- run the server using `dotnet watch run --project VerificationEventHandler --urls=http://localhost:8000`
- start ngrok `ngrok http 8000`
  - copy the ngrok url to the Verification App that you will receive Verification Events from to your [Sinch Dashboard](https://dashboard.sinch.com/verification/apps)
  - make sure to append the following URI at the end of the URL, `/api/verification/events`
  - example `https://df6a-143-177-206-33.ngrok.io/api/verification/events`
- test using the SMS PIN Verification script found in the project
  - replace the required values in the `./SMSVerificationTestScript/Program.cs` file
  - `dotnet run --project SMSVerificationTestScript`
  - by default, the request authenticates by application signing the HTTP request. To switch to basic authentication, comment `line 34` and uncomment `line 40`.
