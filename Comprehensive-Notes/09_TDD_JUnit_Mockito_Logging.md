# 09 — TDD, JUnit 5, Mockito & SLF4J Logging

> Maps to [`Week1/week1_TestDD&LoggingF.md`](../Week1/week1_TestDD&LoggingF.md) — 7 sections, 30+ exercises.
> **References:** [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/), [Mockito](https://site.mockito.org/), [SLF4J Manual](https://www.slf4j.org/manual.html)

---

# PART 1 — Test-Driven Development

## 1.1 What is TDD?
Write the **test first**, then the minimum code to make it pass, then refactor.

```
      ┌──────────► RED ──────────┐
      │      (write a failing     │
      │       test — it must fail │
   REFACTOR      for the right    GREEN
 (clean up,      reason)      (write the simplest
  tests stay                   code that passes)
   green) ◄──────────────────────┘
```

**Benefits:** requirements clarified up front, executable documentation, high coverage by construction, fearless refactoring, better design (testable code is decoupled code), fast feedback.
**Costs:** slower initial pace, needs discipline, poorly-written tests become a maintenance burden.

**Related practices**
- **BDD** — Behaviour-Driven Development: `Given / When / Then`, business-readable (Cucumber, JBehave).
- **ATDD** — Acceptance-Test-Driven Development: acceptance criteria drive the work.

## 1.2 The testing pyramid
```
            ▲       /\        E2E / UI (Selenium, Cypress)   ← few, slow, brittle
            │      /  \
   fewer,  │     /____\      Integration (@SpringBootTest)   ← some
   slower  │    /      \
            │   /________\     Unit (JUnit + Mockito)         ← MANY, fast, isolated
            └──────────────►
```

| Layer | In a Spring Boot project | Annotation |
|---|---|---|
| Unit | one class, all deps mocked | `@ExtendWith(MockitoExtension.class)` |
| Slice — web | controller + JSON only | `@WebMvcTest` |
| Slice — data | repository + in-memory DB | `@DataJpaTest` |
| Integration | whole context | `@SpringBootTest` |
| E2E | deployed app | RestAssured / Selenium |

## 1.3 The AAA pattern ⭐
```java
@Test
void shouldApplyTenPercentBonus() {
    // ARRANGE — set up data and stubs
    Employee e = new Employee(1, "Pranav", 50_000);

    // ACT — invoke exactly ONE thing
    double bonus = service.calculateBonus(e);

    // ASSERT — verify the outcome
    assertEquals(5_000, bonus, 0.001);
}
```
BDD phrasing: **Given / When / Then** — same three steps.

## 1.4 FIRST principles of good tests
| Letter | Meaning |
|---|---|
| **F**ast | milliseconds — you run them constantly |
| **I**ndependent | no ordering or shared state between tests |
| **R**epeatable | same result on any machine, any time (no `LocalDate.now()` dependence, no live network) |
| **S**elf-validating | pass/fail automatically, no manual log reading |
| **T**imely | written just before the production code |

**Naming:** `should<Expected>When<Condition>()` — e.g. `shouldThrowExceptionWhenEmployeeNotFound()`. Or `methodName_stateUnderTest_expectedBehaviour()`.

---

# PART 2 — JUnit 5 (Jupiter) ⭐

## 2.1 Architecture
JUnit 5 = **Platform** (launcher/engine API) + **Jupiter** (the new programming & extension model) + **Vintage** (runs JUnit 3/4 tests).

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-test</artifactId>
  <scope>test</scope>
</dependency>
<!-- brings JUnit 5, Mockito, AssertJ, Hamcrest, JSONassert, Spring Test -->
```

## 2.2 Annotations ⭐
| Annotation | Purpose | JUnit 4 equivalent |
|---|---|---|
| `@Test` | marks a test method | `@Test` |
| `@BeforeEach` | run before **every** test | `@Before` |
| `@AfterEach` | run after every test | `@After` |
| `@BeforeAll` | once before all — must be `static` | `@BeforeClass` |
| `@AfterAll` | once after all — must be `static` | `@AfterClass` |
| `@Disabled("reason")` | skip | `@Ignore` |
| `@DisplayName("...")` | readable report name | — |
| `@Nested` | group related tests in an inner class | — |
| `@Tag("slow")` | filter test groups | `@Category` |
| `@RepeatedTest(5)` | run N times | — |
| `@ParameterizedTest` | run with multiple inputs | `@RunWith(Parameterized)` |
| `@TestMethodOrder` | control execution order | — |
| `@Timeout(1)` | fail if it takes too long | `@Test(timeout=)` |
| `@ExtendWith` | register an extension | `@RunWith` |

```java
@DisplayName("Calculator tests")
class CalculatorTest {
    private Calculator calc;

    @BeforeAll  static void initAll()  { System.out.println("once before"); }
    @BeforeEach void  init()           { calc = new Calculator(); }   // fresh per test
    @AfterEach  void  tearDown()       { calc = null; }
    @AfterAll   static void tearDownAll(){ System.out.println("once after"); }

    @Test
    @DisplayName("2 + 3 should equal 5")
    void shouldAddTwoNumbers() { assertEquals(5, calc.add(2, 3)); }

    @Nested
    @DisplayName("Division")
    class DivisionTests {
        @Test void shouldDivide(){ assertEquals(2, calc.divide(10, 5)); }
        @Test void shouldThrowOnZero(){
            assertThrows(ArithmeticException.class, () -> calc.divide(10, 0));
        }
    }
}
```
⭐ **Lifecycle note:** JUnit creates a **new test-class instance per test method** by default — that's why `@BeforeAll` must be static. Override with `@TestInstance(Lifecycle.PER_CLASS)`.

## 2.3 Assertions
```java
import static org.junit.jupiter.api.Assertions.*;

assertEquals(expected, actual);
assertEquals(expected, actual, "custom failure message");
assertEquals(3.14, actual, 0.001);            // delta for floating point
assertNotEquals(a, b);
assertTrue(cond);  assertFalse(cond);
assertNull(x);     assertNotNull(x);
assertSame(a, b);  assertNotSame(a, b);        // reference identity
assertArrayEquals(expArr, actArr);
assertIterableEquals(expList, actList);
assertLinesMatch(expected, actual);            // supports regex lines

// ALL assertions run, all failures reported together ⭐
assertAll("employee",
    () -> assertEquals("Pranav", emp.getName()),
    () -> assertEquals(50000,   emp.getSalary()),
    () -> assertNotNull(emp.getDept()));

// Exception testing ⭐
ArithmeticException ex = assertThrows(ArithmeticException.class, () -> calc.divide(10, 0));
assertEquals("/ by zero", ex.getMessage());
assertDoesNotThrow(() -> calc.divide(10, 2));

// Timeouts
assertTimeout(Duration.ofMillis(100), () -> service.fastCall());
assertTimeoutPreemptively(Duration.ofSeconds(1), () -> service.call());  // aborts

fail("not implemented yet");

// Assumptions — SKIP (not fail) when the precondition doesn't hold
assumeTrue("CI".equals(System.getenv("ENV")));
assumingThat(isDev(), () -> assertEquals(1, x));
```

**AssertJ** (bundled with `spring-boot-starter-test`) — fluent and far more readable, and what most teams actually use:
```java
import static org.assertj.core.api.Assertions.*;

assertThat(employees).hasSize(3)
                     .extracting(Employee::getName)
                     .containsExactly("Pranav", "Asha", "Ravi");
assertThat(emp.getSalary()).isGreaterThan(40000).isLessThanOrEqualTo(100000);
assertThat(name).isNotBlank().startsWith("Pr").endsWithIgnoringCase("AV");
assertThatThrownBy(() -> service.find(99))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("not found");
assertThat(map).containsEntry("k", "v").doesNotContainKey("x");
```

## 2.4 Parameterized tests ⭐
```java
@ParameterizedTest
@ValueSource(ints = {2, 4, 6, 8})
void shouldDetectEvenNumbers(int n) { assertTrue(n % 2 == 0); }

@ParameterizedTest
@ValueSource(strings = {"racecar", "level", "madam"})
void shouldDetectPalindromes(String s) { assertTrue(Util.isPalindrome(s)); }

@ParameterizedTest(name = "{0} + {1} = {2}")
@CsvSource({ "1, 1, 2", "2, 3, 5", "-1, 1, 0" })
void shouldAdd(int a, int b, int expected) { assertEquals(expected, calc.add(a, b)); }

@ParameterizedTest
@CsvFileSource(resources = "/data.csv", numLinesToSkip = 1)
void fromFile(String name, int age) { }

@ParameterizedTest
@MethodSource("employeeProvider")
void shouldValidate(Employee e, boolean valid) { assertEquals(valid, validator.check(e)); }
static Stream<Arguments> employeeProvider() {
    return Stream.of(
        Arguments.of(new Employee("Pranav", 50000), true),
        Arguments.of(new Employee("", -1),          false));
}

@ParameterizedTest
@EnumSource(Status.class)
void allStatuses(Status s) { assertNotNull(s.name()); }

@ParameterizedTest
@NullAndEmptySource
@ValueSource(strings = {"  "})
void shouldRejectBlank(String s) { assertThrows(IllegalArgumentException.class, () -> svc.save(s)); }
```

## 2.5 Test suites & ordering
```java
@Suite
@SelectPackages("com.cognizant.service")
@SelectClasses({CalculatorTest.class, EmployeeServiceTest.class})
@IncludeTags("fast")
class AllTests { }
```
```java
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderedTest {
    @Test @Order(1) void first()  { }
    @Test @Order(2) void second() { }
}
// Also: MethodOrderer.DisplayName / MethodName / Random
```
> ⚠️ **Order-dependent tests violate the I in FIRST.** Only use ordering for genuinely sequential integration scenarios.

## 2.6 Code coverage
JaCoCo measures line/branch coverage.
```xml
<plugin>
  <groupId>org.jacoco</groupId><artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.12</version>
  <executions>
    <execution><goals><goal>prepare-agent</goal></goals></execution>
    <execution><id>report</id><phase>test</phase><goals><goal>report</goal></goals></execution>
  </executions>
</plugin>
```
```bash
mvn clean test          # runs tests
mvn verify              # tests + coverage report at target/site/jacoco/index.html
```
⭐ **Interview:** *"Is 100% coverage the goal?"* → **No.** Coverage shows what was *executed*, not what was *verified*. 80% with meaningful assertions on critical paths beats 100% of trivial getter tests. Aim for high **branch** coverage on business logic.

---

# PART 3 — Mockito ⭐⭐

## 3.1 Why mock?
Isolate the class under test from its collaborators — the DB, network, clock, other services — so tests are fast, deterministic and focused.

**Test double vocabulary**
| Double | What it does |
|---|---|
| **Dummy** | passed but never used |
| **Stub** | returns canned answers |
| **Mock** | pre-programmed with expectations, **verifies interactions** |
| **Spy** | wraps a **real** object; records calls, can partially stub |
| **Fake** | a working lightweight implementation (in-memory repo, H2) |

## 3.2 Setup
```java
@ExtendWith(MockitoExtension.class)          // JUnit 5 (was @RunWith(MockitoJUnitRunner) in JUnit 4)
class EmployeeServiceTest {
    @Mock  private EmployeeRepository repository;   // create a mock
    @Mock  private EmailService emailService;
    @Spy   private AuditLogger auditLogger = new AuditLogger();
    @InjectMocks private EmployeeService service;   // inject the mocks into this
    @Captor private ArgumentCaptor<Employee> captor;
}
// or programmatically:
EmployeeRepository repo = mock(EmployeeRepository.class);
```

## 3.3 Stubbing
```java
when(repository.findById(1L)).thenReturn(Optional.of(emp));
when(repository.count()).thenReturn(5L);
when(repository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

// exceptions
when(repository.findById(99L)).thenThrow(new ResourceNotFoundException("nope"));

// MULTIPLE consecutive returns ⭐
when(service.next()).thenReturn(1).thenReturn(2).thenReturn(3);
when(service.next()).thenReturn(1, 2, 3);                     // same thing
when(service.call()).thenReturn("ok").thenThrow(new RuntimeException());

// VOID methods — use do* syntax ⭐
doNothing().when(emailService).send(anyString());
doThrow(new MailException("smtp down")).when(emailService).send(anyString());
doAnswer(inv -> { System.out.println(inv.getArgument(0)); return null; })
        .when(emailService).send(anyString());

// Spies MUST use doReturn (when(...) would call the real method)
doReturn("stubbed").when(spy).realMethod();
```

## 3.4 Argument matchers
```java
when(repo.findByName(anyString())).thenReturn(list);
when(repo.find(anyInt(), anyString(), any(Employee.class))).thenReturn(x);
when(repo.findById(eq(1L))).thenReturn(opt);
when(repo.findBySalaryGreaterThan(doubleThat(d -> d > 1000))).thenReturn(list);
when(repo.save(argThat(e -> e.getName().startsWith("P")))).thenReturn(emp);
anyList() anyMap() anySet() anyCollection() isNull() notNull() contains("x") startsWith("P")
```
⚠️ **Rule:** if you use a matcher for **one** argument, you must use matchers for **all** of them — wrap literals in `eq()`. Otherwise: `InvalidUseOfMatchersException`.

## 3.5 Verifying interactions ⭐
```java
verify(repository).save(emp);                          // exactly once (default)
verify(repository, times(2)).findById(1L);
verify(repository, never()).delete(any());
verify(repository, atLeastOnce()).findAll();
verify(repository, atLeast(2)).count();
verify(repository, atMost(3)).count();
verify(repository, timeout(100)).asyncCall();

verifyNoInteractions(emailService);
verifyNoMoreInteractions(repository);                  // after your other verifies

// ORDER of interactions ⭐
InOrder inOrder = inOrder(repository, emailService);
inOrder.verify(repository).save(emp);
inOrder.verify(emailService).send(anyString());

// ArgumentCaptor — inspect what was actually passed ⭐
ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
verify(repository).save(captor.capture());
assertEquals("Pranav", captor.getValue().getName());
assertThat(captor.getAllValues()).hasSize(2);
```

## 3.6 Complete worked example
```java
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock private EmployeeRepository repository;
    @Mock private EmailService emailService;
    @InjectMocks private EmployeeService service;

    @Test
    @DisplayName("should return employee when it exists")
    void shouldReturnEmployeeWhenFound() {
        // Arrange
        Employee emp = new Employee(1L, "Pranav", 50_000);
        when(repository.findById(1L)).thenReturn(Optional.of(emp));

        // Act
        Employee result = service.getById(1L);

        // Assert
        assertThat(result.getName()).isEqualTo("Pranav");
        verify(repository).findById(1L);
        verifyNoInteractions(emailService);
    }

    @Test
    void shouldThrowWhenEmployeeMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void shouldSendWelcomeEmailOnCreate() {
        Employee emp = new Employee(null, "Asha", 60_000);
        when(repository.save(any(Employee.class))).thenAnswer(i -> {
            Employee e = i.getArgument(0); e.setId(2L); return e;
        });

        service.create(emp);

        ArgumentCaptor<String> mail = ArgumentCaptor.forClass(String.class);
        verify(emailService).send(mail.capture());
        assertThat(mail.getValue()).contains("Asha");
    }
}
```

## 3.7 Limitations & alternatives
Mockito **cannot** (by default) mock: static methods, constructors, `final` classes/methods, private methods, `equals`/`hashCode`.
- `mockito-inline` (or Mockito 5's default mock maker) enables **static mocking**:
```java
try (MockedStatic<LocalDate> m = mockStatic(LocalDate.class)) {
    m.when(LocalDate::now).thenReturn(LocalDate.of(2026, 1, 1));
}
```
- Better design usually removes the need: inject a `Clock`, extract a collaborator interface, use DI instead of `new`.
- `UnnecessaryStubbingException` — strict stubs (default in JUnit 5 extension) flag stubs never used; that's a signal, not noise. Use `lenient()` sparingly.

---

# PART 4 — Testing Spring Boot ⭐

## 4.1 Service unit test (no Spring context — fast)
```java
@ExtendWith(MockitoExtension.class)      // pure Mockito, no Spring
class CountryServiceTest { @Mock CountryRepository repo; @InjectMocks CountryService svc; }
```

## 4.2 `@WebMvcTest` — controller slice with MockMvc
```java
@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired  private MockMvc mockMvc;
    @Autowired  private ObjectMapper objectMapper;
    @MockBean   private EmployeeService service;      // replaces the real bean in the context

    @Test
    void shouldReturnEmployeeById() throws Exception {
        when(service.getById(1L)).thenReturn(new Employee(1L, "Pranav", 50000));

        mockMvc.perform(get("/api/employees/1").accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("Pranav"))
               .andExpect(jsonPath("$.salary").value(50000))
               .andDo(print());
    }

    @Test
    void shouldCreateEmployee() throws Exception {
        Employee in = new Employee(null, "Asha", 60000);
        when(service.create(any())).thenReturn(new Employee(2L, "Asha", 60000));

        mockMvc.perform(post("/api/employees")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(objectMapper.writeValueAsString(in)))
               .andExpect(status().isCreated())
               .andExpect(header().exists("Location"))
               .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void shouldReturn404WhenMissing() throws Exception {
        when(service.getById(99L)).thenThrow(new ResourceNotFoundException("Employee", 99));
        mockMvc.perform(get("/api/employees/99"))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn400OnValidationFailure() throws Exception {
        mockMvc.perform(post("/api/employees")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content("{\"name\":\"\",\"salary\":-5}"))
               .andExpect(status().isBadRequest());
    }
}
```
> `@WebMvcTest` loads **only** the web layer (controllers, filters, converters, `@ControllerAdvice`) — not services or repositories. Supply those with `@MockBean`.
> In Spring Boot 3.4+, `@MockBean` is deprecated in favour of `@MockitoBean`.

## 4.3 `@DataJpaTest` — repository slice
```java
@DataJpaTest                                   // in-memory DB, rolls back after each test
class EmployeeRepositoryTest {
    @Autowired private TestEntityManager em;
    @Autowired private EmployeeRepository repo;

    @Test
    void shouldFindByDepartment() {
        em.persist(new Employee("Pranav", 50000, "IT"));
        em.flush();

        List<Employee> found = repo.findByDepartment("IT");

        assertThat(found).hasSize(1).extracting(Employee::getName).containsExactly("Pranav");
    }
}
```

## 4.4 `@SpringBootTest` — full integration
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional                     // rolls back after each test
class EmployeeIntegrationTest {
    @Autowired private TestRestTemplate restTemplate;
    @Autowired private EmployeeRepository repo;

    @Test
    void shouldCreateAndFetchEmployee() {
        ResponseEntity<Employee> res = restTemplate.postForEntity(
            "/api/employees", new Employee(null, "Ravi", 70000), Employee.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(repo.count()).isEqualTo(1);
    }
}
```

| Annotation | Loads | Speed | Use |
|---|---|---|---|
| `@ExtendWith(MockitoExtension)` | nothing | ⚡⚡⚡ | pure unit tests |
| `@WebMvcTest` | web layer only | ⚡⚡ | controllers |
| `@DataJpaTest` | JPA + embedded DB | ⚡⚡ | repositories |
| `@SpringBootTest` | full context | 🐢 | end-to-end wiring |

**Other useful annotations:** `@MockBean`/`@MockitoBean`, `@SpyBean`, `@TestConfiguration`, `@Sql("data.sql")`, `@DirtiesContext`, `@ActiveProfiles`, `@TestPropertySource`, `@Testcontainers` (real MySQL in Docker for integration tests).

---

# PART 5 — Logging: SLF4J + Logback ⭐

## 5.1 Why a logging facade?
**SLF4J** (Simple Logging Facade for Java) is an **abstraction** — you code against the SLF4J API and bind whichever implementation you want (Logback, Log4j2, java.util.logging) at deployment time. Libraries can log without forcing a framework on the application.

```
Your code → SLF4J API → binding → Logback / Log4j2 / JUL
```
Spring Boot's `spring-boot-starter-logging` (included in `spring-boot-starter`) ships **SLF4J + Logback** by default.

⭐ **Why log instead of `System.out.println`?** Levels and runtime filtering, output destinations (file/rolling/remote/JSON), timestamps + thread + class context, per-package configuration, performance (lazy formatting), no recompilation to change verbosity, and centralised aggregation (ELK/Splunk).

## 5.2 Usage
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmployeeService {
    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);
    // with Lombok: @Slf4j on the class gives you `log` for free

    public Employee getById(Long id) {
        log.trace("entering getById");
        log.debug("Fetching employee id={}", id);              // ⭐ parameterised — no concat
        try {
            return repo.findById(id)
                       .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        } catch (ResourceNotFoundException e) {
            log.warn("Employee not found id={}", id);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected failure fetching employee id={}", id, e);  // exception LAST
            throw e;
        }
    }
}
```

## 5.3 Log levels ⭐
| Level | When to use | Production? |
|---|---|---|
| `TRACE` | very fine-grained flow | ❌ |
| `DEBUG` | diagnostic detail for developers | ❌ (on-demand) |
| `INFO` | significant business events (startup, order placed) | ✅ **default** |
| `WARN` | unexpected but recoverable (retry, fallback, deprecated use) | ✅ |
| `ERROR` | failures needing attention (exception, external system down) | ✅ |
Ordering: `TRACE < DEBUG < INFO < WARN < ERROR`. Setting level `INFO` enables INFO, WARN and ERROR.

## 5.4 Parameterised logging — why it matters
```java
log.debug("User " + id + " has " + n + " orders");   // ❌ concatenation happens ALWAYS
log.debug("User {} has {} orders", id, n);           // ✅ formatted ONLY if DEBUG is enabled
log.error("Failed for user {}", id, exception);      // ✅ exception as the last arg (no {})
if (log.isDebugEnabled()) log.debug(expensive());    // guard only for expensive computation
```

## 5.5 Configuration

**`application.properties` (Spring Boot — the quick way)**
```properties
logging.level.root=INFO
logging.level.com.cognizant=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.orm.jdbc.bind=TRACE
logging.file.name=logs/application.log
logging.logback.rollingpolicy.max-file-size=10MB
logging.logback.rollingpolicy.max-history=30
logging.pattern.console=%d{HH:mm:ss.SSS} %-5level [%thread] %logger{36} - %msg%n
```

**`logback-spring.xml` (full control — Exercise 3: appenders)**
```xml
<configuration>
    <!-- CONSOLE APPENDER -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} %highlight(%-5level) [%thread] %cyan(%logger{36}) - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- ROLLING FILE APPENDER -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>logs/application-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>10MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>1GB</totalSizeCap>
        </rollingPolicy>
        <encoder><pattern>%d %-5level [%thread] %logger{36} - %msg%n</pattern></encoder>
    </appender>

    <!-- ERROR-ONLY FILE with a level filter -->
    <appender name="ERROR_FILE" class="ch.qos.logback.core.FileAppender">
        <file>logs/error.log</file>
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>ERROR</level><onMatch>ACCEPT</onMatch><onMismatch>DENY</onMismatch>
        </filter>
        <encoder><pattern>%d %-5level %logger - %msg%n</pattern></encoder>
    </appender>

    <!-- ASYNC wrapper — non-blocking logging -->
    <appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender">
        <appender-ref ref="FILE"/>
    </appender>

    <!-- profile-specific -->
    <springProfile name="dev">
        <root level="DEBUG"><appender-ref ref="CONSOLE"/></root>
    </springProfile>
    <springProfile name="prod">
        <root level="INFO">
            <appender-ref ref="ASYNC"/><appender-ref ref="ERROR_FILE"/>
        </root>
    </springProfile>

    <logger name="com.cognizant" level="DEBUG" additivity="false">
        <appender-ref ref="CONSOLE"/>
    </logger>
</configuration>
```

**Pattern tokens:** `%d` date · `%level` · `%thread` · `%logger{36}` · `%msg` · `%n` · `%X{key}` MDC · `%ex` exception · `%L` line (slow — avoid in prod).

## 5.6 MDC — correlating logs across a request ⭐
```java
MDC.put("traceId", UUID.randomUUID().toString());
MDC.put("userId", currentUser.getId());
try { ... } finally { MDC.clear(); }        // ALWAYS clear — threads are pooled
```
Then add `%X{traceId}` to the pattern. In microservices, propagate the trace id via an HTTP header (Micrometer Tracing / Sleuth does this automatically) so one request can be followed across services.

## 5.7 Logging best practices
1. `private static final Logger log = LoggerFactory.getLogger(X.class);`
2. Always use `{}` placeholders, never string concatenation.
3. Pass the exception as the **last argument** — `log.error("msg {}", id, e)` — to get the stack trace.
4. **Never log secrets:** passwords, tokens, full card numbers, Aadhaar/PAN, OTPs. Mask PII.
5. Log at boundaries (controller entry/exit, external calls), not inside tight loops.
6. Don't log *and* rethrow the same exception — you'll get duplicate stack traces.
7. Use MDC/trace ids for correlation.
8. Roll and cap log files; ship to a central store (ELK, Splunk, CloudWatch).
9. Prefer structured/JSON logging in production (`logstash-logback-encoder`).
10. Use `@Slf4j` (Lombok) or Spring AOP (`@Around`) for cross-cutting method logging — see [10_Spring_Core_Maven_AOP.md](10_Spring_Core_Maven_AOP.md).

### SLF4J vs Log4j2 vs Logback vs JUL
| | SLF4J | Logback | Log4j2 | JUL |
|---|---|---|---|---|
| Role | **facade/API** | implementation | implementation | JDK built-in |
| Speed | n/a | fast | fastest (async, LMAX Disruptor) | slow |
| Spring Boot default | ✅ API | ✅ impl | opt-in starter | — |
> Log4j **1.x** is end-of-life. The **Log4Shell (CVE-2021-44228)** vulnerability affected Log4j **2.x** below 2.17 — worth knowing the name.

---

## ⭐ Interview Gold — Testing & Logging Rapid Fire

| Q | A |
|---|---|
| What is TDD? | Red → Green → Refactor: write the failing test first, then minimal code, then clean up. |
| Benefits of TDD? | Clear requirements, high coverage, safe refactoring, better decoupled design, living documentation. |
| AAA pattern? | Arrange, Act, Assert (Given/When/Then in BDD). |
| FIRST principles? | Fast, Independent, Repeatable, Self-validating, Timely. |
| Unit vs integration test? | One class with mocked collaborators vs multiple real components wired together. |
| JUnit 4 vs 5? | Modular architecture (Platform/Jupiter/Vintage), new annotation names, `@ExtendWith` instead of `@RunWith`, lambda assertions, `@Nested`, better parameterized tests, Java 8+ required. |
| `@BeforeEach` vs `@BeforeAll`? | Per test vs once per class (must be `static`). |
| How do you test an exception? | `assertThrows(Type.class, () -> code())` and assert on the returned exception. |
| What is `assertAll`? | Groups assertions so all are evaluated and all failures are reported. |
| Why mock? | Isolate the unit, remove slow/nondeterministic dependencies, control edge cases. |
| Mock vs Stub vs Spy? | Verifies interactions / returns canned data / wraps a real object with partial stubbing. |
| `@Mock` vs `@MockBean`? | Plain Mockito mock vs a mock registered in the **Spring context**, replacing the real bean. |
| `when()` vs `doReturn()`? | `when()` calls the real method on spies — use `doReturn().when(spy)` for spies and void methods. |
| How do you stub a void method? | `doNothing()`/`doThrow()`/`doAnswer()`.`when(mock).method()`. |
| What is `ArgumentCaptor` for? | Capturing and asserting on the actual arguments passed to a mock. |
| Can Mockito mock static/final? | Not by default — `mockito-inline` / Mockito 5 enables `mockStatic`; better to redesign for injection. |
| `@WebMvcTest` vs `@SpringBootTest`? | Web slice only (fast) vs full application context (slow, end-to-end). |
| What is MockMvc? | A test harness that exercises controllers through the Spring MVC stack without a real server. |
| Is 100% coverage the goal? | No — coverage measures execution, not verification. Target meaningful branch coverage on business logic. |
| Why SLF4J and not `System.out`? | Levels, appenders, formatting, per-package config, performance, aggregation. |
| Log levels in order? | TRACE < DEBUG < INFO < WARN < ERROR. |
| Why `log.debug("x={}", x)` over concatenation? | The string is only built if the level is enabled — avoids wasted work. |
| SLF4J vs Logback? | Facade/API vs the actual logging implementation. |
| Spring Boot's default logging? | SLF4J API + Logback via `spring-boot-starter-logging`. |
| What is MDC? | Mapped Diagnostic Context — per-thread key/values (e.g. traceId) rendered with `%X{}`. |
| What should you never log? | Passwords, tokens, card numbers, OTPs, personal identifiers. |

---

## 📚 References
- JUnit 5 User Guide: https://junit.org/junit5/docs/current/user-guide/
- Mockito Javadoc (the class doc is a full tutorial): https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html
- AssertJ Fluent Assertions: https://assertj.github.io/doc/
- Spring Boot — Testing: https://docs.spring.io/spring-boot/reference/testing/index.html
- Spring Framework — MockMvc: https://docs.spring.io/spring-framework/reference/testing/spring-mvc-test-framework.html
- SLF4J Manual: https://www.slf4j.org/manual.html
- Logback Manual: https://logback.qos.ch/manual/
- JaCoCo: https://www.jacoco.org/jacoco/trunk/doc/
- Testcontainers: https://testcontainers.com/
- Baeldung — Testing with Spring Boot: https://www.baeldung.com/spring-boot-testing
- Martin Fowler — Test Double / Test Pyramid: https://martinfowler.com/bliki/TestDouble.html
