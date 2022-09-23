package verification.smsverification;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class App {
    /*
      The key from one of your Verification Apps, found here https://dashboard.sinch.com/verification/apps
    */
    private static final String applicationKey = "<REPLACE_WITH_VERIF_APP_KEY>";

    /*
      The secret from the Verification App that uses the key above, found here https://dashboard.sinch.com/verification/apps
    */
    private static final String applicationSecret = "<REPLACE_WITH_VERIF_APP_SECRET>";

    /*
          The number that will receive the SMS. Test accounts are limited to verified numbers.
          The number must be in E.164 Format, e.g. Netherlands 0639111222 -> +31639111222
    */
    private static final String toNumber = "<REPLACE_WITH_TO_NUMBER>";

    private static final String SINCH_URL = "https://verificationapi-v1.sinch.com/verification/v1/verifications";
    public static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
    public static final String X_TIMESTAMP = "x-timestamp";
    public static final String HMAC_SHA_256 = "HmacSHA256";

    public static void main(String[] args) throws Exception {
        /*
            Uncomment the line below trigger an SMS PIN Verification,
            using a request that uses basic authentication.
        */
        // HttpResponse<String> response = sendSMSPinWithBasicAuth();

        /*
            Uncomment the line below trigger an SMS PIN Verification,
            using a request that uses application authentication, by signing the request.
        */
        HttpResponse<String> response = sendSMSPinWithSignedRequest();

        System.out.println(response.body());
    }

    private static HttpResponse<String> sendSMSPinWithSignedRequest() throws NoSuchAlgorithmException, InvalidKeyException, IOException, InterruptedException {
        var payload = getSMSVerificationRequestBody();
        var timeNow = getUTCTimeNow();
        var b64DecodedApplicationSecret = Base64.getDecoder().decode(applicationSecret);
        String generatedSignature = getSignature(b64DecodedApplicationSecret, getStringToSign(payload, timeNow));

        var request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .uri(URI.create(SINCH_URL))
                .header("content-type", JSON_CONTENT_TYPE)
                .header("authorization", "application " + applicationKey + ":" + generatedSignature)
                .header(X_TIMESTAMP, timeNow)
                .build();

        return HttpClient
                .newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static String getUTCTimeNow() {
        return ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
    }

    private static String getSignature(byte[] b64DecodedApplicationSecret, String stringToSign) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(b64DecodedApplicationSecret, HMAC_SHA_256);
        Mac mac = Mac.getInstance(HMAC_SHA_256);
        mac.init(secretKeySpec);
        byte[] hmacSha256 = mac.doFinal(stringToSign.getBytes());

        return new String(Base64.getEncoder().encode(hmacSha256), StandardCharsets.UTF_8);
    }

    private static String getStringToSign(String requestBody, String timeNow) throws NoSuchAlgorithmException {
        var httpVerb = "POST";
        var requestTimeStamp = X_TIMESTAMP + ":" + timeNow;
        var requestUriPath = "/verification/v1/verifications";

        byte[] encodedPayload = requestBody.getBytes(StandardCharsets.UTF_8);

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(encodedPayload);
        var base64EncodedMd5EncodedPayload = Base64.getEncoder().encode(md.digest());

        return String.join(System.lineSeparator()
                , httpVerb
                , new String(base64EncodedMd5EncodedPayload, StandardCharsets.UTF_8)
                , JSON_CONTENT_TYPE
                , requestTimeStamp
                , requestUriPath
        );
    }

    private static HttpResponse<String> sendSMSPinWithBasicAuth() throws IOException, InterruptedException {
        var httpClient = HttpClient.newBuilder().build();

        var payload = getSMSVerificationRequestBody();

        var request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .uri(URI.create(SINCH_URL))
                .header("Content-Type", JSON_CONTENT_TYPE)
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((applicationKey + ":" + applicationSecret).getBytes()))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static String getSMSVerificationRequestBody() {
        return """
            {
                "identity": {
                    "type": "number",
                    "endpoint": "%s"
                },
                "method": "sms"
            }
            """.formatted(toNumber);
    }
}
