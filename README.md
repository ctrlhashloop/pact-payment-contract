# Pact contract testing: wallet service and payments API

![contract-tests](https://github.com/ctrlhashloop/pact-payment-contract/actions/workflows/ci.yml/badge.svg).

Consumer-driven contract testing with [Pact JVM](https://docs.pact.io/implementation_guides/jvm), using a payments domain (money transfers between accounts).

- **Consumer:** `wallet-service`, a client that creates and fetches transfers
- **Provider:** `payments-api`, a Spring Boot REST API

## How it works

1. The consumer tests run the real client against a Pact mock server and write a contract to `consumer/target/pacts`.
2. The provider test starts the real API and replays every interaction in that contract against it.
3. Provider states (`@State`) put the API in the situation each interaction assumes, such as "account has insufficient funds".

## Interactions covered

| Interaction | Provider state | Expected |
|---|---|---|
| `POST /transfers` | source account has sufficient funds | `201`, transfer `COMPLETED` |
| `POST /transfers` | source account has insufficient funds | `422`, `INSUFFICIENT_FUNDS` |
| `GET /transfers/{id}` | transfer exists | `200`, transfer details |
| `GET /transfers/{id}` | transfer does not exist | `404` |

## Run it

Requires Java 21 and Maven 3.9+.

```
mvn verify
```

The consumer module builds first and generates the pact, then the provider verifies it.

## Try breaking the contract

1. Run `mvn verify` and confirm it passes.
2. In the provider's `Transfer` record, rename `status` to `state`.
3. Run `mvn verify` again. Verification fails and names the interaction that broke.

## Design notes

- Matchers check the *shape* of values that vary (`id`, `amount`) instead of hard-coded examples.
- The consumer ignores unknown response fields, so the provider can add fields safely.
- The contract only lists what the consumer actually uses.

## Next steps

- Publish pacts to a Pact Broker or PactFlow and add `can-i-deploy` to CI
- REST Assured tests for the provider API (schemas, boundaries, negative cases)
