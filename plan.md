# Quote Module — Rebuild Plan

## Goal
Drop all existing `com.nifilili.quote` code and rebuild from scratch following CLAUDE.md patterns with event-driven cross-module communication (no direct order/offering repository injection).

## Architecture Decisions

### Cross-Module Communication (Key Change)
**Current**: Quote module directly injects `OrderRepository`, `OrderItemRepository`, `OrderStatusHistoryRepository`, `OfferingRepository` — violates Spring Modulith boundary rules.

**New**: Pure event-driven architecture:
1. Quote module publishes `QuoteAcceptedEvent` (with full quote + line items + delivery address data)
2. Order module listens → creates order → publishes `OrderCreatedFromQuoteEvent(quoteId, orderId)`
3. Quote module listens → records conversion in `quote_conversions` table
4. For offering validation: Quote module publishes `QuoteRequestValidationEvent` → Offering module responds (OR we accept offering details in the request DTO and validate via a lightweight check). **Pragmatic approach**: Accept `offeringId`, `businessId` in the request. Publish a `QuoteRequestCreatedEvent` that the offering module can consume for async validation/notification. The quote module trusts the client-provided IDs (the offering page already shows the correct data to the customer).

### Exception Handling
Create `InvalidQuoteStateException` in `core.exception` (matching `InvalidOrderStateException` pattern). Register in `GlobalExceptionHandler`.

---

## File Structure (all under `src/main/java/com/nifilili/quote/`)

### Domain (4 entities — same DB schema, cleaner code)
- `domain/QuoteRequestEntity.java` — renamed from `QuoteRequest` for consistency
- `domain/QuoteEntity.java` — renamed from `Quote`
- `domain/QuoteLineItemEntity.java` — renamed from `QuoteLineItem`
- `domain/QuoteConversionEntity.java` — renamed from `QuoteConversion`

### Repositories (4)
- `repository/QuoteRequestRepository.java`
- `repository/QuoteRepository.java`
- `repository/QuoteLineItemRepository.java`
- `repository/QuoteConversionRepository.java`

### Services — Interface + Impl (3 service pairs)
- `service/QuoteRequestService.java` + `service/impl/QuoteRequestServiceImpl.java`
- `service/QuoteService.java` + `service/impl/QuoteServiceImpl.java`
- `service/QuoteConversionService.java` + `service/impl/QuoteConversionServiceImpl.java`

### Event Listener (1 — listens for order creation confirmation)
- `service/impl/QuoteOrderEventListener.java`

### Controllers (split by role)
- `controller/publicapi/CustomerQuoteRequestController.java` — User creates/views quote requests, accepts/rejects quotes
- `controller/owner/BusinessQuoteController.java` — Business inbox, draft/send/revise quotes, decline requests
- `controller/admin/AdminQuoteController.java` — Admin oversight (list all, view details)

### DTOs — Request (4)
- `dto/request/CreateQuoteRequestRequest.java`
- `dto/request/CreateQuoteRequest.java`
- `dto/request/QuoteLineItemRequest.java`
- `dto/request/RejectQuoteRequest.java`

### DTOs — Response (4)
- `dto/response/QuoteRequestResponse.java`
- `dto/response/QuoteResponse.java`
- `dto/response/QuoteLineItemResponse.java`
- `dto/response/QuoteConversionResponse.java`

### Mappers (2 MapStruct mappers)
- `mapper/QuoteRequestMapper.java`
- `mapper/QuoteMapper.java`

### Events (6 — published by quote module)
- `events/QuoteRequestCreatedEvent.java` — notify business of new request
- `events/QuoteRequestDeclinedEvent.java` — notify customer
- `events/QuoteSentEvent.java` — notify customer of quote
- `events/QuoteAcceptedEvent.java` — **triggers order creation** (carries full data)
- `events/QuoteRejectedEvent.java` — notify business
- `events/QuoteExpiredEvent.java` — for audit/notifications

### Events published by Order module (1 new file in order module)
- `order/events/OrderCreatedFromQuoteEvent.java` — consumed by quote module to record conversion

### Validation (1)
- `validation/QuoteValidator.java` — shared validation logic (status checks, expiry checks)

---

## Core Module Changes

### New Exception
**`core/exception/InvalidQuoteStateException.java`**
```java
public class InvalidQuoteStateException extends RuntimeException {
    public InvalidQuoteStateException(String message) { super(message); }
}
```

### GlobalExceptionHandler — Add Handler
```java
@ExceptionHandler
public ResponseEntity<ErrorDto> handleInvalidQuoteState(InvalidQuoteStateException ex) {
    log.warn("Invalid quote state: {}", ex.getMessage());
    return new ResponseEntity<>(new ErrorDto(HttpStatus.BAD_REQUEST.value(), ex.getMessage()), HttpStatus.BAD_REQUEST);
}
```

### SwaggerConstants — Add Quote Constants
```java
public static final String QUOTE_1 = "6.01 - Quote Requests [User]";
public static final String QUOTE_2 = "6.02 - Quote Management [Owner]";
public static final String QUOTE_3 = "6.03 - Quotes [Admin]";
```

---

## Service Method Design

### QuoteRequestService
| Method | Description |
|--------|-------------|
| `createRequest(CreateQuoteRequestRequest)` | Customer submits quote request. Validates required fields, generates QREQ-xxx number, sets PENDING + 30-day expiry, publishes `QuoteRequestCreatedEvent` |
| `getRequest(Long requestId)` | Get single request with auth check |
| `listMyRequests()` | Customer's own requests (by userId) |
| `listBusinessRequests(Long businessId)` | Business inbox |
| `declineRequest(Long requestId, String reason)` | Business declines PENDING request → REJECTED, publishes `QuoteRequestDeclinedEvent` |
| `cancelRequest(Long requestId)` | Customer cancels own PENDING request → CANCELLED |
| `expireRequests()` | Batch: PENDING + expired → EXPIRED, publishes `QuoteExpiredEvent` per request |

### QuoteService
| Method | Description |
|--------|-------------|
| `createDraft(Long requestId, CreateQuoteRequest)` | Business creates DRAFT quote. Validates request isn't terminal, no existing DRAFT/SENT. Generates QTE-xxx number, saves line items |
| `sendQuote(Long quoteId)` | DRAFT → SENT. Sets sentAt, updates request to QUOTED, publishes `QuoteSentEvent` |
| `reviseQuote(Long quoteId, CreateQuoteRequest)` | Old SENT → REVISED, creates new DRAFT with parentQuoteId, publishes `QuoteRevisedEvent` (notifies customer) |
| `acceptQuote(Long quoteId)` | Customer accepts SENT quote. Validates not expired, sets ACCEPTED + acceptedAt, request → ACCEPTED, publishes `QuoteAcceptedEvent` |
| `rejectQuote(Long quoteId, RejectQuoteRequest)` | Customer rejects SENT quote → REJECTED, request → REJECTED, publishes `QuoteRejectedEvent` |
| `getQuote(Long quoteId)` | Get single quote with line items |
| `listQuotesForRequest(Long requestId)` | All versions for a request |
| `getLatestQuote(Long requestId)` | Most recent quote |
| `expireQuotes()` | Batch: SENT + validUntil passed → EXPIRED |

### QuoteConversionService
| Method | Description |
|--------|-------------|
| `recordConversion(Long quoteId, Long orderId, Long convertedBy)` | Records quote→order conversion. Called by event listener when `OrderCreatedFromQuoteEvent` received |
| `getConversion(Long quoteId)` | Get conversion record for a quote |

---

## Event Flow (Quote Acceptance → Order)

```
1. Customer calls POST /api/v1/quote-requests/quotes/{quoteId}/accept
2. QuoteServiceImpl.acceptQuote():
   - Validates quote is SENT and not expired
   - Sets quote.status = ACCEPTED, quote.acceptedAt = now
   - Sets request.status = ACCEPTED
   - Publishes QuoteAcceptedEvent (contains: quoteId, requestId, userId, businessId,
     offeringId, serviceDetails, totalAmount, currency, lineItems[], deliveryAddress,
     quoteNumber)
3. Order module's QuoteAcceptedEventListener (new file in order module):
   - Creates OrderEntity with sourceQuoteId, sourceRequestId, source="quote"
   - Creates OrderItemEntity for each line item
   - Creates OrderStatusHistoryEntity
   - Publishes OrderCreatedFromQuoteEvent(quoteId, orderId, orderNumber)
4. Quote module's QuoteOrderEventListener:
   - Listens for OrderCreatedFromQuoteEvent
   - Calls QuoteConversionService.recordConversion()
```

---

## Controller Endpoints

### CustomerQuoteRequestController — `/api/v1/quote-requests` — `@PreAuthorize("hasRole('USER')")`
| HTTP | Path | Method | Description |
|------|------|--------|-------------|
| POST | `/` | createRequest | Submit quote request |
| GET | `/mine` | listMyRequests | List user's requests |
| GET | `/{requestId}` | getRequest | Get request details |
| POST | `/{requestId}/cancel` | cancelRequest | Cancel own request |
| GET | `/{requestId}/quotes` | listQuotes | All quotes for request |
| GET | `/{requestId}/quotes/latest` | getLatestQuote | Latest quote |
| POST | `/quotes/{quoteId}/accept` | acceptQuote | Accept a quote |
| POST | `/quotes/{quoteId}/reject` | rejectQuote | Reject a quote |

### BusinessQuoteController — `/api/v1/business/quotes` — `@PreAuthorize("hasRole('USER')")`
| HTTP | Path | Method | Description |
|------|------|--------|-------------|
| GET | `/inbox` | listInbox | Business quote request inbox |
| GET | `/requests/{requestId}` | getRequestDetails | View request details |
| POST | `/requests/{requestId}/decline` | declineRequest | Decline with reason |
| POST | `/requests/{requestId}/draft` | createDraft | Create draft quote |
| POST | `/{quoteId}/send` | sendQuote | Send draft to customer |
| POST | `/{quoteId}/revise` | reviseQuote | Create revision |
| GET | `/{quoteId}` | getQuote | View quote details |

### AdminQuoteController — `/api/v1/admin/quotes` — `@PreAuthorize("hasRole('ADMIN')")`
| HTTP | Path | Method | Description |
|------|------|--------|-------------|
| GET | `/requests` | listAllRequests | List all requests (paginated) |
| GET | `/requests/{requestId}` | getRequest | View any request |
| GET | `/` | listAllQuotes | List all quotes (paginated) |
| GET | `/{quoteId}` | getQuote | View any quote |

---

## Order Module Changes

### New Event
**`order/events/OrderCreatedFromQuoteEvent.java`**
```java
public record OrderCreatedFromQuoteEvent(Long quoteId, Long orderId, String orderNumber) {}
```

### New Event Listener
**`order/service/impl/QuoteAcceptedEventListener.java`**
- `@Component`, `@Slf4j`, `@RequiredArgsConstructor`
- `@TransactionalEventListener` or `@EventListener` on `QuoteAcceptedEvent`
- Creates the order from quote data (same logic as current `QuoteConversionService.convertToOrder` but inside the order module where it belongs)
- Publishes `OrderCreatedFromQuoteEvent` after order creation

---

## Unit Tests (under `src/test/java/com/nifilili/quote/`)

### Service Tests
- `service/impl/QuoteRequestServiceImplTest.java`
  - `createRequest_WhenValidInput_ShouldCreateAndPublishEvent`
  - `createRequest_WhenMissingRequirements_ShouldThrow`
  - `listMyRequests_WhenCalled_ShouldReturnUserRequests`
  - `declineRequest_WhenPending_ShouldRejectAndPublishEvent`
  - `declineRequest_WhenNotPending_ShouldThrowInvalidState`
  - `cancelRequest_WhenPendingAndOwner_ShouldCancel`
  - `cancelRequest_WhenNotOwner_ShouldThrow`
  - `expireRequests_WhenExpired_ShouldUpdateStatus`

- `service/impl/QuoteServiceImplTest.java`
  - `createDraft_WhenValidRequest_ShouldCreateDraftWithLineItems`
  - `createDraft_WhenRequestTerminal_ShouldThrow`
  - `createDraft_WhenDraftAlreadyExists_ShouldThrow`
  - `sendQuote_WhenDraft_ShouldTransitionToSent`
  - `sendQuote_WhenNotDraft_ShouldThrow`
  - `reviseQuote_WhenSent_ShouldMarkRevisedAndCreateNew`
  - `reviseQuote_WhenNotSent_ShouldThrow`
  - `acceptQuote_WhenSentAndValid_ShouldAcceptAndPublishEvent`
  - `acceptQuote_WhenExpired_ShouldThrow`
  - `acceptQuote_WhenNotOwner_ShouldThrow`
  - `rejectQuote_WhenSent_ShouldReject`
  - `expireQuotes_WhenPastValidUntil_ShouldExpire`

- `service/impl/QuoteConversionServiceImplTest.java`
  - `recordConversion_WhenNewConversion_ShouldSave`
  - `recordConversion_WhenAlreadyConverted_ShouldSkip`

- `service/impl/QuoteOrderEventListenerTest.java`
  - `onOrderCreatedFromQuote_ShouldRecordConversion`

### Mapper Tests
- `mapper/QuoteRequestMapperTest.java`
- `mapper/QuoteMapperTest.java`

---

## No Migration Changes
The existing `V12__init_quote_schema.sql` schema is correct and sufficient. No new Flyway migration needed.

---

## Implementation Order
1. Core changes: `InvalidQuoteStateException`, `GlobalExceptionHandler` update, `SwaggerConstants` update
2. Domain entities (4 files)
3. Repositories (4 files)
4. Events (6 quote events + 1 order event)
5. Validation helper
6. MapStruct mappers (2 files)
7. DTOs — request (4) + response (4)
8. Service interfaces (3) + implementations (3)
9. Quote module event listener (1)
10. Controllers (3)
11. Order module: event + listener for quote→order conversion
12. Unit tests (all services + mappers)
13. Postman collections update

Total: ~45 files (production + test)
