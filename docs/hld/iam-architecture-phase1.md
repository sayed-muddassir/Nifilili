# Nifilili IAM Architecture — Phase 1 Design Document

## 1. Context & Goals

**Problem:** The current auth system only supports email+password registration/login. Phase 1 requires mobile+OTP signup, admin user management, and a design extensible for social login.

**Goals:**
- Support Email+Password and Mobile+OTP authentication in Phase 1
- Design linked identity model (one user, multiple auth methods)
- Multi-channel OTP abstraction (SMS, Email, WhatsApp) — hardcoded `123456` for Phase 1
- Full admin user management (create, roles, disable, unlock, force reset)
- Event-driven notification delivery (IAM publishes events, messaging module delivers)
- Microservice-ready module boundaries (extractable with minimal changes)
- Strategy pattern for auth providers (extensible for Google/Facebook OAuth later)

**Design Decisions (Confirmed):**
1. Registration: Phone OR Email + Password
2. Linked identities: One user account, multiple auth methods. Block duplicate registration (login first, add identity from profile)
3. Same login endpoint for all user types (frontend routes by role)
4. Default OTP `123456` for ALL scenarios until SMS provider integrated
5. RBAC + granular permissions (current 24-permission model retained)
6. Single platform (no multi-tenancy)
7. Unlimited concurrent sessions
8. Rate limiting on OTP requests and auth endpoints

---

## 2. Module Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                       API Gateway / Controllers                     │
│  ┌──────────────┐  ┌──────────────────┐  ┌───────────────────────┐  │
│  │AuthController│  │AccountControllers│  │AdminUserController    │  │
│  │/api/auth/*   │  │/api/v1/account/* │  │/api/v1/admin/users/*  │  │
│  └──────┬───────┘  └────────┬─────────┘  └───────────┬───────────┘  │
├─────────┼──────────────────┼─────────────────────────┼──────────────┤
│         ▼                  ▼                         ▼              │
│  ┌──────────────┐  ┌──────────────────┐  ┌───────────────────────┐  │
│  │  AUTH MODULE  │  │  ACCOUNT MODULE  │  │  ACCOUNT MODULE       │  │
│  │              │  │                  │  │  (Admin subpackage)   │  │
│  │ AuthService  │  │ ProfileService   │  │  AdminUserService     │  │
│  │ AuthProvider │  │ SecurityService  │  │                       │  │
│  │  Strategy    │  │ SessionService   │  │                       │  │
│  │ TokenService │  │ VerificationSvc  │  │                       │  │
│  │ OtpService   │  │ IdentityLinkSvc  │  │                       │  │
│  └──────┬───────┘  └────────┬─────────┘  └───────────┬───────────┘  │
├─────────┼──────────────────┼─────────────────────────┼──────────────┤
│         ▼                  ▼                         ▼              │
│  ┌─────────────────────────────────────────────────────────────────┐│
│  │                      DOMAIN / PERSISTENCE                       ││
│  │  User, Role, Permission, RefreshToken, LoginAttempt,            ││
│  │  LoginHistory, UserProfile, EmailVerificationToken,             ││
│  │  PasswordResetToken, OtpToken (NEW), UserAuthProvider (NEW)     ││
│  └─────────────────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────────────────┤
│                         EVENT BUS                                    │
│  UserRegisteredEvent, OtpRequestedEvent (NEW),                      │
│  PasswordResetRequestedEvent, AccountLockedEvent (NEW)               │
│                          ▼                                           │
│  ┌──────────────────────────────────────────────────────────────┐    │
│  │              MESSAGING MODULE (Listener)                      │    │
│  │  Consumes events → delivers via channel strategy              │    │
│  │  (SMS / Email / WhatsApp — hardcoded 123456 for Phase 1)     │    │
│  └──────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘
```

### Module Boundaries (Microservice-Ready)

| Module | Responsibility | Dependencies | Extractable? |
|--------|---------------|--------------|--------------|
| **auth** | Authentication, token management, auth provider strategies | core | Yes — owns User, Role, Permission, tokens |
| **account** | Profile, security settings, sessions, verification, identity linking, admin user mgmt | core, auth (events only) | Yes — consumes auth events, uses auth repos via interfaces |
| **messaging** | Notification delivery (SMS, Email, WhatsApp) | core | Yes — purely event-driven, no direct dependencies |
| **core** | BaseEntity, SecurityUtil, exceptions, shared DTOs | none | Shared library |

**Cross-module communication:** `ApplicationEventPublisher` ONLY. No service injection across modules.

---

## 3. Entity Model

### 3.1 Current Entities (Retained)

```
┌─────────────┐     M:N      ┌──────────┐     M:N     ┌──────────────┐
│    User      │─────────────│   Role    │────────────│  Permission   │
│             │  users_roles  │          │ role_perms  │              │
├─────────────┤              ├──────────┤             ├──────────────┤
│ id          │              │ id       │             │ id           │
│ name        │              │ name     │             │ name         │
│ username    │              └──────────┘             └──────────────┘
│ email (UQ)  │
│ password    │     1:N     ┌──────────────┐
│ phone       │────────────│ RefreshToken  │
│ enabled     │             ├──────────────┤
│ emailVerified│            │ token (UQ)   │
│ phoneVerified│ (NEW)      │ deviceName   │
│ accountLocked│            │ ipAddress    │
│ createdAt   │             │ userAgent    │
│ updatedAt   │             │ expiresAt    │
│ roles (M:N) │             │ revoked      │
└─────────────┘             └──────────────┘
       │
       │ 1:1    ┌──────────────┐
       ├───────│ UserProfile   │
       │        ├──────────────┤
       │        │ avatarUrl    │
       │        │ bio, dob     │
       │        │ gender       │
       │        │ address fields│
       │        └──────────────┘
       │
       │ 1:N    ┌──────────────────┐
       ├───────│ LoginHistory      │
       │        └──────────────────┘
       │
       │ 1:N    ┌──────────────────┐
       ├───────│ LoginAttempt      │
       │        └──────────────────┘
       │
       │ 1:N    ┌──────────────────────┐
       ├───────│ EmailVerificationToken│
       │        └──────────────────────┘
       │
       │ 1:N    ┌──────────────────────┐
       └───────│ PasswordResetToken    │
                └──────────────────────┘
```

### 3.2 New Entities

#### OtpToken (NEW)
Purpose: Unified OTP management for all scenarios (login, signup verification, password reset).

```
Table: otp_tokens
─────────────────────────────────
id              BIGINT PK
identifier      VARCHAR(100) NOT NULL   -- phone number or email
identifier_type VARCHAR(10)  NOT NULL   -- PHONE | EMAIL
otp             VARCHAR(6)   NOT NULL   -- the OTP code (123456 default)
purpose         VARCHAR(20)  NOT NULL   -- LOGIN | SIGNUP | PASSWORD_RESET | VERIFY_PHONE | VERIFY_EMAIL
expires_at      TIMESTAMP    NOT NULL
used            BOOLEAN      DEFAULT FALSE
attempts        INT          DEFAULT 0   -- failed verification attempts (for rate limiting)
created_at      TIMESTAMP    NOT NULL
─────────────────────────────────
Indexes: (identifier, purpose, used), (identifier, otp, used)
Constraint: CHECK purpose IN ('LOGIN','SIGNUP','PASSWORD_RESET','VERIFY_PHONE','VERIFY_EMAIL')
Constraint: CHECK identifier_type IN ('PHONE','EMAIL')
```

**Design rationale:** Single table for all OTP types replaces scattered OTP logic. The `purpose` field determines behavior. The `attempts` counter enables rate limiting per OTP.

#### UserAuthProvider (NEW — Future-Ready, Phase 1 creates table only)
Purpose: Track linked auth methods for social login extensibility.

```
Table: user_auth_providers
─────────────────────────────────
id              BIGINT PK
user_id         BIGINT NOT NULL FK(users)
provider_type   VARCHAR(20) NOT NULL  -- LOCAL_EMAIL | LOCAL_PHONE | GOOGLE | FACEBOOK
provider_user_id VARCHAR(255)         -- external provider user ID (null for local)
metadata        JSONB                 -- provider-specific data (social profile, etc.)
linked_at       TIMESTAMP NOT NULL
─────────────────────────────────
Indexes: (user_id, provider_type) UNIQUE
Constraint: CHECK provider_type IN ('LOCAL_EMAIL','LOCAL_PHONE','GOOGLE','FACEBOOK')
```

**Phase 1 scope:** Create the table. Populate `LOCAL_EMAIL` on email registration, `LOCAL_PHONE` on phone registration. Social providers deferred to future phase.

### 3.3 Schema Changes to Existing Tables

```sql
-- Add to users table:
ALTER TABLE users ADD COLUMN phone_verified BOOLEAN DEFAULT FALSE;
-- Make phone unique when not null:
CREATE UNIQUE INDEX idx_users_phone_unique ON users(phone) WHERE phone IS NOT NULL;
```

---

## 4. Authentication Strategy Pattern

### 4.1 Provider Interface

```java
public interface AuthProviderStrategy {
    /** Check if this provider can handle the given auth request */
    boolean supports(AuthType authType);

    /** Authenticate user and return result */
    AuthResult authenticate(AuthRequest request);

    /** Register new user via this provider */
    AuthResult register(RegistrationRequest request);
}
```

### 4.2 Provider Implementations (Phase 1)

```
AuthProviderStrategy (interface)
├── EmailPasswordAuthProvider    ← Phase 1 (existing logic, refactored)
│   - authenticate: validate email/username + password via AuthenticationManager
│   - register: create user with email + password, assign ROLE_USER
│
├── PhoneOtpAuthProvider         ← Phase 1 (NEW)
│   - authenticate: verify phone + OTP from otp_tokens table
│   - register: create user with phone (no password), verify OTP, assign ROLE_USER
│
└── SocialAuthProvider           ← Future (interface only in Phase 1)
    - authenticate: validate OAuth token, find/create user
    - register: create user from social profile
```

### 4.3 Auth Provider Routing

```java
@Service
public class AuthProviderRouter {
    private final List<AuthProviderStrategy> providers;

    public AuthResult authenticate(AuthRequest request) {
        return providers.stream()
            .filter(p -> p.supports(request.getAuthType()))
            .findFirst()
            .orElseThrow(() -> new UnsupportedAuthMethodException(request.getAuthType()))
            .authenticate(request);
    }
}
```

**AuthType enum:** `EMAIL_PASSWORD`, `PHONE_OTP`, `GOOGLE`, `FACEBOOK`

---

## 5. Authentication Flows

### 5.1 Email + Password Login (Existing — Refactored into Strategy)

```
Client                    AuthController         EmailPasswordProvider       TokenService
  │                            │                        │                       │
  │ POST /api/auth/login       │                        │                       │
  │  {email, password,         │                        │                       │
  │   authType:EMAIL_PASSWORD} │                        │                       │
  │───────────────────────────>│                        │                       │
  │                            │ authenticate(request)  │                       │
  │                            │───────────────────────>│                       │
  │                            │                        │ check lockout         │
  │                            │                        │ validate credentials  │
  │                            │                        │ record attempt        │
  │                            │                        │ record login history  │
  │                            │<───────────────────────│                       │
  │                            │ generate tokens                                │
  │                            │───────────────────────────────────────────────>│
  │                            │<──────────────────────────────────────────────│
  │  JwtAuthResponse           │                        │                       │
  │  {accessToken, refresh,    │                        │                       │
  │   user profile + roles}    │                        │                       │
  │<───────────────────────────│                        │                       │
```

### 5.2 Phone + OTP Login (NEW)

```
Client                  AuthController       OtpService          PhoneOtpProvider     TokenService
  │                          │                   │                     │                  │
  │ Step 1: Request OTP      │                   │                     │                  │
  │ POST /api/auth/otp/request                   │                     │                  │
  │  {phone, purpose:LOGIN}  │                   │                     │                  │
  │─────────────────────────>│                   │                     │                  │
  │                          │ generateOtp(phone)│                     │                  │
  │                          │──────────────────>│                     │                  │
  │                          │                   │ create OtpToken     │                  │
  │                          │                   │ (123456 hardcoded)  │                  │
  │                          │                   │ publish OtpRequestedEvent              │
  │                          │                   │   → messaging module delivers          │
  │  {message: "OTP sent"}   │                   │                     │                  │
  │<─────────────────────────│                   │                     │                  │
  │                          │                   │                     │                  │
  │ Step 2: Verify OTP       │                   │                     │                  │
  │ POST /api/auth/otp/verify│                   │                     │                  │
  │  {phone, otp, purpose}   │                   │                     │                  │
  │─────────────────────────>│                   │                     │                  │
  │                          │ authenticate(request)                   │                  │
  │                          │────────────────────────────────────────>│                  │
  │                          │                   │                     │ validate OTP     │
  │                          │                   │                     │ find user by phone│
  │                          │                   │                     │ record history   │
  │                          │                   │                     │ generate tokens  │
  │                          │                   │                     │────────────────>│
  │                          │<────────────────────────────────────────│                  │
  │  JwtAuthResponse          │                   │                     │                  │
  │<─────────────────────────│                   │                     │                  │
```

### 5.3 Phone Registration (NEW)

```
Client                  AuthController       OtpService          PhoneOtpProvider
  │                          │                   │                     │
  │ Step 1: Request OTP      │                   │                     │
  │ POST /api/auth/otp/request                   │                     │
  │ {phone, purpose:SIGNUP}  │                   │                     │
  │─────────────────────────>│                   │                     │
  │                          │ check phone not   │                     │
  │                          │ already registered│                     │
  │                          │ generateOtp()     │                     │
  │                          │──────────────────>│                     │
  │  {message: "OTP sent"}   │                   │                     │
  │<─────────────────────────│                   │                     │
  │                          │                   │                     │
  │ Step 2: Register         │                   │                     │
  │ POST /api/auth/register  │                   │                     │
  │ {phone, otp, password,   │                   │                     │
  │  name}                   │                   │                     │
  │─────────────────────────>│                   │                     │
  │                          │ register(request) │                     │
  │                          │────────────────────────────────────────>│
  │                          │                   │                     │ verify OTP
  │                          │                   │                     │ create User
  │                          │                   │                     │   phone=verified
  │                          │                   │                     │   password=encoded
  │                          │                   │                     │ assign ROLE_USER
  │                          │                   │                     │ insert UserAuthProvider
  │                          │                   │                     │   (LOCAL_PHONE)
  │                          │                   │                     │ publish UserRegisteredEvent
  │  JwtAuthResponse          │                   │                     │
  │<─────────────────────────│                   │                     │
```

### 5.4 Email Registration (Existing — Minor Changes)

Same as current flow, but additionally:
- Insert `UserAuthProvider` record with `LOCAL_EMAIL`
- Email field optional if phone provided (at least one required)
- Username auto-generated from name if not provided (for phone-first users)

---

## 6. OTP Service Design

### 6.1 OTP Service Interface

```java
public interface OtpService {
    /** Generate and store OTP, publish delivery event */
    void generateAndSendOtp(String identifier, IdentifierType type, OtpPurpose purpose);

    /** Verify OTP. Returns true if valid. Increments attempt counter on failure. */
    boolean verifyOtp(String identifier, String otp, OtpPurpose purpose);

    /** Check rate limit: max N OTP requests per identifier per time window */
    boolean isRateLimited(String identifier, OtpPurpose purpose);
}
```

### 6.2 OTP Configuration

```yaml
app:
  otp:
    default-code: "123456"          # Hardcoded for Phase 1 (null = random)
    length: 6
    expiry-minutes:
      login: 5
      signup: 10
      password-reset: 10
      verify-phone: 10
      verify-email: 1440            # 24 hours
    max-attempts: 3                 # Max verification attempts per OTP
    rate-limit:
      max-requests: 3              # Max OTP requests per window
      window-minutes: 5            # Rate limit window
```

### 6.3 Event-Based Delivery

```java
// Published by OtpService
public record OtpRequestedEvent(
    String identifier,          // phone number or email
    IdentifierType type,        // PHONE or EMAIL
    String otp,                 // the OTP code
    OtpPurpose purpose          // LOGIN, SIGNUP, etc.
) {}

// Consumed by messaging module
@Component
public class OtpDeliveryListener {
    @TransactionalEventListener
    public void handleOtpRequested(OtpRequestedEvent event) {
        // Phase 1: Log the OTP (no actual SMS/Email delivery)
        // Future: Route to SMS/Email/WhatsApp channel based on type
    }
}
```

---

## 7. Rate Limiting Design

### 7.1 OTP Rate Limiting

| Scenario | Limit | Window | Action on Exceed |
|----------|-------|--------|-----------------|
| OTP requests per phone/email | 3 | 5 min | 429 Too Many Requests |
| OTP verification attempts per token | 3 | per token lifetime | Invalidate token |

### 7.2 Auth Rate Limiting

| Scenario | Limit | Window | Action on Exceed |
|----------|-------|--------|-----------------|
| Login attempts per account | 5 | since last success | Lock account |
| Login attempts per IP | 20 | 15 min | 429 Too Many Requests |
| Registration per IP | 5 | 1 hour | 429 Too Many Requests |

**Implementation:** DB-backed counter consistent with current `LoginAttemptService` pattern. No external dependency (Redis) for Phase 1.

---

## 8. Identity Linking Flow

### 8.1 Add Phone to Email Account (from profile settings)

```
Client                 AccountController        IdentityLinkService      OtpService
  │                          │                        │                     │
  │ POST /api/v1/account/    │                        │                     │
  │   identity/link/phone    │                        │                     │
  │ {phone}                  │                        │                     │
  │─────────────────────────>│                        │                     │
  │                          │ initiateLinkPhone()    │                     │
  │                          │───────────────────────>│                     │
  │                          │                        │ check phone not     │
  │                          │                        │  already registered │
  │                          │                        │ generateOtp(phone)  │
  │                          │                        │────────────────────>│
  │  {message: "OTP sent"}   │                        │                     │
  │<─────────────────────────│                        │                     │
  │                          │                        │                     │
  │ POST /api/v1/account/    │                        │                     │
  │   identity/link/phone    │                        │                     │
  │   /verify                │                        │                     │
  │ {phone, otp}             │                        │                     │
  │─────────────────────────>│                        │                     │
  │                          │ completeLinkPhone()    │                     │
  │                          │───────────────────────>│                     │
  │                          │                        │ verify OTP          │
  │                          │                        │ set user.phone      │
  │                          │                        │ set phoneVerified   │
  │                          │                        │ insert AuthProvider │
  │                          │                        │   (LOCAL_PHONE)     │
  │  {success}               │                        │                     │
  │<─────────────────────────│                        │                     │
```

### 8.2 Blocking Duplicate Registration

When a user tries to register with a phone/email that already exists:
- Return error: "This phone/email is already associated with an account. Please login instead."
- Do NOT reveal which account it belongs to (anti-enumeration)

---

## 9. Admin User Management

### 9.1 Endpoints

| HTTP | Path | Purpose | Auth |
|------|------|---------|------|
| GET | `/api/v1/admin/users` | List users (paginated, filterable) | ROLE_ADMIN |
| GET | `/api/v1/admin/users/{userId}` | Get user detail | ROLE_ADMIN |
| POST | `/api/v1/admin/users` | Create user with role(s) | ROLE_ADMIN + SYSTEM_ADMIN |
| PUT | `/api/v1/admin/users/{userId}/status` | Enable/disable account | ROLE_ADMIN |
| PUT | `/api/v1/admin/users/{userId}/roles` | Assign/remove roles | ROLE_ADMIN + SYSTEM_ADMIN |
| POST | `/api/v1/admin/users/{userId}/unlock` | Unlock locked account | ROLE_ADMIN |
| POST | `/api/v1/admin/users/{userId}/force-password-reset` | Force password reset (sends email) | ROLE_ADMIN |
| GET | `/api/v1/admin/users/{userId}/login-history` | View user's login history | ROLE_ADMIN |

### 9.2 Admin Create User Flow

```
Admin                  AdminUserController     AdminUserService
  │                          │                      │
  │ POST /api/v1/admin/users │                      │
  │ {name, email/phone,      │                      │
  │  roles, tempPassword}    │                      │
  │─────────────────────────>│                      │
  │                          │ createUser(request)   │
  │                          │─────────────────────>│
  │                          │                      │ validate uniqueness
  │                          │                      │ create User entity
  │                          │                      │ assign requested roles
  │                          │                      │ encode temp password
  │                          │                      │ save user
  │                          │                      │ publish AdminUserCreatedEvent
  │                          │                      │   (triggers welcome email
  │                          │                      │    with temp password)
  │  {userId, details}       │                      │
  │<─────────────────────────│                      │
```

### 9.3 Filters for User Listing

```
GET /api/v1/admin/users?role=ROLE_USER&enabled=true&search=john&page=0&size=20
```

Filterable by: role, enabled status, accountLocked, emailVerified, search (name/email/phone)

---

## 10. Updated API Contract Summary

### Auth Module Endpoints

| HTTP | Path | Purpose | Public? | Phase |
|------|------|---------|---------|-------|
| POST | `/api/auth/register` | Register (email+pass or phone+OTP+pass) | Yes | P1 (update) |
| POST | `/api/auth/login` | Login (email+pass or phone+OTP) | Yes | P1 (update) |
| POST | `/api/auth/refresh` | Refresh access token | Yes | Existing |
| POST | `/api/auth/logout` | Logout single device | Auth | Existing |
| POST | `/api/auth/logout-all` | Logout all devices | Auth | Existing |
| GET | `/api/auth/me` | Get current user profile | Auth | Existing |
| POST | `/api/auth/otp/request` | Request OTP (login/signup) | Yes | P1 (new) |
| POST | `/api/auth/otp/verify` | Verify OTP (login shortcut) | Yes | P1 (new) |

### Account Module Endpoints

| HTTP | Path | Purpose | Public? | Phase |
|------|------|---------|---------|-------|
| GET | `/api/v1/account/profile` | Get profile | Auth | Existing |
| PUT | `/api/v1/account/profile` | Update profile | Auth | Existing |
| POST | `/api/v1/account/security/change-password` | Change password | Auth | Existing |
| POST | `/api/v1/account/security/request-password-reset` | Request reset | Yes | Existing (update OTP) |
| POST | `/api/v1/account/security/reset-password-link` | Reset via link | Yes | Existing |
| POST | `/api/v1/account/security/reset-password-otp` | Reset via OTP | Yes | Existing (update OTP) |
| POST | `/api/v1/account/security/verify-email` | Verify email | Yes | Existing |
| POST | `/api/v1/account/security/resend-verification` | Resend email verification | Auth | Existing |
| GET | `/api/v1/account/sessions` | List active sessions | Auth | Existing |
| DELETE | `/api/v1/account/sessions/{id}` | Revoke session | Auth | Existing |
| GET | `/api/v1/account/sessions/history` | Login history | Auth | Existing |
| POST | `/api/v1/account/identity/link/phone` | Initiate phone linking | Auth | P1 (new) |
| POST | `/api/v1/account/identity/link/phone/verify` | Complete phone linking | Auth | P1 (new) |
| POST | `/api/v1/account/identity/link/email` | Initiate email linking | Auth | P1 (new) |
| POST | `/api/v1/account/identity/link/email/verify` | Complete email linking | Auth | P1 (new) |

### Admin User Management Endpoints

| HTTP | Path | Purpose | Auth | Phase |
|------|------|---------|------|-------|
| GET | `/api/v1/admin/users` | List users | ADMIN | P1 (new) |
| GET | `/api/v1/admin/users/{userId}` | User detail | ADMIN | P1 (new) |
| POST | `/api/v1/admin/users` | Create user | ADMIN + SYSTEM_ADMIN | P1 (new) |
| PUT | `/api/v1/admin/users/{userId}/status` | Enable/disable | ADMIN | P1 (new) |
| PUT | `/api/v1/admin/users/{userId}/roles` | Manage roles | ADMIN + SYSTEM_ADMIN | P1 (new) |
| POST | `/api/v1/admin/users/{userId}/unlock` | Unlock account | ADMIN | P1 (new) |
| POST | `/api/v1/admin/users/{userId}/force-password-reset` | Force reset | ADMIN | P1 (new) |
| GET | `/api/v1/admin/users/{userId}/login-history` | View history | ADMIN | P1 (new) |

---

## 11. Account State Machine

```
                    ┌──────────┐
        Register    │          │   Admin creates
       ─────────>  │ ACTIVE   │  <────────────
                    │ (enabled)│
                    └────┬─────┘
                         │
            ┌────────────┼────────────┐
            │            │            │
    5 failed logins  Admin disables  Self-delete (future)
            │            │            │
            ▼            ▼            ▼
    ┌───────────┐  ┌──────────┐  ┌──────────┐
    │  LOCKED   │  │ DISABLED │  │ DELETED  │
    │           │  │          │  │ (future) │
    └─────┬─────┘  └────┬─────┘  └──────────┘
          │              │
   Password reset   Admin enables
   or Admin unlock      │
          │              │
          ▼              ▼
    ┌──────────┐   ┌──────────┐
    │  ACTIVE  │   │  ACTIVE  │
    └──────────┘   └──────────┘
```

---

## 12. Design Patterns Used

| Pattern | Where | Why |
|---------|-------|-----|
| **Strategy** | AuthProviderStrategy (Email, Phone, Social) | Swap auth methods without changing controller/service code. Adding Google login = add one new strategy class. |
| **Event-Driven** | OtpRequestedEvent, UserRegisteredEvent | Decouples IAM from messaging. Module boundaries stay clean for microservice extraction. |
| **Interface + Impl** | All services | Standard Spring pattern. Easy to mock in tests, swap implementations. |
| **Repository** | JPA repositories | Clean data access layer. |
| **Builder** | Response DTOs | Immutable responses with flexible construction. |
| **Factory Method** | UserPrincipal.of() | Centralized user principal creation. |
| **Template Method** | completePasswordReset() | Shared reset logic for both link and OTP flows. |

---

## 13. FigJam Diagrams (for Business Team Review)

The following interactive diagrams have been created in FigJam:

1. **IAM System Architecture** — Module overview showing auth, account, messaging modules and their component interactions
2. **Authentication Flows** — Sequence diagram showing email+password login, phone+OTP login, and phone registration flows
3. **Account Lifecycle** — State diagram showing account states (Registering → Active → Locked/Disabled)
4. **Admin Operations** — Flowchart showing all admin user management capabilities and role assignments
