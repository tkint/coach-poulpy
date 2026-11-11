---
module: http4k-connect-amazon-cognitoidentity-fake
license: Apache-2.0
---

# http4k-connect-amazon-cognitoidentity-fake Reference

In-memory fake Amazon Cognito Identity service for testing.

## Setup

```kotlin
val fakeCognitoIdentity = FakeCognitoIdentity()
val client = fakeCognitoIdentity.client()
```

## Custom Configuration

```kotlin
val fakeCognitoIdentity = FakeCognitoIdentity(
    identities = Storage.InMemory(),
    region = Region.of("ldn-north-1"),
    clock = Clock.systemUTC(),
    expiry = Duration.ofHours(1)
)
```

## Test Contracts

```kotlin
class FakeCognitoIdentityTest : CognitoIdentityContract, FakeAwsContract {
    override val http = FakeCognitoIdentity()
    override val identityPoolId = IdentityPoolId.of("ldn-north-1:12345678-1234-1234-1234-123456789012")
}
```

## Inspecting Stored State

```kotlin
val stored = fakeCognitoIdentity.identities[identityId.value]!!

assertThat(stored.logins, equalTo(mapOf("provider" to "token")))
```

## Chaos Testing

```kotlin
fakeCognitoIdentity.returnStatus(Status.SERVICE_UNAVAILABLE)
fakeCognitoIdentity.behave()
```

## Gotchas

- Extends `ChaoticHttpHandler`
- Identities are keyed by pool + the exact `Logins` map: the same pool with different (or
  absent) logins returns a distinct `IdentityId`, but repeating the same pool/logins pair
  returns the previously-minted one instead of creating a new one
- Credentials are always synthesized (`ASIAFAKEACCESSKEY` / `secret` / `token`) with an
  `Expiration` `expiry` (default one hour) after the fake's `clock` — no real STS call is made
- `getCredentialsForIdentity` for an `IdentityId` the fake has never issued returns a
  `ResourceNotFoundException` failure, not empty credentials
- State lives in a `Storage<StoredIdentity>` — pass your own `Storage.InMemory()` if a test
  needs to inspect or seed identities directly
