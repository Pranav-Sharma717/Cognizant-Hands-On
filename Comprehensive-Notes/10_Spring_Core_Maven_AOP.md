# 10 — Spring Core, Maven & AOP

> Maps to [`Week 2/week2_SpringCore_Maven.md`](../Week%202/week2_SpringCore_Maven.md) — 9 exercises.
> **References:** [Spring Framework Reference](https://docs.spring.io/spring-framework/reference/), [Maven Guides](https://maven.apache.org/guides/)

---

# PART 1 — IoC & Dependency Injection ⭐⭐

## 1.1 Inversion of Control
Normally **your code** controls the flow and creates its collaborators. With IoC, that control is **inverted** — a container creates objects, wires them together, and manages their lifecycle. Your code just declares what it needs.

```java
// ❌ WITHOUT Spring — the class controls creation (tight coupling)
public class OrderService {
    private final OrderRepository repo = new MySqlOrderRepository();   // hard-wired
    private final EmailService mail = new SmtpEmailService();
}
// Problems: can't swap implementations, can't unit test (no mocks), duplicated wiring everywhere

// ✅ WITH Spring — the container injects
@Service
public class OrderService {
    private final OrderRepository repo;
    private final EmailService mail;
    public OrderService(OrderRepository repo, EmailService mail) {   // constructor injection
        this.repo = repo; this.mail = mail;
    }
}
```

**Benefits:** loose coupling · testability (inject mocks) · single place for configuration · easier maintenance · reusable components · declarative cross-cutting concerns (transactions, security, caching) via proxies.

> **IoC** is the *principle*, **DI** is the *mechanism*, the **ApplicationContext** is the *container*. See also [06_Design_Patterns_SOLID.md §4.2](06_Design_Patterns_SOLID.md).

## 1.2 The container
| | `BeanFactory` | `ApplicationContext` |
|---|---|---|
| Instantiation | **lazy** (on `getBean`) | **eager** for singletons at startup |
| Features | basic DI only | + i18n, events, AOP, resource loading, auto post-processors |
| Use | memory-constrained | **everything** — this is what Spring Boot creates |

```java
ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
ApplicationContext ctx = SpringApplication.run(App.class, args);   // Spring Boot
OrderService svc = ctx.getBean(OrderService.class);
```

## 1.3 Three types of injection ⭐
```java
// 1. CONSTRUCTOR INJECTION ✅ RECOMMENDED
@Service
public class EmployeeService {
    private final EmployeeRepository repo;
    public EmployeeService(EmployeeRepository repo) { this.repo = repo; }
    // @Autowired is OPTIONAL since Spring 4.3 for a single constructor
}

// 2. SETTER INJECTION — for optional/reconfigurable dependencies
@Autowired
public void setCache(CacheService cache) { this.cache = cache; }

// 3. FIELD INJECTION ❌ discouraged
@Autowired private EmployeeRepository repo;
```

**Why constructor injection wins**
1. Required dependencies are enforced — the object can't exist half-built
2. Fields can be `final` → **immutable**, thread-safe
3. Unit-testable **without** Spring — just `new EmployeeService(mockRepo)`
4. Circular dependencies fail **fast at startup** instead of silently working
5. A long constructor visibly signals too many responsibilities (SRP smell)

**Circular dependency** — A needs B, B needs A. With field/setter injection Spring may resolve it via a partially-initialised bean; with constructor injection it throws `BeanCurrentlyInCreationException`. **Fix:** refactor (extract a third bean), or as a last resort use `@Lazy`. Spring Boot 2.6+ disallows it by default (`spring.main.allow-circular-references=true` to re-enable — a code smell).

## 1.4 Autowiring & disambiguation
```java
@Autowired                              // by TYPE
@Autowired(required = false)            // optional
@Qualifier("mysqlRepo")                 // by NAME when multiple candidates exist
@Primary                                // the default choice among candidates
@Resource(name = "mysqlRepo")           // JSR-250, by name
@Value("${app.title}")                  // property injection
@Value("#{2 * T(Math).PI}")             // SpEL
```
```java
@Service
public class NotificationService {
    private final MessageSender sender;
    public NotificationService(@Qualifier("smsSender") MessageSender sender) { ... }
}

// Injecting ALL implementations — powerful with the Strategy pattern
@Service
public class PaymentService {
    private final Map<String, PaymentStrategy> strategies;   // beanName → bean
    private final List<Validator> validators;                // all Validator beans
}
```
**Resolution order:** by type → if multiple, `@Primary` → then `@Qualifier` → then the field/parameter name. If nothing matches: `NoSuchBeanDefinitionException`. If several match: `NoUniqueBeanDefinitionException`.

---

# PART 2 — Beans

## 2.1 Declaring beans — three ways

**1. XML (Exercise 1 & 5 — classic)**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
           https://www.springframework.org/schema/beans/spring-beans.xsd
           http://www.springframework.org/schema/context
           https://www.springframework.org/schema/context/spring-context.xsd">

    <context:component-scan base-package="com.cognizant"/>
    <context:annotation-config/>

    <bean id="employeeRepository" class="com.cognizant.repo.EmployeeRepositoryImpl"/>

    <bean id="employeeService" class="com.cognizant.service.EmployeeService">
        <constructor-arg ref="employeeRepository"/>          <!-- constructor injection -->
        <property name="taxRate" value="0.1"/>               <!-- setter injection -->
    </bean>

    <!-- Exercise 2: a SimpleDateFormat bean configured from XML -->
    <bean id="dateFormat" class="java.text.SimpleDateFormat">
        <constructor-arg value="dd/MM/yyyy"/>
    </bean>

    <!-- Exercise 4/6: a Country bean with property injection -->
    <bean id="country" class="com.cognizant.model.Country">
        <property name="code" value="IN"/>
        <property name="name" value="India"/>
    </bean>

    <!-- Exercise 6: a list of countries -->
    <bean id="countryList" class="java.util.ArrayList">
        <constructor-arg>
            <list>
                <ref bean="country"/>
                <bean class="com.cognizant.model.Country">
                    <property name="code" value="US"/><property name="name" value="USA"/>
                </bean>
            </list>
        </constructor-arg>
    </bean>
</beans>
```

**2. Annotations + component scanning (most common)**
```java
@Component      // generic Spring-managed component
@Service        // business logic layer (semantic @Component)
@Repository     // data access layer — also translates persistence exceptions ⭐
@Controller     // MVC controller (returns view names)
@RestController // @Controller + @ResponseBody
@Configuration  // a source of @Bean definitions
```
All four stereotypes are **`@Component` under the hood** — the difference is semantic clarity, plus `@Repository`'s automatic translation of vendor exceptions into Spring's `DataAccessException` hierarchy.

**3. Java configuration (`@Bean`) — for third-party classes you can't annotate**
```java
@Configuration
@ComponentScan(basePackages = "com.cognizant")
@PropertySource("classpath:app.properties")
public class AppConfig {

    @Bean
    public SimpleDateFormat dateFormat() { return new SimpleDateFormat("dd/MM/yyyy"); }

    @Bean(name = "primaryDs", initMethod = "init", destroyMethod = "close")
    public DataSource dataSource(@Value("${db.url}") String url) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        return ds;
    }

    @Bean
    public EmployeeService employeeService(EmployeeRepository repo) {
        return new EmployeeService(repo);       // deps resolved as method parameters
    }
}
```

⭐ **`@Component` vs `@Bean`**
| `@Component` | `@Bean` |
|---|---|
| On a **class** | On a **method** inside `@Configuration` |
| Found by component scanning | Explicitly declared |
| You own the class | Works for **third-party** classes |
| Less control | Full programmatic control over construction |

## 2.2 Bean scopes ⭐ (Exercise 5 — singleton vs prototype)
| Scope | Instances | Context |
|---|---|---|
| **`singleton`** (default) | **one per container** | any |
| **`prototype`** | a new one on every request/injection | any |
| `request` | one per HTTP request | web |
| `session` | one per HTTP session | web |
| `application` | one per `ServletContext` | web |
| `websocket` | one per WebSocket session | web |

```java
@Component
@Scope("prototype")                      // or ConfigurableBeanFactory.SCOPE_PROTOTYPE
public class Task { }

@Component
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ProxiedTask { }
```
```java
Task a = ctx.getBean(Task.class);
Task b = ctx.getBean(Task.class);
System.out.println(a == b);        // singleton → true ; prototype → false
```
⚠️ **The classic trap:** injecting a **prototype** bean into a **singleton** gives you *one* instance forever — the singleton is only created once. Fixes: `ObjectProvider<Task>`, `@Lookup` method injection, or a scoped proxy.
⚠️ Spring **does not** call destruction callbacks for prototype beans — the client owns cleanup.
⭐ **Are singleton beans thread-safe?** **No.** Spring guarantees one *instance*, not thread safety. Keep beans **stateless**; any mutable instance field is a bug.

## 2.3 Bean lifecycle ⭐
```
1. Instantiate (constructor)
2. Populate properties (DI)
3. BeanNameAware.setBeanName() / BeanFactoryAware / ApplicationContextAware
4. BeanPostProcessor.postProcessBeforeInitialization()
5. @PostConstruct
6. InitializingBean.afterPropertiesSet()
7. custom init-method
8. BeanPostProcessor.postProcessAfterInitialization()   ← AOP proxies are created here
   ───────── BEAN IS READY ─────────
9. @PreDestroy
10. DisposableBean.destroy()
11. custom destroy-method
```
```java
@Component
public class LifecycleDemo {
    public LifecycleDemo(){ System.out.println("1. constructor"); }
    @PostConstruct public void init(){ System.out.println("5. @PostConstruct"); }
    @PreDestroy   public void cleanup(){ System.out.println("9. @PreDestroy"); }
}
```
Prefer the **annotations** (`@PostConstruct`/`@PreDestroy`, JSR-250) over the Spring interfaces — no framework coupling.
> In Spring Boot 3 / Jakarta EE these live in `jakarta.annotation.*` (was `javax.annotation.*`).

## 2.4 Profiles & properties
```java
@Profile("dev")  @Component class DevDataSourceConfig { }
@Profile("!prod") @Component class DebugTools { }
```
```properties
spring.profiles.active=dev
```
```java
@Value("${app.name:DefaultApp}")   private String appName;   // with a default

@ConfigurationProperties(prefix = "app")   // ✅ type-safe, preferred for grouped config
@Component
public class AppProperties {
    private String name; private int timeout; private List<String> servers;
    // getters/setters
}
```

---

# PART 3 — Spring AOP ⭐ (Exercises 3 & 8)

## 3.1 The problem it solves
**Cross-cutting concerns** — logging, security, transactions, caching, metrics, validation — are needed in many classes but belong to none of them. Without AOP the code is duplicated everywhere and tangles with business logic.

## 3.2 Terminology ⭐
| Term | Meaning |
|---|---|
| **Aspect** | A module encapsulating a cross-cutting concern (`@Aspect` class) |
| **Join point** | A point in execution where an aspect can be applied — in Spring AOP, **always a method execution** |
| **Pointcut** | An expression selecting which join points to advise |
| **Advice** | The action taken at a join point (the code that runs) |
| **Target object** | The bean being advised |
| **Proxy** | The object Spring creates to wrap the target |
| **Weaving** | Linking aspects to targets — Spring does it at **runtime** via proxies (AspectJ can do compile/load-time) |

## 3.3 Advice types ⭐
| Advice | Runs |
|---|---|
| `@Before` | before the method |
| `@AfterReturning` | after normal return (can access the result) |
| `@AfterThrowing` | only if an exception is thrown |
| `@After` | always (like `finally`) |
| `@Around` | **wraps** the call — most powerful; controls whether/when the target runs |

```java
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    // reusable named pointcut
    @Pointcut("execution(* com.cognizant.service.*.*(..))")
    public void serviceLayer() { }

    @Before("serviceLayer()")
    public void logBefore(JoinPoint jp) {
        log.info("→ {} args={}", jp.getSignature().toShortString(), Arrays.toString(jp.getArgs()));
    }

    @AfterReturning(pointcut = "serviceLayer()", returning = "result")
    public void logAfterReturning(JoinPoint jp, Object result) {
        log.info("← {} returned {}", jp.getSignature().getName(), result);
    }

    @AfterThrowing(pointcut = "serviceLayer()", throwing = "ex")
    public void logAfterThrowing(JoinPoint jp, Throwable ex) {
        log.error("✗ {} threw {}", jp.getSignature().getName(), ex.getMessage());
    }

    @After("serviceLayer()")
    public void logAfter(JoinPoint jp) { log.debug("finally: {}", jp.getSignature().getName()); }

    @Around("@annotation(com.cognizant.annotation.TrackTime)")
    public Object measure(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();          // ⭐ MUST call proceed() to run the target
            return result;
        } finally {
            log.info("{} took {} ms", pjp.getSignature().getName(),
                     System.currentTimeMillis() - start);
        }
    }
}
```
Enable with `@EnableAspectJAutoProxy` (automatic in Spring Boot when `spring-boot-starter-aop` is on the classpath).

## 3.4 Pointcut expressions
```java
execution(* com.cognizant.service.*.*(..))           // any method in the service package
execution(public * *(..))                            // any public method
execution(* save*(..))                               // methods starting with "save"
execution(* com.cognizant..*.*(..))                  // package and subpackages
execution(String com.x.Y.get*(int, ..))              // specific return/args

within(com.cognizant.service..*)                     // any join point in these types
@annotation(org.springframework.transaction.annotation.Transactional)   // annotated methods
@within(org.springframework.stereotype.Service)      // methods in @Service classes
bean(*Service)                                       // by bean-name pattern
args(String, ..)                                     // by runtime argument types
this(com.x.Foo) / target(com.x.Foo)                  // proxy type / target type

// combine
@Pointcut("execution(* com.cognizant.service.*.*(..)) && !@annotation(NoLog)")
```

## 3.5 How Spring AOP works — and its limits ⭐
Spring creates a **proxy** around the bean:
- **JDK dynamic proxy** — if the target implements an interface (the default)
- **CGLIB subclass proxy** — otherwise (Spring Boot forces CGLIB by default; hence classes and methods must not be `final`)

**Limitations (very common interview question)**
1. **Method execution join points only** — no field access or constructor advice (AspectJ can do those).
2. **Public methods only** by default (private/protected aren't proxied by JDK proxies).
3. **Self-invocation is not advised** ⭐ — calling `this.otherMethod()` inside the same bean bypasses the proxy, so `@Transactional`, `@Cacheable`, `@Async` and your aspects **do not fire**.
```java
@Service
public class OrderService {
    public void placeOrder() {
        this.saveOrder();          // ❌ @Transactional on saveOrder does NOT apply
    }
    @Transactional
    public void saveOrder() { }
}
// Fix: move saveOrder into another bean, or self-inject, or use AspectJ weaving
```
4. Only **Spring-managed beans** are advised.

**Spring AOP vs AspectJ**
| | Spring AOP | AspectJ |
|---|---|---|
| Weaving | runtime (proxies) | compile-time / load-time |
| Join points | method execution | methods, fields, constructors, static init |
| Performance | slight proxy overhead | faster (bytecode woven) |
| Setup | trivial | needs a weaver/compiler |
| Scope | Spring beans only | any object |

**Where AOP is used in Spring itself:** `@Transactional`, `@Cacheable`, `@Async`, `@Retryable`, `@PreAuthorize`, `@Validated`, Micrometer `@Timed`.

---

# PART 4 — Maven ⭐ (Exercise 4)

## 4.1 What Maven is
A **build automation and dependency management** tool built on **Convention over Configuration** and a declarative POM. It handles compile, test, package, dependency resolution, and reporting.

## 4.2 Standard directory layout
```
project/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/          # application source
    │   └── resources/     # application.properties, XML, static files
    └── test/
        ├── java/          # test source
        └── resources/     # test config
└── target/                # BUILD OUTPUT — always in .gitignore
    ├── classes/
    ├── test-classes/
    └── myapp-1.0.jar
```

## 4.3 The POM
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>

    <parent>                                        <!-- inherits dependency versions -->
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.4</version>
        <relativePath/>
    </parent>

    <!-- GAV coordinates uniquely identify an artifact -->
    <groupId>com.cognizant</groupId>              <!-- organisation, reverse domain -->
    <artifactId>employee-service</artifactId>     <!-- project name -->
    <version>1.0.0-SNAPSHOT</version>             <!-- SNAPSHOT = in development -->
    <packaging>jar</packaging>                    <!-- jar | war | pom -->

    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

## 4.4 Dependency scopes ⭐
| Scope | Compile | Test | Runtime | Packaged | Example |
|---|:---:|:---:|:---:|:---:|---|
| `compile` (default) | ✅ | ✅ | ✅ | ✅ | spring-core |
| `provided` | ✅ | ✅ | ❌ | ❌ | servlet-api, lombok |
| `runtime` | ❌ | ✅ | ✅ | ✅ | JDBC driver |
| `test` | ❌ | ✅ | ❌ | ❌ | junit, mockito |
| `system` | ✅ | ✅ | ❌ | ❌ | deprecated |
| `import` | — | — | — | — | BOM in `<dependencyManagement>` |

## 4.5 Build lifecycle ⭐
Three lifecycles: **clean**, **default**, **site**. The `default` lifecycle phases (running a phase runs **all preceding phases**):
```
validate → compile → test → package → verify → install → deploy
```
| Phase | Does |
|---|---|
| `validate` | check the project is correct |
| `compile` | compile `src/main/java` → `target/classes` |
| `test` | run unit tests (Surefire) — **fails the build on failure** |
| `package` | bundle into a JAR/WAR in `target/` |
| `verify` | run integration tests & checks (Failsafe, JaCoCo) |
| `install` | copy the artifact into the **local** repo `~/.m2/repository` |
| `deploy` | upload to a **remote** repository |

```bash
mvn clean install          # the most common command
mvn clean package -DskipTests
mvn test
mvn dependency:tree        # ⭐ diagnose version conflicts
mvn spring-boot:run
mvn versions:display-dependency-updates
mvn -U clean install       # force update SNAPSHOT dependencies
```
> `mvn clean install` = delete `target/`, compile, test, package, install locally. `install` vs `package`: `package` stops at `target/`, `install` also puts it in `~/.m2` so other local projects can depend on it.

## 4.6 Repositories & transitive dependencies
```
Local (~/.m2/repository) → Remote/Private (Nexus, Artifactory) → Central (repo.maven.apache.org)
```
Maven resolves **transitive dependencies** automatically. On version conflicts it applies **"nearest wins"** (shortest path in the dependency tree; ties broken by declaration order).
```xml
<dependency>
  <groupId>x</groupId><artifactId>y</artifactId>
  <exclusions>
    <exclusion><groupId>org.slf4j</groupId><artifactId>slf4j-log4j12</artifactId></exclusion>
  </exclusions>
</dependency>
```
`<dependencyManagement>` centralises versions **without** adding the dependency; child modules/inheritors omit `<version>`.

## 4.7 Maven vs Gradle
| | Maven | Gradle |
|---|---|---|
| Config | XML (declarative) | Groovy/Kotlin DSL (programmable) |
| Speed | slower | faster (incremental, build cache, daemon) |
| Learning curve | easier, rigid conventions | more flexible, steeper |
| Ecosystem | huge, enterprise standard | Android standard, growing |
> Cognizant projects overwhelmingly use **Maven**.

---

## ⭐ Interview Gold — Spring Core / AOP / Maven Rapid Fire

| Q | A |
|---|---|
| What is IoC? | The container, not your code, controls object creation and wiring. |
| IoC vs DI? | IoC is the principle; DI is how it's implemented (constructor/setter/field). |
| BeanFactory vs ApplicationContext? | Lazy basic DI vs eager singletons + events, i18n, AOP, resource loading. |
| Types of DI, and which is best? | Constructor (best — required, final, testable), setter (optional deps), field (discouraged). |
| Why avoid field injection? | Hidden dependencies, no `final`, needs reflection/Spring to test, easy to violate SRP unnoticed. |
| What is a Spring bean? | An object instantiated, configured and managed by the Spring IoC container. |
| Bean scopes? | singleton (default), prototype, request, session, application, websocket. |
| Are singleton beans thread-safe? | No — one instance shared by all threads; keep them stateless. |
| Prototype bean in a singleton — problem? | Only injected once. Use `ObjectProvider`, `@Lookup`, or a scoped proxy. |
| Bean lifecycle? | Instantiate → populate → Aware → BPP-before → `@PostConstruct` → `afterPropertiesSet` → init-method → BPP-after → **ready** → `@PreDestroy` → `destroy` → destroy-method. |
| `@Component` vs `@Bean`? | Class-level + scanned vs method-level + explicit; `@Bean` works for third-party classes. |
| `@Component` vs `@Service` vs `@Repository` vs `@Controller`? | All are `@Component`; semantic layering, and `@Repository` adds exception translation. |
| `@Autowired` resolution? | By type → `@Primary` → `@Qualifier` → parameter name. |
| `@Qualifier` vs `@Primary`? | Explicit per-injection-point choice vs a global default candidate. |
| Circular dependency? | A↔B; constructor injection fails fast; refactor, or `@Lazy` as a last resort. |
| What is AOP? | Modularising cross-cutting concerns via aspects applied at join points. |
| AOP terminology? | Aspect, Join point, Pointcut, Advice, Target, Proxy, Weaving. |
| Advice types? | `@Before`, `@After`, `@AfterReturning`, `@AfterThrowing`, `@Around`. |
| `@Around` requirement? | You must call `pjp.proceed()` or the target never executes. |
| How does Spring AOP work? | Runtime proxies — JDK dynamic proxy (interface) or CGLIB (subclass). |
| Spring AOP limitations? | Method execution only, public methods, Spring beans only, **self-invocation bypasses the proxy**. |
| Spring AOP vs AspectJ? | Runtime proxies vs compile/load-time weaving; fewer vs more join point types. |
| Real uses of AOP in Spring? | `@Transactional`, `@Cacheable`, `@Async`, `@PreAuthorize`, logging, metrics. |
| What is Maven? | Build + dependency management tool driven by a declarative POM with strong conventions. |
| Maven lifecycle phases? | validate, compile, test, package, verify, install, deploy. |
| `package` vs `install` vs `deploy`? | `target/` only vs also `~/.m2` vs also a remote repository. |
| Dependency scopes? | compile, provided, runtime, test, system, import. |
| What is a transitive dependency & conflict resolution? | Dependencies of dependencies; Maven picks the **nearest** version in the tree. |
| How do you debug a dependency conflict? | `mvn dependency:tree`, then `<exclusions>` or pin the version in `<dependencyManagement>`. |
| What is a SNAPSHOT? | An in-development version re-fetched from the remote repo on each build. |

---

## 📚 References
- Spring Framework — Core (IoC container): https://docs.spring.io/spring-framework/reference/core/beans.html
- Spring Framework — AOP: https://docs.spring.io/spring-framework/reference/core/aop.html
- Spring Framework — Annotation config: https://docs.spring.io/spring-framework/reference/core/beans/annotation-config.html
- Maven — Introduction to the Build Lifecycle: https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html
- Maven — Dependency Mechanism: https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html
- Maven Central Repository: https://central.sonatype.com/
- Baeldung — Spring Bean Scopes: https://www.baeldung.com/spring-bean-scopes
- Baeldung — Intro to Spring AOP: https://www.baeldung.com/spring-aop
- Martin Fowler — Inversion of Control Containers and the DI Pattern: https://martinfowler.com/articles/injection.html
