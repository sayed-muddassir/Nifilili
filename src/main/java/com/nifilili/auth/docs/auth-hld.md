# Auth Package HLD

```mermaid
flowchart LR
    C["Client (Web/Mobile)"] -->| "POST /api/auth/login" | AC["AuthController"]
    AC --> AS["AuthService"]
    AS --> AM["AuthenticationManager"]
    AM --> UDS["CustomUserDetailsService"]
    UDS --> UR["UserRepository"]
    UR --> DB[("PostgreSQL: users, roles, users_roles")]
    AS --> JTP["JwtTokenProvider"]
    JTP --> AC
    AC -->| "accessToken (Bearer)" | C

    C -->| "Bearer token on protected APIs" | API["Any Protected Module API"]
    API --> SF["SpringSecurityFilterChain"]
    SF --> JAF["JwtAuthenticationFilter"]
    JAF --> JTP
    JAF --> UDS
    JAF --> SC["SecurityContextHolder (UserPrincipal)"]
    API --> SU["SecurityUtil.getCurrentUserId()"]
    SU --> SC

    SF --> JEP["JwtAuthenticationEntryPoint (401 JSON)"]
```
