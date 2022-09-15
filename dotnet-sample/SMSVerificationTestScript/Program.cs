using System.Globalization;
using System.Security.Cryptography;
using System.Text;
using System.Text.Json;
using static System.Net.Mime.MediaTypeNames;

class Program
{
    /*
        The number that will receive the SMS. Test accounts are limited to verified numbers.
        The number must be in E.164 Format, e.g. Netherlands 0639111222 -> +31639111222
    */ 
    private const string _to = "<REPLACE_WITH_TO_NUMBER>";

    /*
        The key from one of your Verification Apps, found here https://dashboard.sinch.com/verification/apps
    */
    private const string _key = "<REPLACE_WITH_APP_KEY>";

    /*
        The secret from the Verification App that uses the key above, found here https://dashboard.sinch.com/verification/apps
    */
    private const string _secret = "<REPLACE_WITH_APP_SECRET>";

    private const string _sinchUrl = "https://verificationapi-v1.sinch.com/verification/v1/verifications";
    private const string _timeHeader = "x-timestamp";

    static async Task Main(string[] args)
    {
        /*
            Uncomment the line below trigger an SMS PIN Verification,
            using a request that uses application authentication, by signing the request.
        */
        await SendSMSPINWithSignedRequest();
        
        /*
            Uncomment the line below trigger an SMS PIN Verification,
            using a request that uses basic authentication.
        */
        // await SendSMSPinWithBasicAuth();
    }

    private static async Task SendSMSPinWithBasicAuth()
    {
        using (var _client = new HttpClient())
        {
            var requestBody = GetSMSVerificationRequestBody();
            var base64String = Convert.ToBase64String(Encoding.UTF8.GetBytes($"{_key}:{_secret}"));

            var requestMessage = new HttpRequestMessage(HttpMethod.Post, _sinchUrl);
            requestMessage.Headers.TryAddWithoutValidation("authorization", "basic " + base64String);
            requestMessage.Content = requestBody;

            var request = await _client.SendAsync(requestMessage);

            Console.WriteLine(request.StatusCode + " " + request.ReasonPhrase);
            Console.WriteLine(await request.Content.ReadAsStringAsync());

            request.EnsureSuccessStatusCode();
        }
    }

    private static async Task SendSMSPINWithSignedRequest() 
    {
        using (var _client = new HttpClient())
        {
            var requestBody = GetSMSVerificationRequestBody();
            var timestamp = DateTime.UtcNow.ToString("O", CultureInfo.InvariantCulture);

            var b64decodedApplicationSecret = Convert.FromBase64String(_secret);
            var stringToSign = await BuildStringToSignAsync(requestBody, timestamp);

            var authAuthAppValue = _key + ":" + GetSignature(b64decodedApplicationSecret, stringToSign);

            var requestMessage = new HttpRequestMessage(HttpMethod.Post, _sinchUrl);
            requestMessage.Headers.TryAddWithoutValidation("authorization", "application " + authAuthAppValue);
            requestMessage.Headers.Add(_timeHeader, timestamp);
            requestMessage.Content = requestBody;

            var request = await _client.SendAsync(requestMessage);

            Console.WriteLine(request.StatusCode + " " + request.ReasonPhrase);
            Console.WriteLine(await request.Content.ReadAsStringAsync());

            request.EnsureSuccessStatusCode();            
        }
    }

    private static string GetSignature(byte[] secret, string stringToSign)
    {
        using (var sha = new HMACSHA256(secret))
        {
            return Convert.ToBase64String(
                sha.ComputeHash(Encoding.UTF8.GetBytes(stringToSign))
            );
        }
    }

    private static async Task<string> BuildStringToSignAsync(StringContent requestBody, string timestamp)
    {
        var sb = new StringBuilder();

        sb.Append("POST");
        sb.AppendLine();

        using (var md5 = MD5.Create())
		{
            sb.Append(Convert.ToBase64String(md5.ComputeHash(await requestBody.ReadAsByteArrayAsync().ConfigureAwait(false))));
        }
        sb.AppendLine();

        sb.Append("application/json; charset=utf-8");
        sb.AppendLine();

        sb.Append(_timeHeader);
        sb.Append(":");
        sb.Append(timestamp);
        sb.AppendLine();

        sb.Append("/verification/v1/verifications");

        return sb.ToString();
    }

    public static StringContent GetSMSVerificationRequestBody()
    {
        var myData = new
        {
            identity = new {
                type = "number",
                endpoint = _to
            },
            method = "sms",
        };

        return new StringContent(
            JsonSerializer.Serialize(myData),
            Encoding.UTF8,
            Application.Json
        );
    }
}
