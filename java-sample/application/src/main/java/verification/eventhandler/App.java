package verification.eventhandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(App.class);
        app.setDefaultProperties(Map.of("server.port", "8000"));
        app.run(args);
    }

    @Controller
    static class VerificationEventCallbackController {
        Logger logger = LoggerFactory.getLogger(VerificationEventCallbackController.class);

        @PostMapping(value = "/api/verification/events")
        public ResponseEntity<String> handleVerificationEvent(RequestEntity<String> request) throws NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {

            /*
                The key from one of your Verification Apps, found here https://dashboard.sinch.com/verification/apps
            */
            var applicationKey = "<REPLACE_WITH_VERIF_APP_KEY>";

            /*
                The secret from the Verification App that uses the key above, found here https://dashboard.sinch.com/verification/apps
            */
            var applicationSecret = "<REPLACE_WITH_VERIF_APP_SECRET>";

            String authorization = request.getHeaders().get("authorization").stream().findFirst().get();
            String[] authSplit = authorization.split("[\\s:]");
            String callbackKey = authSplit[1];
            String callbackSignature = authSplit[2];

            if (!applicationKey.equals(callbackKey)) {
                logger.info("The keys do not match, the HTTP request did not originate from Sinch!");
                return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
            }

            var requestBody = request.getBody();
            var requestTimestamp = request.getHeaders().get("x-timestamp").stream().findFirst().get();
            var requestUriPath = request.getUrl().getPath();
            var requestContentType = request.getHeaders().get("content-type").stream().findFirst().get();
            var requestMethod = request.getMethod().name();

            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(requestBody.getBytes(StandardCharsets.UTF_8));
            var base64EncodedMd5EncodedPayload = Base64.getEncoder().encode(md.digest());

            var stringToSign = String.join(System.lineSeparator()
                    , requestMethod
                    , new String(base64EncodedMd5EncodedPayload, StandardCharsets.UTF_8)
                    , requestContentType
                    , "x-timestamp:" + requestTimestamp
                    , requestUriPath
            );

            var b64DecodedApplicationSecret = Base64.getDecoder().decode(applicationSecret);

            String hmacSHA256 = "HmacSHA256";
            SecretKeySpec secretKeySpec = new SecretKeySpec(b64DecodedApplicationSecret, hmacSHA256);
            Mac mac = Mac.getInstance(hmacSHA256);
            mac.init(secretKeySpec);
            byte[] hmacSha256 = mac.doFinal(stringToSign.getBytes());

            var calculatedSignature = new String(
                    Base64.getEncoder().encode(hmacSha256),
                    StandardCharsets.UTF_8
            );

            if (!calculatedSignature.equals(callbackSignature)) {
                logger.info("The hashes do not match, the HTTP request did not originate from Sinch!");
                return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
            }

            logger.info("Verification Callback validation was successful, the hashes match!");

            // Continue processing the data...

            return new ResponseEntity<>(
                    new ObjectMapper().writeValueAsString(
                            Collections.singletonMap("action", "allow") // or "deny"
                    ),
                    HttpStatus.OK
            );
        }
    }
}
