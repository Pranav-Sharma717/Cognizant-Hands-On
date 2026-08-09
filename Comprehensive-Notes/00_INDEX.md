# 📘 Cognizant Digital Nurture 5.0 — Java Full Stack Engineer (React)
## Master Notes Index

> **Scope:** Every topic present in this repository, plus **Java from absolute basics to interview-ready** and **MySQL from basics**, written for the Cognizant DN 5.0 **Java FSE** placement drive.
>
> **Author of repo:** G V Pranav Sharma · **Notes generated:** August 2026

---

## 🎯 How to use these notes

| Goal | Read this |
|---|---|
| "I have 4 weeks before the drive" | 01 → 03 → 05 → 09 → 11 → 15 (in order) |
| "I have 1 week" | 15 (Rapid-fire) + 04 (Collections) + 09 (Boot/REST) + 06 (SOLID/Patterns) |
| "I have 1 day" | 15 only, plus the ⭐ **Interview Gold** boxes in every file |
| "I need to code in the assessment" | 05 (DSA) + 04 (Collections) + 08 (JPA queries) + 03 (MySQL) |

Every file follows the same layout:
1. **Concept** — plain-English explanation
2. **Syntax / Code** — runnable Java, SQL or JSX
3. **Gotchas** — what actually breaks in production
4. ⭐ **Interview Gold** — the question, the model answer, the follow-up
5. **References** — official docs + trusted sources

---

## 📂 File Map

### Part A — Language Foundations (learn first)
| # | File | Covers |
|---|---|---|
| 01 | [Java Core Basics](01_Java_Core_Basics.md) | JDK/JRE/JVM, syntax, data types, operators, control flow, arrays, strings, I/O, `main` |
| 02 | [Java OOP](02_Java_OOP.md) | Classes, objects, 4 pillars, inheritance, polymorphism, abstraction, interfaces, `Object` methods, immutability, records |
| 03 | [MySQL](03_MySQL.md) | RDBMS basics → DDL/DML → joins → subqueries → indexes → transactions → normalization → tuning → JDBC |
| 04 | [Java Collections & Generics](04_Java_Collections_Generics.md) | List/Set/Map/Queue internals, HashMap deep dive, generics, comparators, `equals`/`hashCode` |

### Part B — Engineering Concepts (Week 1 of the program)
| # | File | Covers |
|---|---|---|
| 05 | [DSA in Java](05_DSA_Java.md) | Big-O, arrays, searching, sorting, recursion, linked list, stack, queue, tree, graph, DP + the 7 repo exercises |
| 06 | [Design Patterns & SOLID](06_Design_Patterns_SOLID.md) | All 12 patterns implemented in `design-patterns/`, SOLID, GRASP, anti-patterns |
| 07 | [PL/SQL](07_PLSQL.md) | Blocks, control structures, exceptions, procedures, functions, triggers, cursors, packages + PL/SQL vs MySQL SP |

### Part C — Java Advanced (interview-critical)
| # | File | Covers |
|---|---|---|
| 08 | [Java Advanced](08_Java_Advanced.md) | Exceptions, Java 8 lambdas/streams/Optional, functional interfaces, multithreading, `ExecutorService`, `synchronized` vs `Lock`, JVM memory & GC, Java 8→25 features |

### Part D — Testing & Quality
| # | File | Covers |
|---|---|---|
| 09 | [TDD, JUnit 5, Mockito, SLF4J](09_TDD_JUnit_Mockito_Logging.md) | Red-Green-Refactor, AAA, all JUnit 5 annotations, Mockito stubbing/verifying, `@WebMvcTest`/`@DataJpaTest`, SLF4J + Logback |

### Part E — Spring Ecosystem
| # | File | Covers |
|---|---|---|
| 10 | [Spring Core, Maven & AOP](10_Spring_Core_Maven_AOP.md) | IoC/DI, bean lifecycle, scopes, XML vs annotation vs Java config, AOP advice types, Maven lifecycle & POM |
| 11 | [Spring Data JPA & Hibernate](11_Spring_Data_JPA_Hibernate.md) | Entities, relationships, repositories, derived queries, JPQL/native, pagination, auditing, projections, N+1, caching, lazy loading |
| 12 | [Spring Boot, REST & Security](12_Spring_Boot_REST_Security.md) | Auto-configuration, starters, layered architecture, HTTP/REST, status codes, validation, exception handling, Basic Auth, JWT |

### Part F — Distributed Systems
| # | File | Covers |
|---|---|---|
| 13 | [Microservices](13_Microservices.md) | Monolith vs microservices, Eureka, OpenFeign, API Gateway, Resilience4j, config server, OAuth2/OIDC, SSO, saga, observability |

### Part G — Frontend & Delivery
| # | File | Covers |
|---|---|---|
| 14 | [React JS](14_React.md) | SPA vs MPA, JSX, Virtual DOM, components, props/state, hooks, lifecycle, routing, forms, context, Axios, RTL testing, ES6 |
| 15 | [Git, GitHub & DevOps](15_Git_DevOps.md) | Git internals, all commands from the repo, branching strategies, merge vs rebase, conflicts, CI/CD, Docker, cloud basics |

### Part H — The Drive Itself
| # | File | Covers |
|---|---|---|
| 16 | [Interview Rapid-Fire & Drive Guide](16_Interview_RapidFire.md) | 300+ Q&A, the Cognizant DN process, coding-round patterns, HR round, project pitch script |

---

## 🗺️ The Cognizant Java FSE Tech Stack (what you must be able to talk about)

```
┌──────────────────────────────────────────────────────────────┐
│  BROWSER          React 18 SPA (hooks, router, axios)        │
└───────────────────────────┬──────────────────────────────────┘
                            │ HTTPS / JSON / JWT
┌───────────────────────────▼──────────────────────────────────┐
│  EDGE             Spring Cloud Gateway  (routing, rate-limit)│
└───────────────────────────┬──────────────────────────────────┘
                            │
┌───────────────────────────▼──────────────────────────────────┐
│  SERVICES         Spring Boot 3.x microservices              │
│    @RestController → @Service → @Repository                  │
│    Eureka (discovery) · OpenFeign (calls) · Resilience4j     │
│    Spring Security (JWT / OAuth2) · Validation · AOP         │
└───────────────────────────┬──────────────────────────────────┘
                            │ Spring Data JPA / Hibernate
┌───────────────────────────▼──────────────────────────────────┐
│  DATA             MySQL 8 / Oracle (PL/SQL) · Redis cache    │
└──────────────────────────────────────────────────────────────┘
   BUILD: Maven   TEST: JUnit 5 + Mockito   LOG: SLF4J+Logback
   SCM: Git/GitHub   CI: Jenkins/GitHub Actions   RUN: Docker
```

---

## 📌 Version reality check (August 2026)

| Tech | Version used in this program | Current landscape |
|---|---|---|
| Java | 17 / 21 (LTS) | **Java 25 is the newest LTS** (GA Sept 2025, premier support to Sept 2030). Next LTS = 27 (Sept 2027). LTS cadence is now 2 years. |
| Spring Boot | **3.x** (Spring Framework 6, needs Java 17+, `jakarta.*` namespace) | Spring Boot 4.x on Spring Framework 7 is now GA |
| MySQL | 8.x | 8.4 LTS / 9.x innovation |
| React | 18 | 19 available |
| JUnit | 5 (Jupiter) | 5.x |

> **Interview line:** *"The program was built on Spring Boot 3 with Java 17. I know Boot 3 moved `javax.*` → `jakarta.*` and requires Java 17 as the baseline, and that Java 25 is now the current LTS."* — this single sentence signals you track the ecosystem.

---

## ✅ Self-assessment checklist

Tick these off. If you can't explain it out loud in 60 seconds, revisit the file.

**Java**
- [ ] JDK vs JRE vs JVM; what `javac` and `java` actually do
- [ ] `==` vs `.equals()`; the `equals`/`hashCode` contract
- [ ] String immutability, String pool, `String` vs `StringBuilder` vs `StringBuffer`
- [ ] Overloading vs overriding; static vs dynamic binding
- [ ] `ArrayList` vs `LinkedList`; `HashMap` internals (buckets → treeify at 8)
- [ ] Checked vs unchecked exceptions; try-with-resources
- [ ] Lambda, functional interface, Stream pipeline, `map` vs `flatMap`
- [ ] `Thread` lifecycle, `synchronized`, `volatile`, `ExecutorService`
- [ ] Heap vs Stack; GC generations

**SQL / MySQL**
- [ ] All 5 join types with a drawn Venn diagram
- [ ] `WHERE` vs `HAVING`; SQL logical execution order
- [ ] Index: B-Tree, clustered vs secondary, when an index is *not* used
- [ ] ACID + 4 isolation levels + which anomaly each prevents
- [ ] 1NF/2NF/3NF with a worked example
- [ ] `EXPLAIN` output — read it

**Spring**
- [ ] IoC vs DI; the 3 injection types and which is recommended
- [ ] `@Component` vs `@Bean`; `@Autowired` resolution
- [ ] Bean scopes and lifecycle callbacks
- [ ] What `@SpringBootApplication` expands to
- [ ] `@Transactional` — propagation, rollback rules, why it fails on self-invocation
- [ ] Lazy vs eager fetch, the N+1 problem and 3 fixes
- [ ] `@ControllerAdvice` global exception handling
- [ ] JWT flow end to end

**Microservices / DevOps / React**
- [ ] Service discovery, why client-side load balancing
- [ ] Circuit breaker states (CLOSED → OPEN → HALF_OPEN)
- [ ] Virtual DOM & reconciliation; why keys matter
- [ ] `useState` vs `useEffect` vs `useContext`
- [ ] merge vs rebase; how you resolve a conflict

---

## 📚 Primary references used throughout

**Official documentation (always prefer these)**
- Java SE Docs & Tutorials — https://docs.oracle.com/en/java/javase/
- Java Language Specification — https://docs.oracle.com/javase/specs/
- Java API Javadoc — https://docs.oracle.com/en/java/javase/21/docs/api/
- MySQL 8.0 Reference Manual — https://dev.mysql.com/doc/refman/8.0/en/
- Spring Framework Reference — https://docs.spring.io/spring-framework/reference/
- Spring Boot Reference — https://docs.spring.io/spring-boot/index.html
- Spring Data JPA — https://docs.spring.io/spring-data/jpa/reference/
- Spring Security — https://docs.spring.io/spring-security/reference/
- Spring Cloud — https://spring.io/projects/spring-cloud
- Hibernate ORM User Guide — https://docs.jboss.org/hibernate/orm/6.4/userguide/html_single/Hibernate_User_Guide.html
- JUnit 5 User Guide — https://junit.org/junit5/docs/current/user-guide/
- Mockito Javadoc — https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html
- SLF4J Manual — https://www.slf4j.org/manual.html
- Logback Manual — https://logback.qos.ch/manual/
- Maven Docs — https://maven.apache.org/guides/
- React Docs — https://react.dev/
- React Router — https://reactrouter.com/
- Testing Library — https://testing-library.com/docs/react-testing-library/intro/
- Git Book (Pro Git) — https://git-scm.com/book/en/v2
- Resilience4j — https://resilience4j.readme.io/docs
- Oracle PL/SQL Language Reference — https://docs.oracle.com/en/database/oracle/oracle-database/21/lnpls/
- Docker Docs — https://docs.docker.com/
- OpenAPI / Swagger — https://swagger.io/docs/
- JSON Web Tokens — https://jwt.io/introduction

**Trusted secondary sources**
- Baeldung — https://www.baeldung.com/ (best Spring/Java tutorials)
- Refactoring Guru (Design Patterns) — https://refactoring.guru/design-patterns
- GeeksforGeeks — https://www.geeksforgeeks.org/
- Java Design Patterns (iluwatar) — https://java-design-patterns.com/
- Martin Fowler — https://martinfowler.com/ (microservices, refactoring, DI)
- Use The Index, Luke (SQL indexing) — https://use-the-index-luke.com/
- microservices.io (Chris Richardson patterns) — https://microservices.io/patterns/
- Spring Guides — https://spring.io/guides
- MDN Web Docs (JS/HTTP) — https://developer.mozilla.org/
- Vlad Mihalcea (Hibernate performance) — https://vladmihalcea.com/
- Awesome Java Interview — https://github.com/topics/java-interview-questions
