using System.Net;
using System.Security.Cryptography;
using System.Text;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.

builder.Services.AddControllers();

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseRouting();

/*
    The key from one of your Verification Apps, found here https://dashboard.sinch.com/verification/apps
*/
string applicationKey = "<REPLACE_WITH_APP_KEY>";

/*
    The secret from the Verification App that uses the key above, found here https://dashboard.sinch.com/verification/apps
*/
string applicationSecret = "<REPLACE_WITH_APP_SECRET>";

app.UseEndpoints(endpoints => 
{
    app.MapPost("/api/verification/events", async context =>
    {   
        string calculatedSignature;

        string? authorization = context.Request.Headers.TryGetValue("authorization", out var authValue) ? authValue.First() : null;
        string[]? authSplit = authorization?.Split(' ', ':');
        string callbackKey = authSplit[1];
        string callbackSignature = authSplit[2];

        if (!applicationKey.Equals(callbackKey, StringComparison.Ordinal))
        {
            Console.WriteLine("The keys do not match, the HTTP request did not originate from Sinch!");
            context.Response.StatusCode = (int) HttpStatusCode.Forbidden;
            return;
        }

        var requestBody = context.Request.Body;
        string? requestTimestamp = context.Request.Headers.TryGetValue("x-timestamp", out var timeValue) ? timeValue.First() : null;
        string? requestUriPath = context.Request.Path.Value;
        string? requestContentType = context.Request.ContentType;
        string requestMethod = context.Request.Method;

        var stringToSign = new StringBuilder();

        stringToSign.Append(requestMethod);
        stringToSign.AppendLine();

        using (var md5 = MD5.Create())
		{
            stringToSign.Append(
                Convert.ToBase64String(
                    await md5.ComputeHashAsync(requestBody)
                )
            );
        }
        stringToSign.AppendLine();

        stringToSign.Append(requestContentType);
        stringToSign.AppendLine();

        stringToSign.Append("x-timestamp:");
        stringToSign.Append(requestTimestamp);
        stringToSign.AppendLine();

        stringToSign.Append(requestUriPath);

        using (var sha = new HMACSHA256(Convert.FromBase64String(applicationSecret)))
        {
            calculatedSignature = Convert.ToBase64String(
                sha.ComputeHash(Encoding.UTF8.GetBytes(stringToSign.ToString()))
            );
        }

        if (!calculatedSignature.Equals(callbackSignature, StringComparison.Ordinal))
        {
            Console.WriteLine("The hashes do not match, the HTTP request did not originate from Sinch!");
            context.Response.StatusCode = (int) HttpStatusCode.Forbidden;
            return;
        } 

        Console.WriteLine("Verification Callback validation was succesful, the hashes match!");

        // Continue processing the data...
        
        context.Response.StatusCode = (int) HttpStatusCode.OK;
        await context.Response.WriteAsJsonAsync(new {
                action = "allow" // or "deny"
        });
    });
});

app.Run();
