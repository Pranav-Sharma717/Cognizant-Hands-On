# 12 — Spring Boot, REST APIs & Spring Security

> Maps to [`Week3/week3.md`](../Week3/week3.md) and [`Week 2/week2_part3_SpringRest.md`](../Week%202/week2_part3_SpringRest.md) — 13 exercises, plus [`Notes/class_notes.md`](../Notes/class_notes.md).
> **References:** [Spring Boot Reference](https://docs.spring.io/spring-boot/index.html), [Spring Security Reference](https://docs.spring.io/spring-security/reference/)

---

# PART 1 — Spring Boot

## 1.1 Why Spring Boot? ⭐
Spring Framework is powerful but needs a lot of configuration. Spring Boot adds **opinionated defaults** so you can run a production app in minutes.

| Feature | What it gives you |
|---|---|
| **Auto-configuration** | Configures beans based on what's on the classpath |
| **Starters** | One dependency pulls a curated, version-compatible set |
| **Embedded server** | Tomcat/Jetty/Undertow inside the JAR — `java -jar app.jar` |
| **Opinionated defaults** | Sensible settings you can always override |
| **Externalised config** | properties/YAML/env vars/CLI args, with profiles |
| **Actuator** | Production endpoints: health, metrics, info, env |
| **No XML** | Convention over configuration |
| **Spring Initializr** | https://start.spring.io project generator |

## 1.2 `@SpringBootApplication` ⭐
```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) { SpringApplication.run(Application.class, args); }
}
```
It's a meta-annotation combining three:
1. **`@SpringBootConfiguration`** — a specialised `@Configuration`; this class is a bean source.
2. **`@EnableAutoConfiguration`** — triggers auto-config: Spring reads `META-INF/spring/…AutoConfiguration.imports` and applies each candidate **conditionally**.
3. **`@ComponentScan`** — scans this package **and its sub-packages** for `@Component`/`@Service`/`@Repository`/`@Controller`.

> ⭐ **Common bug:** put `Application.java` in the root package. If a controller sits outside that package tree, it's never scanned and you get a 404.

**How auto-configuration decides** — `@Conditional` annotations:
`@ConditionalOnClass`, `@ConditionalOnMissingBean`, `@ConditionalOnProperty`, `@ConditionalOnWebApplication`, `@ConditionalOnBean`.
Example: `DataSourceAutoConfiguration` runs only if a DataSource class is on the classpath **and** you haven't defined your own `DataSource` bean — that's how "override by just declaring your own bean" works.
```bash
# see exactly what was auto-configured and why
java -jar app.jar --debug
```
Disable one: `@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)`.

## 1.3 Common starters
| Starter | Brings |
|---|---|
| `spring-boot-starter-web` | Spring MVC, Jackson, **embedded Tomcat**, validation |
| `spring-boot-starter-data-jpa` | Spring Data JPA, Hibernate, HikariCP |
| `spring-boot-starter-security` | Spring Security |
| `spring-boot-starter-validation` | Jakarta Bean Validation (Hibernate Validator) |
| `spring-boot-starter-test` | JUnit 5, Mockito, AssertJ, MockMvc |
| `spring-boot-starter-actuator` | health/metrics endpoints |
| `spring-boot-starter-webflux` | reactive stack |
| `spring-boot-devtools` | auto-restart, live reload |

## 1.4 Configuration & profiles
```properties
# application.properties
server.port=8080
server.servlet.context-path=/api
spring.application.name=employee-service
spring.profiles.active=dev
```
```yaml
# application.yml — same thing, hierarchical
server:
  port: 8080
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/db
---
spring:
  config:
    activate:
      on-profile: prod
  jpa:
    hibernate:
      ddl-auto: validate
```
**Property precedence (highest first):** command-line args → `SPRING_APPLICATION_JSON` → OS env vars → profile-specific `application-{profile}.properties` → `application.properties` → `@PropertySource` → defaults.

## 1.5 Layered architecture ⭐⭐ (the shape of every exercise)

```
HTTP Request
     │
┌────▼──────────────────────────────────────────┐
│ @RestController  — HTTP concerns only         │
│   parse request, validate, map DTO, status    │
└────┬──────────────────────────────────────────┘
┌────▼──────────────────────────────────────────┐
│ @Service         — BUSINESS LOGIC             │
│   orchestration, @Transactional, rules        │
└────┬──────────────────────────────────────────┘
┌────▼──────────────────────────────────────────┐
│ @Repository      — DATA ACCESS                │
│   JpaRepository, queries                      │
└────┬──────────────────────────────────────────┘
┌────▼──────────────────────────────────────────┐
│ @Entity          — DOMAIN MODEL / DB          │
└───────────────────────────────────────────────┘
      DTOs cross the controller↔service boundary
```
**Why layer?** Separation of concerns, independent testability, swappable implementations, and it maps cleanly onto SRP.

## 1.6 Actuator
```properties
management.endpoints.web.exposure.include=health,info,metrics,loggers,env
management.endpoint.health.show-details=when-authorized
```
`/actuator/health` (used for Kubernetes liveness/readiness probes), `/actuator/metrics`, `/actuator/loggers` (change a log level at runtime!), `/actuator/info`, `/actuator/env`, `/actuator/prometheus`.
⚠️ **Always secure Actuator in production** — `/env` and `/heapdump` leak secrets.

---

# PART 2 — HTTP & REST fundamentals ⭐

## 2.1 Client-server & HTTP
**Client** — makes requests (browser, React SPA, mobile, Postman).
**Server** — listens, processes, responds.
**HTTP** — a stateless, text-based request/response protocol over TCP. Stateless means each request carries everything needed (hence tokens/JWT rather than server-side session affinity).

**Request:** method + URL + headers + optional body.
**Response:** status line + headers + body.

**Common headers**
| Header | Purpose |
|---|---|
| `Content-Type` | media type of the **body being sent** (`application/json`) |
| `Accept` | media types the client **wants back** |
| `Authorization` | `Bearer <jwt>` or `Basic <base64>` |
| `Cache-Control` / `ETag` | caching |
| `Location` | URI of a newly created resource (with 201) |
| `X-Correlation-Id` | request tracing |

## 2.2 HTTP methods ⭐
| Method | Purpose | Safe? | **Idempotent?** | Body |
|---|---|:---:|:---:|:---:|
| `GET` | read | ✅ | ✅ | ❌ |
| `POST` | create / non-idempotent action | ❌ | **❌** | ✅ |
| `PUT` | full replace/update | ❌ | ✅ | ✅ |
| `PATCH` | partial update | ❌ | ❌ (usually) | ✅ |
| `DELETE` | remove | ❌ | ✅ | ❌ |
| `HEAD` | headers only | ✅ | ✅ | ❌ |
| `OPTIONS` | supported methods / CORS preflight | ✅ | ✅ | ❌ |

⭐ **Safe** = no server state change. **Idempotent** = calling it N times has the same effect as calling it once. `DELETE /users/1` five times leaves one deleted user (idempotent); `POST /users` five times creates five users (not idempotent).

## 2.3 Status codes ⭐⭐
| Class | Meaning |
|---|---|
| **1xx** | Informational |
| **2xx** | Success |
| **3xx** | Redirection |
| **4xx** | **Client** error |
| **5xx** | **Server** error |

| Code | Name | Use |
|---|---|---|
| **200** | OK | successful GET/PUT/PATCH |
| **201** | Created | successful POST — include a `Location` header |
| 202 | Accepted | async processing started |
| **204** | No Content | successful DELETE, or PUT with no body |
| 301/302 | Moved Permanently / Found | redirect |
| 304 | Not Modified | conditional GET, cache hit |
| **400** | Bad Request | malformed JSON / **validation failure** |
| **401** | Unauthorized | **not authenticated** (missing/invalid credentials or token) |
| **403** | Forbidden | authenticated but **not authorised** for this resource |
| **404** | Not Found | resource doesn't exist |
| 405 | Method Not Allowed | wrong HTTP verb |
| 409 | Conflict | duplicate key, optimistic-lock failure |
| 415 | Unsupported Media Type | wrong `Content-Type` |
| 422 | Unprocessable Entity | semantic validation failure |
| 429 | Too Many Requests | rate limited |
| **500** | Internal Server Error | unhandled exception |
| 502/503/504 | Bad Gateway / Service Unavailable / Gateway Timeout | upstream problems |

⭐ **401 vs 403** — *"Who are you?"* vs *"I know who you are, and you can't do this."*

## 2.4 REST principles ⭐
**RE**presentational **S**tate **T**ransfer (Roy Fielding, 2000).
1. **Client-Server** — separation of concerns
2. **Stateless** — the server stores no client session between requests
3. **Cacheable** — responses declare their cacheability
4. **Uniform Interface** — resource identification via URIs, standard methods, self-descriptive messages, HATEOAS
5. **Layered System** — proxies/gateways are transparent to the client
6. **Code on Demand** (optional)

**URI design conventions**
```
✅ GET    /api/v1/employees            list
✅ GET    /api/v1/employees/1          one
✅ POST   /api/v1/employees            create
✅ PUT    /api/v1/employees/1          full update
✅ PATCH  /api/v1/employees/1          partial update
✅ DELETE /api/v1/employees/1          delete
✅ GET    /api/v1/departments/1/employees   nested resource
✅ GET    /api/v1/employees?status=ACTIVE&page=0&size=20&sort=salary,desc

❌ /getAllEmployees   ❌ /employee/delete/1   ❌ /api/Employees
```
Rules: **plural nouns**, no verbs (the HTTP method is the verb), lowercase with hyphens, version the API, filter/sort/page with query params.

**REST vs SOAP**
| | REST | SOAP |
|---|---|---|
| Style | architectural style | protocol |
| Format | JSON (usually), XML, anything | XML only |
| Transport | HTTP | HTTP, SMTP, JMS |
| Contract | OpenAPI (optional) | WSDL (mandatory) |
| Weight | lightweight, fast | verbose, heavy |
| Built-in security/txn | no (use HTTPS/JWT) | WS-Security, WS-AT |

## 2.5 JSON
```json
{ "id": 1, "name": "Pranav", "salary": 50000.0, "active": true,
  "skills": ["Java","Spring"], "dept": { "id": 10, "name": "IT" }, "manager": null }
```
Spring Boot uses **Jackson** to serialize/deserialize automatically.
```java
@JsonProperty("employee_name") private String name;
@JsonIgnore                    private String password;
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(pattern = "yyyy-MM-dd") private LocalDate hireDate;
@JsonIgnoreProperties(ignoreUnknown = true)
```

---

# PART 3 — Building REST APIs in Spring Boot ⭐

## 3.1 A complete controller
```java
@RestController                                  // @Controller + @ResponseBody
@RequestMapping("/api/v1/employees")
@Validated
public class EmployeeController {

    private final EmployeeService service;
    public EmployeeController(EmployeeService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<Page<EmployeeDto>> getAll(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.findAll(status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));            // 200
    }

    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeRequest req) {
        EmployeeDto created = service.create(req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                          .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);    // 201 + Location
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> update(@PathVariable Long id,
                                              @Valid @RequestBody EmployeeRequest req) {
        return ResponseEntity.ok(service.update(id, req));        // 200
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeDto> patch(@PathVariable Long id,
                                             @RequestBody Map<String,Object> updates) {
        return ResponseEntity.ok(service.patch(id, updates));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }   // 204
}
```

**Parameter annotations**
| Annotation | Source | Example |
|---|---|---|
| `@PathVariable` | URI template | `/employees/{id}` |
| `@RequestParam` | query string | `?status=ACTIVE` |
| `@RequestBody` | request body (JSON→object) | POST/PUT payloads |
| `@RequestHeader` | header | `@RequestHeader("Authorization")` |
| `@CookieValue` | cookie | |
| `@ModelAttribute` | form data | |

⭐ **`@Controller` vs `@RestController`:** `@RestController` = `@Controller` + `@ResponseBody`, so every method's return value is serialised into the response body (JSON) instead of being resolved as a view name.

## 3.2 Validation (Exercise 10) ⭐
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```
```java
public record EmployeeRequest(
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be 2-50 characters")
    String name,

    @NotNull @Email(message = "Invalid email format")
    String email,

    @NotNull @Positive @DecimalMax("1000000.00")
    BigDecimal salary,

    @Min(18) @Max(65) Integer age,

    @Past LocalDate dateOfBirth,
    @FutureOrPresent LocalDate joiningDate,

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
    String phone,

    @NotEmpty List<@NotBlank String> skills,

    @Valid Address address          // ⭐ cascades validation into nested objects
) { }
```
Constraints: `@NotNull`, `@NotEmpty` (collection/string not empty), `@NotBlank` (string with non-whitespace), `@Size`, `@Min`/`@Max`, `@Positive`/`@Negative`, `@Email`, `@Pattern`, `@Past`/`@Future`, `@AssertTrue`, `@Digits`, `@Valid`.
⭐ `@NotNull` vs `@NotEmpty` vs `@NotBlank`: not null / not null and size>0 / not null, size>0 and contains non-whitespace.

`@Valid` on the parameter triggers validation → a failure throws `MethodArgumentNotValidException` → handle it globally for a clean 400.

## 3.3 Global exception handling (Exercise 10) ⭐
```java
public record ErrorResponse(LocalDateTime timestamp, int status, String error,
                            String message, String path, Map<String,String> fieldErrors) { }

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex,
                                                        HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest req) {
        Map<String,String> errors = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(FieldError::getField,
                                      f -> Optional.ofNullable(f.getDefaultMessage()).orElse(""),
                                      (a,b) -> a));
        return build(HttpStatus.BAD_REQUEST, "Validation failed", req, errors);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleConflict(DataIntegrityViolationException ex,
                                                        HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "Data integrity violation", req, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException ex,
                                                          HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "Access denied", req, null);
    }

    @ExceptionHandler(Exception.class)                       // catch-all — LAST
    public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception on {}", req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                     "An unexpected error occurred", req, null);   // ⭐ never leak internals
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus s, String msg,
                                                 HttpServletRequest req, Map<String,String> f) {
        return ResponseEntity.status(s).body(new ErrorResponse(
            LocalDateTime.now(), s.value(), s.getReasonPhrase(), msg, req.getRequestURI(), f));
    }
}
```
`@ResponseStatus(HttpStatus.NOT_FOUND)` on the exception class is a simpler alternative for basic cases.
Spring 6 also supports **RFC 7807 Problem Details** (`ProblemDetail`, `ResponseEntityExceptionHandler`) — the modern standard error format.

## 3.4 Calling other services
```java
// RestTemplate — legacy but still everywhere
@Bean RestTemplate restTemplate(RestTemplateBuilder b){ return b.build(); }
Employee e = restTemplate.getForObject("/api/employees/{id}", Employee.class, 1);

// WebClient — reactive, non-blocking (works fine in an MVC app too)
Mono<Employee> mono = webClient.get().uri("/api/employees/{id}", 1)
                               .retrieve().bodyToMono(Employee.class);

// RestClient — Spring 6.1+, synchronous with a fluent API ✅ the modern default
Employee e = restClient.get().uri("/api/employees/{id}", 1)
                       .retrieve().body(Employee.class);
```
> `RestTemplate` is in **maintenance mode** — mention `RestClient`/`WebClient` to show currency.

## 3.5 CORS
```java
@CrossOrigin(origins = "http://localhost:3000")          // per controller
```
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override public void addCorsMappings(CorsRegistry r) {
        r.addMapping("/api/**")
         .allowedOrigins("http://localhost:3000")
         .allowedMethods("GET","POST","PUT","PATCH","DELETE")
         .allowedHeaders("*").allowCredentials(true).maxAge(3600);
    }
}
```
**CORS** = Cross-Origin Resource Sharing — the browser's same-origin policy blocks a React app on `:3000` from calling an API on `:8080` unless the server sends `Access-Control-Allow-Origin`. Non-simple requests trigger an `OPTIONS` **preflight**.

## 3.6 Documentation — OpenAPI/Swagger
```xml
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.6.0</version>
</dependency>
```
→ UI at `/swagger-ui.html`, spec at `/v3/api-docs`.
```java
@Operation(summary = "Get an employee by id")
@ApiResponses({ @ApiResponse(responseCode = "200", description = "Found"),
                @ApiResponse(responseCode = "404", description = "Not found") })
```

---

# PART 4 — Spring Security ⭐

## 4.1 Core concepts
- **Authentication** — *who are you?* (credentials → `Authentication` in the `SecurityContext`)
- **Authorization** — *what are you allowed to do?* (roles/authorities)
- **Principal** — the authenticated user
- **`UserDetailsService`** — loads user data by username
- **`PasswordEncoder`** — hashes passwords (**BCrypt** by default)
- **Filter chain** — security is a chain of servlet filters in front of `DispatcherServlet`

```
Request → SecurityFilterChain → DispatcherServlet → Controller
          (Cors → Csrf → Authentication → Authorization → …)
```

## 4.2 Basic Authentication (Exercise 12)
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          .csrf(csrf -> csrf.disable())                    // ok for a stateless REST API
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/api/public/**", "/actuator/health").permitAll()
              .requestMatchers(HttpMethod.GET, "/api/employees/**").hasAnyRole("USER","ADMIN")
              .requestMatchers("/api/admin/**").hasRole("ADMIN")
              .anyRequest().authenticated())
          .httpBasic(Customizer.withDefaults())
          .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails user = User.withUsername("user")
                .password(encoder.encode("password")).roles("USER").build();
        UserDetails admin = User.withUsername("admin")
                .password(encoder.encode("admin")).roles("ADMIN").build();
        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean public PasswordEncoder passwordEncoder(){ return new BCryptPasswordEncoder(); }
}
```
Basic Auth sends `Authorization: Basic base64(user:pass)` on **every request**. Base64 is **encoding, not encryption** — it's only safe over HTTPS, and it has no logout or expiry. Hence JWT.

> **Spring Security 6 note:** the old `WebSecurityConfigurerAdapter` is **removed**. Use a `SecurityFilterChain` bean and the lambda DSL, as above.

**Method-level security**
```java
@EnableMethodSecurity
// ...
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasAuthority('SCOPE_write')")
@PreAuthorize("#id == authentication.principal.id")
@PostAuthorize("returnObject.owner == authentication.name")
```
⭐ **Role vs Authority:** a role is just an authority prefixed with `ROLE_`. `hasRole("ADMIN")` checks for the authority `ROLE_ADMIN`.

## 4.3 JWT Authentication (Exercise 13) ⭐⭐

**JWT** (JSON Web Token) — a compact, URL-safe, **self-contained**, digitally signed token.
```
header.payload.signature
eyJhbGciOiJIUzI1NiJ9 . eyJzdWIiOiJwcmFuYXYiLCJleHAiOjE3MDB9 . SflKxwRJSMeKKF2QT4f...
```
| Part | Contents |
|---|---|
| **Header** | `{"alg":"HS256","typ":"JWT"}` |
| **Payload** | claims: `sub`, `iat`, `exp`, `iss`, `aud`, plus custom (roles, userId) |
| **Signature** | `HMACSHA256(base64(header) + "." + base64(payload), secret)` |

⚠️ The payload is **Base64-encoded, not encrypted** — anyone can read it. Never put passwords or sensitive data in a JWT. The signature only guarantees **integrity** (it wasn't tampered with).

**The flow**
```
1. POST /api/auth/login  { username, password }
2. Server validates → signs a JWT → returns { token }
3. Client stores it and sends  Authorization: Bearer <token>  on every request
4. A filter validates the signature + expiry, builds an Authentication,
   and puts it in the SecurityContext — NO server-side session
5. Token expires → client uses a refresh token or logs in again
```

```java
@Component
public class JwtUtil {
    private final SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRY_MS = 1000 * 60 * 60;   // 1 hour

    public String generateToken(UserDetails user) {
        return Jwts.builder()
            .subject(user.getUsername())
            .claim("roles", user.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority).toList())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRY_MS))
            .signWith(key)
            .compact();
    }

    public String extractUsername(String token) { return parse(token).getSubject(); }

    public boolean isValid(String token, UserDetails user) {
        Claims c = parse(token);
        return c.getSubject().equals(user.getUsername()) && c.getExpiration().after(new Date());
    }

    private Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil; private final UserDetailsService uds;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                String username = jwtUtil.extractUsername(token);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails user = uds.loadUserByUsername(username);
                    if (jwtUtil.isValid(token, user)) {
                        var auth = new UsernamePasswordAuthenticationToken(
                                        user, null, user.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (JwtException e) {
                logger.warn("Invalid JWT: " + e.getMessage());
            }
        }
        chain.doFilter(req, res);
    }
}
```
```java
// register the filter
http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
```
```java
@PostMapping("/api/auth/login")
public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req) {
    authManager.authenticate(
        new UsernamePasswordAuthenticationToken(req.username(), req.password()));
    UserDetails user = uds.loadUserByUsername(req.username());
    return ResponseEntity.ok(new AuthResponse(jwtUtil.generateToken(user)));
}
```

⭐ **Session vs JWT**
| | Session (cookie) | JWT |
|---|---|---|
| State | server stores the session | **stateless** — all state in the token |
| Scaling | needs sticky sessions or a shared store | scales horizontally trivially |
| Revocation | easy (delete the session) | **hard** — needs a blocklist or short expiry |
| Size | small cookie | larger header on every request |
| Cross-domain / mobile | awkward | ✅ natural |

**Security best practices:** HTTPS only · short access-token expiry (15–60 min) + refresh tokens · strong secret (≥256-bit) from env/vault, never in Git · validate `exp`, `iss`, `aud`, `alg` (reject `alg: none`) · store tokens in memory or an httpOnly cookie rather than `localStorage` (XSS) · BCrypt for passwords · rate-limit the login endpoint · keep CSRF enabled for cookie-based sessions.

**OWASP Top 10** — worth naming: Broken Access Control, Cryptographic Failures, **Injection (SQLi/XSS)**, Insecure Design, Security Misconfiguration, Vulnerable Components, Identification & Authentication Failures, Software/Data Integrity Failures, Logging & Monitoring Failures, SSRF.

---

## ⭐ Interview Gold — Boot / REST / Security Rapid Fire

| Q | A |
|---|---|
| Spring vs Spring Boot? | Spring is the framework; Boot adds auto-configuration, starters, an embedded server and opinionated defaults. |
| What does `@SpringBootApplication` do? | `@SpringBootConfiguration` + `@EnableAutoConfiguration` + `@ComponentScan`. |
| How does auto-configuration work? | Conditional configuration classes listed in `AutoConfiguration.imports`, applied via `@ConditionalOnClass`/`OnMissingBean` etc. |
| How do you override an auto-configured bean? | Define your own bean — `@ConditionalOnMissingBean` backs off. Or `exclude=` the auto-config class. |
| What is a starter? | A curated dependency aggregator with compatible versions. |
| Embedded server — which and how to change? | Tomcat by default; exclude it and add `spring-boot-starter-jetty`/`undertow`. |
| `@Controller` vs `@RestController`? | The latter adds `@ResponseBody` to every method (returns data, not view names). |
| `@RequestParam` vs `@PathVariable`? | Query string vs URI template segment. |
| What is REST? | An architectural style: stateless, client-server, cacheable, uniform interface, layered. |
| Is REST stateless? Why does it matter? | Yes — every request is self-contained, which is what makes horizontal scaling and JWT natural. |
| Idempotent methods? | GET, PUT, DELETE, HEAD, OPTIONS (not POST/PATCH). |
| PUT vs PATCH vs POST? | Full replace (idempotent) vs partial update vs create/non-idempotent action. |
| 401 vs 403? | Not authenticated vs authenticated but not permitted. |
| Which status for a successful POST? | 201 Created, with a `Location` header. |
| Which status for a successful DELETE? | 204 No Content. |
| How do you handle exceptions globally? | `@RestControllerAdvice` + `@ExceptionHandler`, returning a structured `ErrorResponse` (or RFC 7807 `ProblemDetail`). |
| How do you validate a request body? | `@Valid @RequestBody` + Jakarta constraints; handle `MethodArgumentNotValidException` → 400. |
| `@NotNull` vs `@NotEmpty` vs `@NotBlank`? | Not null / not null and non-empty / not null and has non-whitespace. |
| What is CORS and why does it appear? | Browser same-origin policy; the API must send `Access-Control-Allow-Origin` for a different-origin SPA. |
| Why DTOs instead of entities in the API? | Decoupling, no lazy-loading/recursion issues, no over-exposure of internal fields, independent API versioning. |
| Authentication vs Authorization? | Who you are vs what you may do. |
| How does Spring Security work? | A chain of servlet filters that authenticate, populate the `SecurityContext`, and authorise before the controller. |
| What is a JWT and its three parts? | Signed self-contained token: header, payload (claims), signature. |
| Is a JWT encrypted? | No — Base64-encoded and signed. Readable by anyone; only integrity is guaranteed. |
| Session vs JWT? | Server-side state vs stateless; easy revocation vs easy scaling. |
| How do you invalidate a JWT? | You can't directly — use short expiry + refresh tokens, or maintain a server-side blocklist. |
| How do you store passwords? | Salted hash with BCrypt (never plaintext, never MD5/SHA-1). |
| How do you prevent SQL injection? | Parameterised queries / JPA bind parameters, never string concatenation; least-privilege DB user. |
| What is CSRF and when can you disable it? | Cross-site request forgery; safe to disable for a stateless token-based API (no ambient cookie auth). |

---

## 📚 References
- Spring Boot Reference: https://docs.spring.io/spring-boot/index.html
- Spring Boot — Auto-configuration: https://docs.spring.io/spring-boot/reference/using/auto-configuration.html
- Spring MVC — Web on Servlet Stack: https://docs.spring.io/spring-framework/reference/web/webmvc.html
- Spring Security Reference: https://docs.spring.io/spring-security/reference/
- Spring Security — Architecture: https://docs.spring.io/spring-security/reference/servlet/architecture.html
- Spring Guides (build a REST service): https://spring.io/guides/gs/rest-service
- Spring Initializr: https://start.spring.io/
- MDN — HTTP: https://developer.mozilla.org/en-US/docs/Web/HTTP
- MDN — HTTP status codes: https://developer.mozilla.org/en-US/docs/Web/HTTP/Status
- REST API Tutorial: https://restfulapi.net/
- JWT Introduction: https://jwt.io/introduction
- OWASP Top 10: https://owasp.org/www-project-top-ten/
- springdoc-openapi: https://springdoc.org/
- Baeldung — Spring Security JWT: https://www.baeldung.com/spring-security-oauth-jwt
