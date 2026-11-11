---
license: http4k Commercial
module: http4k-ai-mcp-x402
---

# http4k-ai-mcp-x402 Reference

X402 payment protocol integration for MCP servers — charge for tool calls and general MCP usage via a standardised X402 facilitator.

## Dependencies

Requires `http4k-ai-mcp-sdk` and `http4k-connect-x402`. For testing, add `http4k-connect-x402-fake` and `http4k-ai-mcp-testing`.

## Payment Check

```kotlin
sealed interface PaymentCheck {
    data class Required(val requirements: List<PaymentRequirements>) : PaymentCheck
    data object Free : PaymentCheck
}
```

Use `PaymentCheck` to decide per-request whether payment is needed.

## Settlement Mode

Controls when the X402 settlement call happens relative to tool execution:

```kotlin
enum class SettlementMode {
    SettleBefore,  // Verify → Settle → run tool (default). Tool never runs if settlement fails.
    SettleAfter    // Verify → run tool → Settle. Tool effect happens even if settle later fails.
}
```

Use `SettleAfter` when the tool operation is idempotent and you want to avoid blocking on settlement before execution.

## X402ToolFilter (Tool-Level Payments)

Wraps individual tools with payment verification and settlement. Returns structured `PaymentRequired` errors when payment is missing or invalid, and includes settlement details in the response meta on success.

```kotlin
val requirements = PaymentRequirements(
    scheme = PaymentScheme.of("exact"),
    network = PaymentNetwork.of("base-sepolia"),
    asset = AssetAddress.of("0xUSDC"),
    amount = PaymentAmount.of("100"),
    payTo = WalletAddress.of("0xmerchant"),
    maxTimeoutSeconds = 60
)

val facilitator = X402Facilitator.Http(Uri.of("https://facilitator.example.com"), http)

// Default: SettleBefore — settle before running the tool
val paidTool = X402ToolFilter(facilitator) { PaymentCheck.Required(listOf(requirements)) }
    .then(Tool("premium_data", "get premium data") bind { Ok(listOf(Content.Text("Here is your data!"))) })

// SettleAfter — run tool first, settle after
val paidTool = X402ToolFilter(facilitator, mode = SettlementMode.SettleAfter) { PaymentCheck.Required(listOf(requirements)) }
    .then(Tool("data", "get data") bind { Ok(listOf(Content.Text("result"))) })

val server = mcp(
    ServerMetaData(McpEntity.of("paid-server"), Version.of("1.0.0")),
    NoMcpSecurity,
    paidTool
)
```

## X402McpFilter (Protocol-Level Payments)

Operates at the MCP protocol level via `McpFilters`. Throws `McpException` with code 402 on payment failure instead of returning structured tool errors.

```kotlin
// Default: SettleBefore
val filter = McpFilters.X402PaymentRequired(facilitator) { request: McpRequest ->
    PaymentCheck.Required(listOf(requirements))
}

// Explicit mode
val filter = McpFilters.X402PaymentRequired(facilitator, mode = SettlementMode.SettleAfter) { request: McpRequest ->
    PaymentCheck.Required(listOf(requirements))
}
```

## Meta Keys for Payment Data

Payment data flows through MCP `_meta` fields using the lens system:

```kotlin
// Create lenses
val paymentLens = MetaKey.x402PaymentPayload().toLens()
val settlementLens = MetaKey.x402Settled().toLens()

// Inject payment into tool request meta
val request = ToolRequest(meta = Meta(paymentLens of payload))

// Extract settlement from tool response meta
val settled: SettledResponse? = settlementLens(response.meta)
```

### Meta Field Keys

| Key | Type | Direction |
|-----|------|-----------|
| `x402/payment` | `PaymentPayload` | Client → Server (in tool call `_meta`) |
| `x402/payment-response` | `SettledResponse` | Server → Client (in tool response `_meta`) |

## Payment Flow (Tool-Level)

**SettleBefore (default):**
1. Client calls tool without payment → `ToolResponse.Error` with `PaymentRequired` in `structuredContent`
2. Client extracts requirements, signs payment, retries with payment in `_meta`
3. Server matches scheme/network, verifies via facilitator, then settles
4. If settlement fails, tool is never invoked — returns payment error
5. Server executes tool and returns `ToolResponse.Ok` with `SettledResponse` in `_meta`

**SettleAfter:**
Steps 1–3 same; step 4 runs the tool first, then settles. The tool effect occurs even if settlement later fails.

## Error Responses

`X402ToolFilter` returns `ToolResponse.Error` with:
- `content`: JSON string of `PaymentRequired`
- `structuredContent`: JSON object of `PaymentRequired` (for programmatic access)

`X402McpFilter` throws `McpException(ErrorMessage(402, message))`.

## Testing

```kotlin
val fake = FakeX402Facilitator()

val paidTool = X402ToolFilter(fake.client()) { PaymentCheck.Required(listOf(requirements)) }
    .then(Tool("data", "get data") bind { Ok(listOf(Content.Text("result"))) })

val server = mcp(metadata, NoMcpSecurity, paidTool)

server.testMcpClient(Request(POST, "/mcp")).use { client ->
    // Without payment — returns error with PaymentRequired
    val error = client.tools().call(ToolName.of("data"), ToolRequest())

    // With payment — succeeds
    val paymentLens = MetaKey.x402PaymentPayload().toLens()
    val result = client.tools().call(
        ToolName.of("data"),
        ToolRequest(meta = Meta(paymentLens of payment))
    )
}
```

## Gotchas

- **Scheme/network matching**: Payment is matched to requirements by `scheme` + `network` pair. If no requirement matches the payment's scheme/network, a "Unsupported payment scheme/network" error is returned.
- **Settlement mode**: `SettleBefore` (default) — settlement happens before tool execution; if settlement fails the tool never runs. `SettleAfter` — tool runs first, settlement happens after; the tool's effect is not rolled back if settlement fails.
- **ToolFilter vs McpFilter**: `X402ToolFilter` returns structured `ToolResponse.Error` (tool-level). `McpFilters.X402PaymentRequired` throws `McpException` (protocol-level). Use `X402ToolFilter` for per-tool payment gating.
- **Meta lens creation**: Use `MetaKey.x402PaymentPayload()` and `MetaKey.x402Settled()` — these return specs that need `.toLens()` before use.
- **Requires Moshi**: Payment serialization uses `X402Moshi`. Ensure `http4k-format-moshi` is on the classpath.
