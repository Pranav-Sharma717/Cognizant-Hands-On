
---

## 1. Design Patterns & Principles

| Principle | What it means (in Java) | Typical use‑case | Example (Spring‑style) |
|-----------|------------------------|-----------------|------------------------|
| **SOLID** | Five object‑oriented design rules | Keep classes small, testable, extensible | `UserService` implements `UserRepository` → **Dependency Inversion** |
| **DRY** (Don’t Repeat Yourself) | Extract common logic to reusable components | Utility classes, shared config beans | `CommonUtils`, `MessageSource` |
| **KISS** (Keep It Simple, Stupid) | Prefer clear, straightforward code over clever tricks | Simple DTOs, direct method calls | `UserDto` ← `UserEntity` via MapStruct |
| **YAGNI** (You Aren’t Gonna Need It) | Avoid adding features “just in case” | Skip premature abstractions | No generic “CrudService” until you have >2 entities. |
| **Composition over Inheritance** | Favor object composition to reuse behavior | Spring’s `@Autowired` injection, strategy pattern | `PaymentProcessor` composed of `CreditCardStrategy`, `PayPalStrategy`. |
| **Factory Method / Abstract Factory** | Hide object creation behind an interface | Creating JPA repositories, REST clients | `RestTemplateBuilder` → `RestTemplate` |
| **Strategy** | Swap algorithms at runtime | Validation, sorting, caching policies | `CacheStrategy` interface with `RedisCacheStrategy`, `InMemoryCacheStrategy`. |
| **Singleton** | One‑instance global object (managed by Spring) | `ApplicationContext`, `ObjectMapper` | `@Component` + default singleton scope |
| **Decorator** | Add responsibilities without changing original class | Servlet filters, Spring AOP | `LoggingFilter` decorates a `UserController`. |
| **Observer (Publish‑Subscribe)** | Notify many listeners of an event | Domain events, WebSocket broadcast | `ApplicationEventPublisher` → `UserCreatedEvent`. |

### How they appear in a Spring‑Boot project
```java
@Service                     // Singleton bean → DI container handles lifecycle
@RequiredArgsConstructor     // Lombok: inject final fields (DI = Dependency Injection)
public class OrderService {
    private final OrderRepository repo;               // Repository = DAO (Factory)
    private final PaymentStrategy paymentStrategy;    // Strategy pattern

    public Order place(OrderDto dto) {
        // KISS + DRY: delegate validation to a validator bean
        validator.validate(dto);                     // Observer‑style event if needed
        Order order = mapper.toEntity(dto);
        paymentStrategy.pay(order);                  // Strategy call
        return repo.save(order);                     // DAO (Repository) pattern
    }
}
```

---

## 2. Data Structures & Algorithms (Java‑centric)

| DS / Algo | Typical Java implementation | When to use it in a web app |
|-----------|-----------------------------|------------------------------|
| **Array / `ArrayList`** | `List<T> list = new ArrayList<>();` | Small mutable collections, ordering matters. |
| **Linked List** (`LinkedList`) | `Deque<T> dq = new LinkedList<>();` | Frequent insert/remove at both ends, but rare random access. |
| **HashMap / `ConcurrentHashMap`** | `Map<K,V> map = new HashMap<>();` | Caching, lookup tables, request‑scoped data (e.g., session attributes). |
| **TreeMap / `NavigableMap`** | `TreeMap<K,V>` (red‑black tree) | Sorted maps, range queries, pagination on key order. |
| **Set / `HashSet`, `TreeSet`** | `Set<T> set = new HashSet<>();` | De‑duplication, membership tests. |
| **PriorityQueue / `Heap`** | `Queue<T> pq = new PriorityQueue<>(Comparator.comparingInt(...));` | Scheduling, rate‑limiting, background job priority. |
| **Stack (`Deque`)** | `Deque<T> stack = new ArrayDeque<>();` | DFS recursion simulation, undo/redo. |
| **Binary Search** | `Collections.binarySearch(sortedList, key);` | Look‑ups on sorted lists (e.g., timeline). |
| **Sorting (QuickSort, MergeSort, TimSort)** | `Collections.sort(list)` (TimSort) | Ordering results before sending to client. |
| **Graph algorithms (BFS/DFS, Dijkstra)** | Use adjacency list (`Map<Vertex, List<Edge>>`) or libraries like JGraphT. | Recommendation engines, shortest‑path routing, micro‑service dependency analysis. |
| **String algorithms (KMP, regex)** | `Pattern.compile(...).matcher(str).find();` | Validation, URL pattern matching, log parsing. |
| **Concurrency utilities** (`java.util.concurrent`) | `ExecutorService`, `CompletableFuture`, `BlockingQueue` | Async processing, non‑blocking I/O, parallel streams. |

**Performance tip:**  
- Use **`ArrayList`** for random access (`O(1)`) and **`LinkedList`** only when you truly need constant‑time inserts/removes at both ends.  
- For **thread‑safe** maps, prefer **`ConcurrentHashMap`** over synchronizing a `HashMap`.  
- For **large collections** that never change, consider **`ImmutableList`** (Guava) to avoid accidental mutation.

---

## 3. PL/SQL Programming (Oracle)

| Concept | Java ↔ PL/SQL bridge | Typical use in a Java‑Spring stack |
|---------|----------------------|------------------------------------|
| **Stored Procedure** | Call via `JdbcTemplate` or `SimpleJdbcCall` | Business logic that must run close to the data (e.g., bulk insert, complex calculations). |
| **Function** | `CallableStatement` returns a value | Compute a derived field (e.g., `GET_DISCOUNT(p_customer_id)`). |
| **Package** | Grouped procedures/functions | Keeps 100‑line PL/SQL modules tidy; you import the package name in calls. |
| **Cursor / Ref Cursor** | `ResultSet` mapping | Stream large result sets lazily (e.g., `SELECT * FROM large_table WHERE ...`). |
| **Exception handling** | Translate Oracle errors (`ORA‑xxxxx`) to Spring `DataAccessException` | Central error handling, retry logic. |
| **Transaction control** | `@Transactional` works with Oracle’s **XA** or **JTA** when you use a **JTA‑aware** `DataSource`. | Guarantees atomicity across multiple PL/SQL calls. |

**Sample call using Spring JDBC:**
```java
@Autowired
private JdbcTemplate jdbc;

public List<User> findByStatus(String status) {
    SimpleJdbcCall call = new SimpleJdbcCall(jdbc)
            .withProcedureName("user_pkg.get_by_status")
            .returningResultSet("users", (rs, rowNum) ->
                    new User(rs.getLong("id"), rs.getString("name"), rs.getString("status")));

    Map<String, Object> in = Collections.singletonMap("p_status", status);
    Map<String, Object> out = call.execute(in);
    return (List<User>) out.get("users");
}
```

---

## 4. Test‑Driven Development (TDD) & Logging Framework

| TDD Phase | Goal | Typical Java tools |
|-----------|------|--------------------|
| **Red**   | Write a failing test that describes the desired behavior. | `JUnit 5`, `AssertJ`, `Mockito` for mocks. |
| **Green** | Implement just enough code to make the test pass. | Write minimal method body, maybe a stub. |
| **Refactor** | Clean up implementation while keeping tests green. | IDE refactoring, static analysis (`SpotBugs`, `PMD`). |

### Typical test layers in a Spring Boot project
| Layer | Test type | Annotation / framework |
|-------|-----------|------------------------|
| **Unit** | Pure Java, no Spring context | `@ExtendWith(MockitoExtension.class)`, `@MockBean` |
| **Slice** | Partial Spring context (e.g., `@WebMvcTest`, `@DataJpaTest`) | Faster than full boot. |
| **Integration** | Full `@SpringBootTest` (loads all beans, DB) | Use **Testcontainers** for an isolated DB. |
| **Contract** | Verify API contract (OpenAPI, Pact) | `spring-cloud-contract`, `wiremock`. |

### Logging in Java (SLF4J + Logback – de‑facto standard)

```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderService {
    public void place(Order order) {
        log.info("Placing order id={} for user={}", order.getId(), order.getUserId());
        try {
            // business logic …
        } catch (Exception ex) {
            log.error("Failed to place order id={}", order.getId(), ex);
            throw ex; // rethrow or handle
        }
    }
}
```

**Key log‑back configuration snippets (`logback‑spring.xml`):**
```xml
<configuration>
    <property name="LOG_PATTERN"
          value="%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %logger{36} - %msg%n"/>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
    </appender>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/app.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/app.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <maxHistory>30</maxHistory>
            <timeBasedFileNamingAndTriggeringPolicy
                 class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>10MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
        </rollingPolicy>
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```
- **`INFO`** for normal flow, **`DEBUG`** for detailed state, **`WARN`/`ERROR`** for problems.
- Use **MDC** (`Mapped Diagnostic Context`) to add request‑scoped data (e.g., correlation id).

---

## 5. Spring Core + Maven

| Maven Concept | Example (pom.xml) | Purpose |
|---------------|-------------------|---------|
| **Parent** | `<parent><groupId>org.springframework.boot</groupId>...` | Inherit Spring Boot defaults (dependency management, plugin versions). |
| **Dependency Management** | `<dependencies>…<dependency><groupId>org.springframework</groupId><artifactId>spring-context</artifactId></dependency>…</dependencies>` | Pull the core Spring IoC container, AOP, etc. |
| **Plugins** | `spring-boot-maven-plugin` for `repackage` and running `java -jar`. | Build executable JARs with embedded Tomcat. |
| **Profiles** | `<profile><id>dev</id>…</profile>` | Switch DB configs, logging levels, etc. |
| **Properties** | `<java.version>17</java.version>` | Central version control. |

**Core Spring concepts you’ll meet daily**

| Concept | What it does | Typical Java annotation |
|---------|--------------|--------------------------|
| **IoC Container** | Manages bean lifecycle, dependency injection. | `@Component`, `@Service`, `@Repository` |
| **Bean Scopes** | Singleton (default), prototype, request, session. | `@Scope("prototype")` |
| **AOP** | Cross‑cutting concerns (transactions, logging). | `@Aspect`, `@Around` |
| **Transaction Management** | Declarative rollback/commit. | `@Transactional` |
| **Event Publishing** | Decouple producers & consumers. | `ApplicationEventPublisher` |
| **Configuration Properties** | Type‑safe binding of `application.yml`. | `@ConfigurationProperties(prefix="app")` |

---

## 6. Spring Data JPA + Spring Boot + Hibernate

| Layer | Technology | Typical annotation / class |
|-------|------------|----------------------------|
| **Entity** | JPA (Hibernate) | `@Entity`, `@Table`, `@Id`, `@GeneratedValue` |
| **Repository** | Spring Data JPA | `interface UserRepo extends JpaRepository<User, Long>` |
| **Service** | Spring Business Logic | `@Service` + `@Transactional` |
| **Controller** | Spring MVC / WebFlux | `@RestController`, `@GetMapping("/users")` |
| **DTO / Mapper** | MapStruct / ModelMapper | `UserDto toDto(User e);` |
| **Boot auto‑configuration** | `spring-boot-starter-data-jpa` pulls in `hibernate-core`, connection pool (HikariCP). |

### Example entity & repo
```java
@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    private String email;
}
```

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByEmailContainingIgnoreCase(String fragment);
}
```

### Custom query (JPQL / native)
```java
@Query("SELECT u FROM User u WHERE u.createdAt > :since")
List<User> recentUsers(@Param("since") LocalDateTime since);
```
or
```java
@Query(value = "SELECT * FROM users WHERE status = ?1", nativeQuery = true)
List<User> findByStatusNative(String status);
```

### Transactional service sample
```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repo;
    private final PasswordEncoder encoder;

    @Transactional
    public User register(RegisterCmd cmd) {
        User user = new User();
        user.setUsername(cmd.getUsername());
        user.setEmail(cmd.getEmail());
        user.setPassword(encoder.encode(cmd.getPassword()));
        return repo.save(user);
    }
}
```

### Boot application properties (common)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/appdb
    username: app
    password: secret
  jpa:
    hibernate:
      ddl-auto: update           # create‑drop, validate, none
    show-sql: true
    properties:
      hibernate.format_sql: true
logging:
  level:
    org.hibernate.SQL: debug
    com.myapp: info
```

---

## 7. Putting It All Together – Typical Request Flow

```
[HTTP Client] ──► (Spring MVC DispatcherServlet)
                 │
                 ▼
           @RestController (handles /users POST)
                 │
                 ▼
            Service Layer (business rules, @Transactional)
                 │
        ┌────────┴─────────┐
        │                  │
  Repository (JPA)   EventPublisher
        │                  │
        ▼                  ▼
   Hibernate (ORM)   Async Listener (e.g., Kafka)
        │
        ▼
   Relational DB (PostgreSQL / Oracle)
```

- **Logging** (SLF4J) is woven at each layer via **AOP** (`@Around` on service methods).  
- **TDD** ensures each layer has its own test suite (unit for service, slice for repository, integration for controller).  
- **Design patterns** (Strategy for payment, Observer for domain events, Factory for DTO → Entity conversion) keep the code maintainable.

---

## Quick Reference Cheat‑Sheet (Copy‑Paste)

```markdown
# Java Full‑Stack Engineer – Core Knowledge

## Design Patterns & SOLID
- Factory, Strategy, Observer, Decorator, Singleton
- SOLID: Single‑res‑p, OCP, LSP, ISP, DIP
- KISS, DRY, YAGNI

## Data Structures (Java)
- List/ArrayList, Set/HashSet, Map/HashMap, Queue/PriorityQueue
- TreeMap/TreeSet for sorted data
- ConcurrentHashMap, BlockingQueue for multi‑threaded apps

## Algorithms (What to know)
- Sorting (TimSort via Collections.sort)
- Searching (binarySearch on sorted arrays)
- Graph traversal (BFS/DFS via adjacency list)
- String pattern matching (regex, KMP optional)

## PL/SQL Integration
- `SimpleJdbcCall` / `CallableStatement`
- `@Transactional` for consistency
- Map result sets → DTOs (RowMapper, BeanPropertyRowMapper)

## TDD Stack
- JUnit‑5 + AssertJ + Mockito
- Spring Test slices: `@WebMvcTest`, `@DataJpaTest`
- Testcontainers for DB‑integration

## Logging
- SLF4J + Logback (MDC for request correlation)
- `log.info("msg {}", var);`
- Config in `logback‑spring.xml`

## Spring Core + Maven
- Parent: `spring-boot-starter-parent`
- Plugins: `spring-boot-maven-plugin`
- Bean scopes, AOP, Events, ConfigurationProperties

## Spring Data JPA + Hibernate
- `@Entity`, `JpaRepository`, `@Query`
- `@Transactional` in service
- `application.yml` → datasource + JPA settings

## Typical Request Flow
Client → DispatcherServlet → @RestController → Service (@Transactional) → Repository (JPA) → DB
```

---

