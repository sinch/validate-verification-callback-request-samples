# Validate Callback Request event from Sinch Verification using SpringBoot

This project validates a Verification Event callback received from the Sinch platform when using the [Sinch Verification](https://dashboard.sinch.com/verification/overview) product with callbacks enabled.

## Requirements

- [OpenJDK 17+](https://openjdk.org)
- [ngrok](http://ngrok.com)

## Install

- replace the required values in the - `./application/src/main/java/verification/eventhandler/App.java` file
- run `./gradlew build` to build the project with all the required dependencies
- run the server using `./gradlew :application:bootRun`
- start ngrok `ngrok http 8000`
    - copy the ngrok url to the Verification App that you will receive Verification Events from to your [Sinch Dashboard](https://dashboard.sinch.com/verification/apps)
    - make sure to append the following URI at the end of the URL, `/api/verification/events`
    - example `https://df6a-143-177-206-33.ngrok.io/api/verification/events`
- test using the SMS PIN Verification scripts found in the project
    - replace the required values in the `./library/src/main/java/verification/smsverification/App.java` file
    - run the command `./gradlew :library:run`
    - by default, the request authenticates by application signing the HTTP request. To switch to basic authentication, comment `line 50` and uncomment `line 44`.
