# 13 — Microservices with Spring Boot & Spring Cloud

> Maps to [`Week4/microservices.md`](../Week4/microservices.md) — 4 parts, 14 exercises (user/order, inventory + Eureka, gateway, resilience, bank services, OAuth2/SSO, edge services, load balancing).
> **References:** [Spring Cloud](https://spring.io/projects/spring-cloud), [microservices.io](https://microservices.io/patterns/), [Resilience4j](https://resilience4j.readme.io/docs)

---

## 1. Monolith vs Microservices ⭐

```
   MONOLITH                          MICROSERVICES
┌──────────────────┐        ┌────────┐ ┌────────┐ ┌────────┐
│  UI              │        │ User   │ │ Order  │ │Payment │
│  Business logic  │        │ Service│ │ Service│ │ Service│
│  Data access     │        └───┬────┘ └───┬────┘ └───┬────┘
│  ONE DATABASE    │          ┌─▼─┐      ┌─▼─┐      ┌─▼─┐
└──────────────────┘          │DB │      │DB │      │DB │  ← database per service
   one deployable             └───┘      └───┘      └───┘
```

| | Monolith | Microservices |
|---|---|---|
| Deployment | one unit | independent per service |
| Scaling | scale the whole app | **scale only the hot service** |
| Tech stack | uniform | polyglot allowed |
| Database | shared | **one per service** |
| Team | one large team | small autonomous teams |
| Fault isolation | one bug can kill everything | contained (with circuit breakers) |
| Complexity | simple to start | **distributed-systems complexity** |
| Transactions | ACID, easy | eventual consistency / Saga |
| Testing | simpler | needs contract & integration tests |
| Debugging | one stack trace | needs distributed tracing |

⭐ **Say this:** *"Microservices trade simplicity for independent deployability and scalability. They're not automatically better — for a small team a well-structured modular monolith is often the right call. You adopt microservices when team size, deployment friction or scaling needs justify the operational overhead."*

**Principles:** single responsibility per service · database per service · independently deployable · decentralised governance · design for failure · smart endpoints, dumb pipes · automation (CI/CD) · observability.

---

## 2. Spring Cloud components ⭐

| Concern | Component |
|---|---|
| Service discovery | **Netflix Eureka** (or Consul, Kubernetes DNS) |
| Client-side load balancing | **Spring Cloud LoadBalancer** (replaced Ribbon) |
| Declarative HTTP client | **OpenFeign** |
| API Gateway / edge | **Spring Cloud Gateway** (replaced Zuul) |
| Fault tolerance | **Resilience4j** (replaced Hystrix) |
| Centralised config | **Spring Cloud Config Server** |
| Distributed tracing | **Micrometer Tracing** + Zipkin/Jaeger (replaced Sleuth) |
| Messaging | Spring Cloud Stream + Kafka/RabbitMQ |
| Security | Spring Security OAuth2 / Spring Authorization Server |

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-dependencies</artifactId>
      <version>2023.0.3</version>
      <type>pom</type><scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

---

## 3. Service Discovery — Eureka (Exercises 2 & Bank-3) ⭐

**Problem:** in the cloud, service instances come and go with dynamic IPs. Hard-coding `http://192.168.1.5:8081` doesn't work.
**Solution:** a **service registry**. Services register themselves; clients look them up by **logical name**.

**Eureka Server**
```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication { }
```
```properties
server.port=8761
spring.application.name=eureka-server
eureka.client.register-with-eureka=false     # the server doesn't register with itself
eureka.client.fetch-registry=false
```
Dashboard at `http://localhost:8761`.

**Eureka Client (every service)**
```java
@SpringBootApplication
@EnableDiscoveryClient          // optional in recent Spring Cloud — auto-detected
public class OrderServiceApplication { }
```
```properties
spring.application.name=order-service          # ⭐ this becomes the service ID
server.port=8082
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.prefer-ip-address=true
eureka.instance.lease-renewal-interval-in-seconds=30
```

**How it works:** register on startup → send a **heartbeat** every 30 s → the server evicts an instance after ~90 s without one → clients cache the registry locally and refresh every 30 s.
**Self-preservation mode** — if too many heartbeats are lost at once (likely a network partition), Eureka stops evicting instances rather than emptying the registry.

⭐ **Client-side vs server-side load balancing:** Eureka + Spring Cloud LoadBalancer is **client-side** — the caller has the full instance list and picks one (default round-robin), avoiding an extra network hop and a central bottleneck. A traditional load balancer (nginx/ELB) is server-side.

---

## 4. Inter-service communication (Exercise 1)

### Synchronous — OpenFeign ⭐
```java
@SpringBootApplication
@EnableFeignClients
public class OrderServiceApplication { }

@FeignClient(name = "user-service", fallback = UserClientFallback.class)
public interface UserClient {
    @GetMapping("/api/users/{id}")
    UserDto getUserById(@PathVariable("id") Long id);

    @PostMapping("/api/users")
    UserDto create(@RequestBody UserDto dto);
}

@Component
class UserClientFallback implements UserClient {
    public UserDto getUserById(Long id){ return new UserDto(id, "Unknown", "N/A"); }
    public UserDto create(UserDto d){ throw new ServiceUnavailableException("user-service down"); }
}
```
`name = "user-service"` is resolved through Eureka → load balanced automatically. No URL, no `RestTemplate` boilerplate.

### `RestTemplate` / `WebClient` with `@LoadBalanced`
```java
@Bean @LoadBalanced
public RestTemplate restTemplate(){ return new RestTemplate(); }

// use the SERVICE NAME as the host
restTemplate.getForObject("http://user-service/api/users/" + id, UserDto.class);
```

### Asynchronous — messaging
Kafka/RabbitMQ events decouple services in **time**: the producer doesn't wait, the consumer processes when ready. Use for: order-placed → notify, inventory-updated → reindex, audit events.

| Sync (REST/Feign) | Async (Kafka/Rabbit) |
|---|---|
| Immediate response | eventual |
| Tight temporal coupling | decoupled |
| Simple | needs a broker + idempotent consumers |
| Cascading failure risk | natural buffering |

---

## 5. API Gateway (Exercises 3, Bank-4, Edge-1) ⭐

The **single entry point** for all clients. Handles cross-cutting concerns once instead of in every service.

**Responsibilities:** routing · load balancing · authentication/authorization · rate limiting · caching · request/response transformation · CORS · logging & tracing · circuit breaking · SSL termination · **API composition**.

```properties
spring.application.name=api-gateway
server.port=8080
spring.cloud.gateway.discovery.locator.enabled=true

spring.cloud.gateway.routes[0].id=user-service
spring.cloud.gateway.routes[0].uri=lb://user-service          # lb:// = load-balanced via Eureka
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/users/**

spring.cloud.gateway.routes[1].id=order-service
spring.cloud.gateway.routes[1].uri=lb://order-service
spring.cloud.gateway.routes[1].predicates[0]=Path=/orders/**
spring.cloud.gateway.routes[1].filters[0]=RewritePath=/orders/(?<seg>.*), /api/orders/$\{seg}
spring.cloud.gateway.routes[1].filters[1]=AddRequestHeader=X-Source, gateway
```
```java
@Bean
public RouteLocator routes(RouteLocatorBuilder builder) {
    return builder.routes()
        .route("payment", r -> r.path("/pay/**")
            .filters(f -> f.stripPrefix(1)
                           .circuitBreaker(c -> c.setName("payCB").setFallbackUri("forward:/fallback"))
                           .requestRateLimiter(rl -> rl.setRateLimiter(redisRateLimiter())))
            .uri("lb://payment-service"))
        .build();
}
```

**Predicates:** `Path`, `Method`, `Header`, `Query`, `Host`, `Cookie`, `After`/`Before`/`Between`, `RemoteAddr`, `Weight` (canary releases).
**Filters:** `AddRequestHeader`, `RemoveResponseHeader`, `RewritePath`, `StripPrefix`, `PrefixPath`, `Retry`, `RequestRateLimiter`, `CircuitBreaker`, `SetStatus`, `RequestSize`.

### Global filter (Exercise Bank-4)
```java
@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(LoggingGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        long start = System.currentTimeMillis();
        log.info("→ {} {}", exchange.getRequest().getMethod(), path);
        return chain.filter(exchange).then(Mono.fromRunnable(() ->
            log.info("← {} {} in {} ms", exchange.getResponse().getStatusCode(), path,
                     System.currentTimeMillis() - start)));
    }
    @Override public int getOrder(){ return -1; }
}
```

### Rate limiting (Redis token bucket)
```properties
spring.cloud.gateway.routes[0].filters[0]=RequestRateLimiter=10, 20, #{@userKeyResolver}
# replenishRate=10/s, burstCapacity=20
```
```java
@Bean KeyResolver userKeyResolver() {
    return exchange -> Mono.just(
        Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                .map(a -> a.getAddress().getHostAddress()).orElse("anon"));
}
```
> Spring Cloud Gateway is built on **Spring WebFlux** (reactive, non-blocking) — don't add `spring-boot-starter-web` to a gateway module; they conflict.

---

## 6. Resilience patterns — Resilience4j (Exercise 4, Resilience-3) ⭐⭐

### Circuit Breaker — the key pattern
```
        failure rate < threshold
   ┌─────────── CLOSED ◄──────────────┐
   │        (calls pass through)      │ success rate ok
   │ failure rate ≥ threshold         │
   ▼                                  │
  OPEN ──── after waitDuration ───► HALF_OPEN
 (fail fast,                       (allow N trial calls)
  call fallback)  ◄─── failures ────┘
```
| State | Behaviour |
|---|---|
| **CLOSED** | normal — calls go through, failures are counted |
| **OPEN** | fail immediately with the fallback; no calls to the failing service (gives it time to recover) |
| **HALF_OPEN** | let a few probe calls through; success → CLOSED, failure → OPEN |

**Why:** prevents **cascading failure** — one slow downstream service exhausting all your threads and taking down the whole system.

```java
@Service
public class PaymentService {

    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    @Retry(name = "paymentService")
    @TimeLimiter(name = "paymentService")
    @Bulkhead(name = "paymentService")
    @RateLimiter(name = "paymentService")
    public PaymentResponse processPayment(PaymentRequest req) {
        return restClient.post().uri("http://payment-gateway/pay").body(req)
                         .retrieve().body(PaymentResponse.class);
    }

    // ⭐ the fallback MUST have the same signature + a Throwable parameter
    public PaymentResponse paymentFallback(PaymentRequest req, Throwable t) {
        log.warn("Payment service unavailable, queuing request: {}", t.getMessage());
        return PaymentResponse.queued(req.orderId());
    }
}
```
```yaml
resilience4j:
  circuitbreaker:
    instances:
      paymentService:
        registerHealthIndicator: true
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50            # % failures to open
        waitDurationInOpenState: 10s
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
  retry:
    instances:
      paymentService:
        maxAttempts: 3
        waitDuration: 1s
        exponentialBackoffMultiplier: 2
        retryExceptions: [java.io.IOException, java.util.concurrent.TimeoutException]
  ratelimiter:
    instances:
      paymentService: { limitForPeriod: 10, limitRefreshPeriod: 1s, timeoutDuration: 0 }
  bulkhead:
    instances:
      paymentService: { maxConcurrentCalls: 20, maxWaitDuration: 100ms }
  timelimiter:
    instances:
      paymentService: { timeoutDuration: 3s }
```

**The five Resilience4j patterns**
| Pattern | Purpose |
|---|---|
| **CircuitBreaker** | stop calling a failing service |
| **Retry** | transparently retry transient failures (with backoff) |
| **RateLimiter** | cap the call rate |
| **Bulkhead** | isolate resource pools so one slow dependency can't consume all threads |
| **TimeLimiter** | bound how long a call may take |
> Decorator order (outermost→innermost): Retry → CircuitBreaker → RateLimiter → TimeLimiter → Bulkhead.
> ⚠️ Retry + non-idempotent operations = duplicate side effects. Only retry idempotent calls, or use idempotency keys.

---

## 7. Centralised Configuration
```java
@SpringBootApplication @EnableConfigServer
public class ConfigServerApplication { }
```
```properties
server.port=8888
spring.cloud.config.server.git.uri=https://github.com/org/config-repo
spring.cloud.config.server.git.default-label=main
```
Client (`application.properties`):
```properties
spring.config.import=optional:configserver:http://localhost:8888
spring.application.name=order-service       # resolves order-service.yml in the repo
```
Refresh at runtime with `@RefreshScope` + `POST /actuator/refresh`, or Spring Cloud Bus for a fleet-wide refresh. Encrypt secrets with `{cipher}` or use Vault.

---

## 8. Centralised Authentication & SSO (Part 3) ⭐

### OAuth 2.1 / OpenID Connect
- **OAuth 2.0** — an **authorization** framework: delegate access without sharing passwords.
- **OIDC** — an **authentication** layer on top of OAuth2 that adds the **ID token** (a JWT describing the user).

**Roles:** Resource Owner (user) · Client (your app) · Authorization Server (Keycloak/Okta/Auth0/Spring Authorization Server) · Resource Server (your API).

**Authorization Code flow + PKCE** (the recommended flow for web & SPA):
```
1. User clicks Login → browser redirects to the Authorization Server
2. User authenticates there (once) → consents
3. AS redirects back with an authorization CODE
4. Client exchanges code (+ PKCE verifier) for ACCESS + ID + REFRESH tokens
5. Client calls the API with  Authorization: Bearer <access_token>
6. Resource Server validates the JWT signature via the AS's JWKS endpoint
```
> **Why the code flow and not implicit?** The token never travels through the browser URL. The **implicit flow is deprecated** in OAuth 2.1; PKCE is now required for public clients.

**Client (OIDC login)**
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```
```properties
spring.security.oauth2.client.registration.keycloak.client-id=fse-app
spring.security.oauth2.client.registration.keycloak.client-secret=${CLIENT_SECRET}
spring.security.oauth2.client.registration.keycloak.scope=openid,profile,email
spring.security.oauth2.client.provider.keycloak.issuer-uri=http://localhost:8180/realms/fse
```
```java
http.oauth2Login(Customizer.withDefaults());
```

**Resource Server (JWT validation)**
```xml
<artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
```
```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8180/realms/fse
# or: spring.security.oauth2.resourceserver.jwt.jwk-set-uri=.../protocol/openid-connect/certs
```
```java
http.authorizeHttpRequests(a -> a
        .requestMatchers("/api/admin/**").hasAuthority("SCOPE_admin")
        .anyRequest().authenticated())
    .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()));
```

**SSO (Single Sign-On)** — one login at the Authorization Server grants access to every participating service; each service validates the token independently. This is exactly what makes JWT-based auth fit microservices: **no shared session store**.

**Propagating identity between services:** forward the `Authorization` header via a Feign `RequestInterceptor`, or use the client-credentials grant for service-to-service calls.

---

## 9. Distributed data & transactions

**Database per service** — each service owns its schema; no cross-service joins or foreign keys.

**Saga pattern** — a sequence of local transactions with **compensating transactions** on failure (there is no distributed 2PC in practice).
```
Order created → reserve inventory → charge payment → ship
     ↓ payment fails
  compensate: release inventory → cancel order
```
- **Choreography** — services react to each other's events (decentralised, no orchestrator, harder to trace).
- **Orchestration** — a central saga orchestrator drives the steps (clearer, but a new component).

**Other patterns to name:**
- **CQRS** — separate read and write models
- **Event Sourcing** — store state changes as an event log
- **Outbox pattern** — write the event to a DB table in the same transaction as the business data, then publish it (guarantees atomicity)
- **API Composition / BFF** — aggregate data for a specific client
- **Strangler Fig** — incrementally migrate a monolith

**CAP theorem** — in the presence of a network **P**artition you must choose between **C**onsistency and **A**vailability. Most microservice systems choose AP with **eventual consistency**.

---

## 10. Observability ⭐
The **three pillars**:
1. **Logs** — SLF4J + a correlation/trace id (MDC), shipped to ELK/Loki
2. **Metrics** — Micrometer → Prometheus → Grafana (latency, error rate, throughput, saturation)
3. **Traces** — Micrometer Tracing + Zipkin/Jaeger: follow one request across every service via a propagated `traceId`

```xml
<dependency><groupId>io.micrometer</groupId><artifactId>micrometer-tracing-bridge-brave</artifactId></dependency>
<dependency><groupId>io.zipkin.reporter2</groupId><artifactId>zipkin-reporter-brave</artifactId></dependency>
```
```properties
management.tracing.sampling.probability=1.0
management.zipkin.tracing.endpoint=http://localhost:9411/api/v2/spans
```
Health checks: `/actuator/health/liveness` and `/readiness` for Kubernetes probes.

---

## 11. A reference architecture (what to draw on a whiteboard)

```
                        ┌──────────────┐
   React SPA ─────────► │ API Gateway  │ ── auth, rate-limit, routing
                        └──────┬───────┘
                               │  lb://
        ┌──────────────┬───────┴────────┬───────────────┐
        ▼              ▼                ▼               ▼
   user-service   order-service   payment-service  inventory-service
        │              │ Feign          │                │
      MySQL          MySQL            MySQL            MySQL
        └──────────── Kafka events ────┴────────────────┘

  Cross-cutting:  Eureka (discovery) · Config Server · Zipkin (tracing)
                  Prometheus + Grafana (metrics) · ELK (logs)
                  Keycloak (OAuth2/OIDC) · Redis (cache/rate-limit)
```

**Ports used in the exercises:** Eureka 8761 · Gateway 8080 · Config 8888 · services 8081+ · Zipkin 9411.

---

## ⭐ Interview Gold — Microservices Rapid Fire

| Q | A |
|---|---|
| What are microservices? | Small, independently deployable services, each owning its data, communicating over lightweight protocols. |
| Monolith vs microservices — when do you choose which? | Monolith for small teams/simple domains; microservices when independent deployment, scaling or team autonomy justify the operational overhead. |
| Challenges of microservices? | Distributed transactions, network latency & failure, data consistency, testing, debugging/tracing, deployment complexity, service discovery. |
| Why database per service? | Loose coupling and independent schema evolution — a shared DB recreates the monolith's coupling. |
| How do services find each other? | A service registry (Eureka) with logical names + client-side load balancing. |
| Client-side vs server-side load balancing? | Caller picks from a cached instance list (no extra hop) vs a central LB. |
| What is an API Gateway and why? | Single entry point handling routing, auth, rate limiting, CORS, tracing — so services don't each reimplement them. |
| Gateway vs Load Balancer? | Application-aware routing + cross-cutting concerns vs pure traffic distribution. |
| Circuit breaker states? | CLOSED → OPEN (on failure threshold) → HALF_OPEN (probe) → CLOSED. |
| Why a circuit breaker? | Fail fast and stop cascading failure; give the downstream time to recover. |
| Resilience4j patterns? | CircuitBreaker, Retry, RateLimiter, Bulkhead, TimeLimiter. |
| What is a bulkhead? | Isolated resource pools so one failing dependency can't exhaust all threads. |
| Retry risks? | Duplicates on non-idempotent operations, and retry storms — use backoff + idempotency keys. |
| How do you handle distributed transactions? | Saga with compensating transactions (choreography or orchestration); avoid 2PC. |
| Sync vs async communication? | REST/Feign for immediate responses; events for decoupling and resilience. |
| How do you trace a request across services? | A propagated trace id (Micrometer Tracing/Sleuth) collected by Zipkin/Jaeger; log it via MDC. |
| How is authentication handled? | Centralised OAuth2/OIDC authorization server issuing JWTs; each service validates independently — stateless SSO. |
| OAuth2 vs OIDC? | Authorization framework vs an authentication layer adding the ID token. |
| Which OAuth2 flow for a SPA? | Authorization Code with PKCE (implicit is deprecated). |
| What is CAP? | Under a partition, pick consistency or availability — most microservices pick AP + eventual consistency. |
| How do you configure many services centrally? | Spring Cloud Config Server backed by Git + `@RefreshScope`. |
| How do you version an API? | URI (`/api/v1/`), header, or content negotiation — URI versioning is most common. |
| How do you deploy microservices? | Containerise with Docker, orchestrate with Kubernetes, automate with CI/CD; blue-green or canary releases. |
| Which technologies did you use? | Spring Boot 3, Spring Cloud (Eureka, Gateway, OpenFeign, Config), Resilience4j, MySQL, Docker. |

---

## 📚 References
- Spring Cloud: https://spring.io/projects/spring-cloud
- Spring Cloud Gateway: https://docs.spring.io/spring-cloud-gateway/reference/
- Spring Cloud Netflix (Eureka): https://docs.spring.io/spring-cloud-netflix/reference/
- Spring Cloud OpenFeign: https://docs.spring.io/spring-cloud-openfeign/reference/
- Resilience4j: https://resilience4j.readme.io/docs
- Spring Security OAuth2 Resource Server: https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html
- **microservices.io** — Chris Richardson's pattern catalogue: https://microservices.io/patterns/
- Martin Fowler — Microservices: https://martinfowler.com/articles/microservices.html
- Martin Fowler — MonolithFirst: https://martinfowler.com/bliki/MonolithFirst.html
- OAuth 2.0 / OIDC: https://oauth.net/2/ · https://openid.net/developers/how-connect-works/
- Micrometer Tracing: https://docs.micrometer.io/tracing/reference/
- *Building Microservices*, Sam Newman
