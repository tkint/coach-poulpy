---
module: http4k-connect-amazon-cognitoidentity
license: Apache-2.0
---

# http4k-connect-amazon-cognitoidentity Reference

Amazon Cognito Identity client — exchange an identity pool for an identity, then exchange
that identity for temporary AWS credentials.

## Client

```kotlin
val cognitoIdentity = CognitoIdentity.Http(
    region = Region.of("us-east-1"),
    credentialsProvider = { AwsCredentials("accessKeyId", "secretKey") },
    http = JavaHttpClient()   // optional
)

// or from environment
val cognitoIdentity = CognitoIdentity.Http(Environment.ENV)
```

The endpoint is derived from the region (`https://cognito-identity.<region>.amazonaws.com`)
unless `overrideEndpoint` is passed.

## Get an identity, then credentials for it

```kotlin
val identityId = cognitoIdentity.getId(
    IdentityPoolId.of("us-east-1:12345678-1234-1234-1234-123456789012")
).successValue().IdentityId

val credentials = cognitoIdentity.getCredentialsForIdentity(identityId).successValue()

credentials.Credentials.AccessKeyId
credentials.Credentials.asHttp4k()   // -> org.http4k.aws.AwsCredentials for signing further requests
```

`getId` also accepts an optional `AccountId` and a `Logins` map (identity provider name to
token) for authenticated (rather than anonymous) identities.

## Gotchas

- Uses `application/x-amz-json-1.0` protocol with `X-Amz-Target` header
  (`AWSCognitoIdentityService.GetId` / `AWSCognitoIdentityService.GetCredentialsForIdentity`)
- `IdentityId` and `IdentityPoolId` are both validated against `[\w-]+:[0-9a-f-]+` — a bare
  UUID without the `<region>:` prefix is rejected with `IllegalArgumentException`
- Calling `getId` twice with the same pool and the same `Logins` returns the same `IdentityId`
  rather than minting a new one
- A different `Logins` map for the same pool mints a distinct identity
- `getCredentialsForIdentity` for an identity that doesn't exist fails rather than succeeding
  with empty credentials
- `TemporaryCredentials.asHttp4k()` converts straight to `org.http4k.aws.AwsCredentials`, ready
  to feed into another signed client
- Use `http4k-connect-amazon-cognitoidentity-fake` for testing
