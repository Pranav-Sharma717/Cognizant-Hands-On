# 11 — Spring Data JPA & Hibernate

> Maps to [`Week 2/week2_part2.md`](../Week%202/week2_part2.md) — 10 exercises + HQL/native query hands-on.
> **References:** [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/reference/), [Hibernate User Guide](https://docs.jboss.org/hibernate/orm/6.4/userguide/html_single/Hibernate_User_Guide.html)

---

## 1. The stack ⭐

```
Your code
   │
Spring Data JPA      ← repository abstraction, query derivation, pagination
   │
JPA (Jakarta Persistence API)   ← the SPECIFICATION (interfaces, annotations)
   │
Hibernate            ← the default IMPLEMENTATION (ORM provider)
   │
JDBC                 ← low-level DB API
   │
MySQL / Oracle
```

| Layer | What it is |
|---|---|
| **JPA** | A *specification* — `EntityManager`, `@Entity`, JPQL. No implementation. |
| **Hibernate** | The most popular JPA *provider*; also has its own extra API (`Session`, HQL, Criteria). |
| **Spring Data JPA** | A layer on top that generates repository implementations at runtime from interfaces. |

**ORM (Object-Relational Mapping)** maps Java objects ↔ relational tables. It removes boilerplate JDBC, gives caching, lazy loading, dirty checking and DB portability — at the cost of a leaky abstraction you must understand (N+1, lazy-loading exceptions, generated SQL).

> Spring Boot 3 uses **`jakarta.persistence.*`** (not `javax.persistence.*`) — a very common migration question.

**Dependencies**
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
  <groupId>com.mysql</groupId><artifactId>mysql-connector-j</artifactId><scope>runtime</scope>
</dependency>
```

---

## 2. Configuration (Exercises 1, 9, 10)

```properties
# ---- DataSource ----
spring.datasource.url=jdbc:mysql://localhost:3306/cognizant_fse?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# ---- HikariCP connection pool (Spring Boot default) ----
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000

# ---- JPA / Hibernate ----
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.default_batch_fetch_size=20

# ---- Hibernate 2nd-level cache (Exercise 10) ----
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.use_query_cache=true
spring.jpa.properties.hibernate.cache.region.factory_class=org.hibernate.cache.jcache.JCacheRegionFactory
```

### `ddl-auto` values ⭐
| Value | Effect |
|---|---|
| `none` | do nothing — **production default** |
| `validate` | verify the schema matches the entities, fail if not — **safe production choice** |
| `update` | add missing tables/columns (never drops) — **dev only** |
| `create` | drop + create at startup |
| `create-drop` | create at start, drop at shutdown — tests |
> ⚠️ **Never `update`/`create` in production.** Use **Flyway** or **Liquibase** for versioned migrations.

⭐ **`spring.jpa.open-in-view`** defaults to `true` and keeps the `EntityManager` open through view rendering — it hides `LazyInitializationException` but holds a DB connection for the whole request. **Set it to `false`** and fetch what you need in the service layer. Spring Boot logs a warning about this.

---

## 3. Entities (Exercise 2) ⭐

```java
@Entity
@Table(name = "employees",
       indexes = @Index(name = "idx_emp_email", columnList = "email"),
       uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(precision = 10, scale = 2)
    private BigDecimal salary;

    @Enumerated(EnumType.STRING)              // ⭐ never ORDINAL
    private Status status;

    @Temporal(TemporalType.DATE)              // not needed for java.time types
    private LocalDate hireDate;

    @Lob
    private String biography;

    @Transient                                 // NOT persisted
    private int computedAge;

    @Version                                   // optimistic locking
    private Long version;

    @Embedded
    private Address address;

    // relationships below…
    protected Employee() { }                  // ⭐ JPA requires a no-arg constructor
}
```

**Entity requirements:** `@Entity`, an `@Id`, a public/protected **no-arg constructor**, a non-final class, non-final persistent fields.

### ID generation strategies ⭐
| Strategy | How | Notes |
|---|---|---|
| `IDENTITY` | DB auto-increment | **MySQL default choice**; disables JDBC batch inserts |
| `SEQUENCE` | DB sequence | best for Oracle/PostgreSQL; allows batching + allocation size |
| `TABLE` | a key table | portable but slow — avoid |
| `AUTO` | provider picks | Hibernate 6 picks SEQUENCE where available |
| `UUID` / `@UuidGenerator` | random UUID | distributed systems |

### Relationships ⭐⭐
```java
// ONE-TO-ONE
@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
@JoinColumn(name = "profile_id")          // this side owns the FK
private Profile profile;

// ONE-TO-MANY (inverse side) — mappedBy points to the FIELD in the owning entity
@OneToMany(mappedBy = "department", cascade = CascadeType.ALL,
           orphanRemoval = true, fetch = FetchType.LAZY)
private List<Employee> employees = new ArrayList<>();

// MANY-TO-ONE (owning side — holds the FK). Default fetch is EAGER → override it!
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "dept_id", nullable = false)
private Department department;

// MANY-TO-MANY
@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(name = "employee_project",
    joinColumns        = @JoinColumn(name = "employee_id"),
    inverseJoinColumns = @JoinColumn(name = "project_id"))
private Set<Project> projects = new HashSet<>();
```

**Default fetch types (memorise this) ⭐**
| Relationship | Default |
|---|---|
| `@ManyToOne` | **EAGER** |
| `@OneToOne` | **EAGER** |
| `@OneToMany` | LAZY |
| `@ManyToMany` | LAZY |
> Best practice: make **everything LAZY** and fetch explicitly with a join fetch or entity graph when you need it.

**Cascade types:** `PERSIST`, `MERGE`, `REMOVE`, `REFRESH`, `DETACH`, `ALL`.
`orphanRemoval = true` deletes a child when it's removed from the parent's collection (stronger than `CascadeType.REMOVE`).

**Bidirectional helper (always keep both sides in sync)**
```java
public void addEmployee(Employee e) {
    employees.add(e);
    e.setDepartment(this);
}
```
⚠️ Bidirectional relationships break `toString()`, `equals`/`hashCode` and JSON serialization with infinite recursion. Fix with `@JsonManagedReference`/`@JsonBackReference`, `@JsonIgnore`, or — better — **return DTOs, not entities**.

**Inheritance mapping**
```java
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)   // one table + @DiscriminatorColumn (fast, nullable cols)
@Inheritance(strategy = InheritanceType.JOINED)         // normalised, needs joins
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)// one table per concrete class
@MappedSuperclass                                        // shared fields, not an entity itself
```

---

## 4. Repositories (Exercise 3) ⭐

```
Repository (marker)
  └─ CrudRepository<T, ID>            save, findById, findAll, delete, count, existsById
      └─ ListCrudRepository<T, ID>    same but returns List
      └─ PagingAndSortingRepository   findAll(Pageable), findAll(Sort)
          └─ JpaRepository<T, ID>     + flush, saveAndFlush, deleteInBatch, getReferenceById
```

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // Spring generates the implementation at runtime — no code needed
}
```

### Derived query methods ⭐⭐ (Exercise 5)
Spring parses the **method name** into a query.
```java
List<Employee> findByLastName(String lastName);
List<Employee> findByFirstNameAndLastName(String f, String l);
List<Employee> findByFirstNameOrEmail(String f, String e);
List<Employee> findBySalaryGreaterThan(BigDecimal s);
List<Employee> findBySalaryBetween(BigDecimal lo, BigDecimal hi);
List<Employee> findByHireDateAfter(LocalDate d);
List<Employee> findByFirstNameLike(String pattern);
List<Employee> findByFirstNameContainingIgnoreCase(String q);
List<Employee> findByFirstNameStartingWith(String p);
List<Employee> findByDepartmentIsNull();
List<Employee> findByStatusIn(List<Status> statuses);
List<Employee> findByDepartment_Name(String name);          // nested property traversal
List<Employee> findTop5ByOrderBySalaryDesc();
List<Employee> findDistinctByStatus(Status s);
Optional<Employee> findByEmail(String email);
long countByDepartmentId(Long id);
boolean existsByEmail(String email);
void deleteByStatus(Status s);                              // needs @Transactional
Page<Employee> findByStatus(Status s, Pageable pageable);
```
**Grammar:** `find|read|get|query|count|exists|delete` + `Distinct` + `Top<N>`/`First<N>` + `By` + criteria + `OrderBy<Prop><Asc|Desc>`.
Keywords: `Is`, `Equals`, `Between`, `LessThan(Equal)`, `GreaterThan(Equal)`, `After`, `Before`, `IsNull`, `NotNull`, `Like`, `NotLike`, `StartingWith`, `EndingWith`, `Containing`, `In`, `NotIn`, `True`, `False`, `IgnoreCase`.

### `@Query` — JPQL and native SQL (Exercise 5 + HQL hands-on)
```java
// JPQL — operates on ENTITIES and FIELDS, not tables and columns
@Query("SELECT e FROM Employee e WHERE e.salary > :minSalary ORDER BY e.salary DESC")
List<Employee> findHighEarners(@Param("minSalary") BigDecimal minSalary);

@Query("SELECT e FROM Employee e JOIN FETCH e.department WHERE e.status = :status")
List<Employee> findWithDepartment(@Param("status") Status status);   // ⭐ solves N+1

@Query("SELECT new com.cognizant.dto.EmpDto(e.id, e.firstName, d.name) " +
       "FROM Employee e JOIN e.department d")
List<EmpDto> findAsDto();                                            // constructor expression

@Query("SELECT e.department.name, AVG(e.salary) FROM Employee e GROUP BY e.department.name")
List<Object[]> avgSalaryByDept();

// NATIVE SQL — full DB power, but DB-specific
@Query(value = "SELECT * FROM employees WHERE YEAR(hire_date) = :year", nativeQuery = true)
List<Employee> findHiredInYear(@Param("year") int year);

// Modifying query
@Modifying(clearAutomatically = true, flushAutomatically = true)
@Transactional
@Query("UPDATE Employee e SET e.salary = e.salary * :factor WHERE e.department.id = :deptId")
int applyRaise(@Param("factor") BigDecimal factor, @Param("deptId") Long deptId);
```
⚠️ `@Modifying` queries **bypass the persistence context** — stale entities remain cached; use `clearAutomatically = true`.

| | JPQL / HQL | Native SQL | Criteria API |
|---|---|---|---|
| Operates on | entities & fields | tables & columns | entities, type-safe |
| Portable | ✅ | ❌ | ✅ |
| DB-specific features | ❌ | ✅ | ❌ |
| Dynamic queries | awkward | awkward | ✅ best |
| Compile-time checked | ❌ | ❌ | ✅ (with metamodel) |

**Criteria API / Specifications** — for dynamic filters (search screens):
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long>,
                                            JpaSpecificationExecutor<Employee> { }

Specification<Employee> hasStatus(Status s) =
    (root, q, cb) -> cb.equal(root.get("status"), s);
Specification<Employee> salaryAbove(BigDecimal v) =
    (root, q, cb) -> cb.greaterThan(root.get("salary"), v);

repo.findAll(hasStatus(ACTIVE).and(salaryAbove(new BigDecimal("50000"))));
```

---

## 5. CRUD & the persistence context (Exercise 4)

```java
@Service
public class EmployeeService {
    private final EmployeeRepository repo;

    @Transactional
    public Employee create(Employee e) { return repo.save(e); }        // INSERT (id null)

    @Transactional(readOnly = true)
    public Employee get(Long id) {
        return repo.findById(id)
                   .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
    }

    @Transactional
    public Employee update(Long id, EmployeeDto dto) {
        Employee e = get(id);
        e.setSalary(dto.salary());        // ⭐ DIRTY CHECKING — no save() needed
        return e;                          // Hibernate flushes the UPDATE at commit
    }

    @Transactional
    public void delete(Long id) { repo.deleteById(id); }
}
```

### Entity lifecycle states ⭐⭐
```
   new Employee()
        │  TRANSIENT (no id, not managed)
        │
    persist()/save()
        ▼
    MANAGED  ◄── find() / query ──  (in the persistence context, dirty-checked)
        │  │
 remove()│  │ detach() / clear() / close() / txn ends
        ▼  ▼
    REMOVED   DETACHED  ──merge()──► MANAGED (a copy)
```
| State | Meaning |
|---|---|
| **Transient** | New object, no DB identity, not tracked |
| **Managed / Persistent** | Tracked by the `EntityManager`; changes auto-flush at commit |
| **Detached** | Has DB identity but is no longer tracked (e.g. after the txn ends) |
| **Removed** | Scheduled for DELETE at flush |

⭐ **`save()` vs `persist()` vs `merge()`**: `save()` (Spring Data) calls `persist()` if the id is null, else `merge()`. `persist()` makes a *transient* object managed and returns void. `merge()` copies a *detached* object's state into a managed instance and **returns the managed copy** — the original stays detached.

⭐ **Dirty checking:** inside a transaction, Hibernate snapshots managed entities and, at flush time, issues UPDATEs for whatever changed — you never call `save()` on a loaded entity.

⭐ **`getReferenceById()` vs `findById()`:** `getReferenceById` (was `getOne`) returns a **lazy proxy** without hitting the DB — great for setting a FK relationship; throws `EntityNotFoundException` on first access if the row doesn't exist. `findById` executes a SELECT immediately and returns `Optional`.

---

## 6. `@Transactional` ⭐⭐

```java
@Transactional(
    propagation = Propagation.REQUIRED,
    isolation   = Isolation.READ_COMMITTED,
    readOnly    = false,
    timeout     = 30,
    rollbackFor = Exception.class)
public void transfer(Long from, Long to, BigDecimal amt) { }
```

**Propagation**
| Value | Behaviour |
|---|---|
| **`REQUIRED`** (default) | join the existing transaction, or start one |
| `REQUIRES_NEW` | always start a new one, suspending any current |
| `SUPPORTS` | join if one exists, else run non-transactionally |
| `NOT_SUPPORTED` | suspend any current transaction |
| `MANDATORY` | must already be in a transaction, else throw |
| `NEVER` | throw if a transaction exists |
| `NESTED` | a savepoint within the current transaction |

**Isolation** — `DEFAULT` (use the DB's), `READ_UNCOMMITTED`, `READ_COMMITTED`, `REPEATABLE_READ`, `SERIALIZABLE`. See [03_MySQL.md §13](03_MySQL.md).

### The three classic gotchas ⭐
1. **Rollback only on unchecked exceptions by default.** A checked exception does **not** roll back unless you add `rollbackFor = Exception.class`.
2. **Self-invocation doesn't work.** Calling `this.txMethod()` bypasses the proxy → no transaction. Move it to another bean.
3. **Only public methods are proxied.** `@Transactional` on a private/protected method is silently ignored (with CGLIB proxies).

`@Transactional(readOnly = true)` — hints the driver/DB, skips dirty-check snapshots → a real performance win on query methods.

---

## 7. Pagination & Sorting (Exercise 6)

```java
Pageable pageable = PageRequest.of(0, 10, Sort.by("salary").descending());
Page<Employee> page = repo.findAll(pageable);

page.getContent();        // List<Employee>
page.getTotalElements();  // total rows (runs a COUNT query)
page.getTotalPages();
page.getNumber();         // current page (0-based)
page.hasNext();

Slice<Employee> slice = repo.findByStatus(ACTIVE, pageable);  // no COUNT query — cheaper

Sort sort = Sort.by(Sort.Order.desc("salary"), Sort.Order.asc("firstName"));
```
```java
@GetMapping("/employees")
public Page<EmployeeDto> list(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
    return service.findAll(pageable);      // ?page=0&size=20&sort=salary,desc
}
```
This becomes `LIMIT 20 OFFSET 0` in MySQL. `Page` runs an extra `COUNT(*)`; use `Slice` if you only need "has next".
⚠️ Pagination combined with a `JOIN FETCH` on a collection makes Hibernate paginate **in memory** (`HHH90003004` warning) — use `@EntityGraph` or a two-query approach instead.

---

## 8. Auditing (Exercise 7)

```java
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaConfig {
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext())
                             .map(c -> c.getAuthentication())
                             .map(Authentication::getName)
                             .or(() -> Optional.of("system"));
    }
}

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {
    @CreatedDate      @Column(updatable = false) private LocalDateTime createdAt;
    @LastModifiedDate                            private LocalDateTime updatedAt;
    @CreatedBy        @Column(updatable = false) private String createdBy;
    @LastModifiedBy                              private String updatedBy;
}

@Entity
public class Employee extends Auditable { }
```
**JPA lifecycle callbacks:** `@PrePersist`, `@PostPersist`, `@PreUpdate`, `@PostUpdate`, `@PreRemove`, `@PostRemove`, `@PostLoad`.
For full change history, use **Hibernate Envers** (`@Audited` → `_AUD` tables).

---

## 9. Projections (Exercise 8) ⭐

Return only the columns you need — smaller queries, no entity overhead.

```java
// 1. INTERFACE projection (closed) — Spring proxies it, selects only these columns
public interface EmployeeNameView {
    String getFirstName();
    String getEmail();
    @Value("#{target.firstName + ' ' + target.lastName}") String getFullName();  // open
}
List<EmployeeNameView> findByStatus(Status s);

// 2. CLASS / DTO projection — a constructor expression
public record EmployeeDto(Long id, String firstName, BigDecimal salary) { }
@Query("SELECT new com.cognizant.dto.EmployeeDto(e.id, e.firstName, e.salary) FROM Employee e")
List<EmployeeDto> findAllDtos();

// 3. DYNAMIC projection — the caller picks the shape
<T> List<T> findByStatus(Status s, Class<T> type);
repo.findByStatus(ACTIVE, EmployeeNameView.class);
repo.findByStatus(ACTIVE, Employee.class);
```
⭐ **Best practice:** never return `@Entity` objects from a REST controller — return **DTOs**. This avoids lazy-loading errors, infinite JSON recursion, over-fetching, and accidentally exposing internal fields.

---

## 10. Performance ⭐⭐

### The N+1 select problem (the #1 JPA interview question)
```java
List<Department> depts = repo.findAll();          // 1 query
for (Department d : depts)
    d.getEmployees().size();                      // N more queries — one per department!
```
**Fixes**
```java
// 1. JOIN FETCH
@Query("SELECT DISTINCT d FROM Department d LEFT JOIN FETCH d.employees")
List<Department> findAllWithEmployees();

// 2. @EntityGraph — declarative, works with derived queries & pagination
@EntityGraph(attributePaths = {"employees", "employees.projects"})
List<Department> findAll();

// 3. Batch fetching — turns N queries into N/size queries
spring.jpa.properties.hibernate.default_batch_fetch_size=20
// or @BatchSize(size = 20) on the collection

// 4. A DTO projection that joins in one query
```
**Detect it:** enable `spring.jpa.show-sql=true` and count the statements, or use `datasource-proxy` / p6spy / Hibernate statistics.

### `LazyInitializationException`
Accessing a LAZY association **after** the session closes.
**Fixes:** fetch inside the transaction (join fetch / entity graph), map to a DTO in the service layer, or use `@Transactional` on the service method. **Do not** "fix" it by setting `open-in-view=true` or switching to EAGER.

### Caching
| Level | Scope | Default |
|---|---|---|
| **L1 — persistence context** | one `EntityManager`/transaction | **always on**, can't disable |
| **L2 — SessionFactory** | shared across transactions | off; enable with EhCache/Hazelcast/Infinispan + `@Cacheable` (Hibernate's) |
| **Query cache** | query results | off; needs L2 |
Spring's own `@Cacheable`/`@CacheEvict` (Caffeine/Redis) sits at the **service** layer and is usually the better choice for application data.

### Other performance wins
- Batch inserts: `hibernate.jdbc.batch_size=50` + `order_inserts=true` (⚠️ **disabled by `GenerationType.IDENTITY`** — use SEQUENCE).
- `@Transactional(readOnly = true)` on query methods.
- Fetch only needed columns via projections.
- Index the FK and filter columns (see [03_MySQL.md §12](03_MySQL.md)).
- Avoid `@OneToMany` with `List` + `EAGER` (MultipleBagFetchException).
- Use `Set` for `@ManyToMany`.
- Tune the HikariCP pool size.
- Optimistic locking with `@Version` instead of pessimistic locks where contention is low.

### Locking
```java
@Version private Long version;                               // optimistic

@Lock(LockModeType.PESSIMISTIC_WRITE)                        // SELECT ... FOR UPDATE
@Query("SELECT a FROM Account a WHERE a.id = :id")
Account findByIdForUpdate(@Param("id") Long id);
```

---

## ⭐ Interview Gold — JPA / Hibernate Rapid Fire

| Q | A |
|---|---|
| JPA vs Hibernate vs Spring Data JPA? | Specification / implementation / repository abstraction that generates code from interfaces. |
| What is ORM and why use it? | Maps objects↔tables; removes JDBC boilerplate, adds caching, dirty checking, portability. |
| Requirements for an entity class? | `@Entity`, `@Id`, no-arg constructor, non-final class/fields. |
| `@Column` vs `@JoinColumn`? | A plain column vs a foreign-key column for a relationship. |
| `mappedBy` — what does it mean? | Marks the **inverse** (non-owning) side; the owning side holds the FK. |
| Which side owns a relationship? | The one with the FK — `@ManyToOne`, or the side without `mappedBy`. |
| Default fetch types? | `@ManyToOne`/`@OneToOne` EAGER; `@OneToMany`/`@ManyToMany` LAZY. |
| LAZY vs EAGER? | Load on first access (proxy) vs load immediately with the parent. Prefer LAZY + explicit fetching. |
| What is the N+1 problem and how do you fix it? | 1 query for parents + N for children; fix with `JOIN FETCH`, `@EntityGraph`, batch fetch size, or a DTO projection. |
| `LazyInitializationException` — cause and fix? | Touching a lazy association after the session closed; fetch inside the transaction or map to a DTO. |
| Entity lifecycle states? | Transient → Managed → Detached / Removed. |
| `save()` vs `persist()` vs `merge()`? | Spring's save delegates; persist makes transient→managed (void); merge copies detached state and returns the managed instance. |
| What is dirty checking? | Hibernate compares managed entities to their load-time snapshot and auto-issues UPDATEs at flush. |
| `getReferenceById` vs `findById`? | Lazy proxy, no SELECT vs immediate SELECT returning `Optional`. |
| First vs second level cache? | Per-`EntityManager` (always on) vs per-`SessionFactory`, shared, opt-in. |
| JPQL vs native SQL vs Criteria? | Entity-oriented & portable / DB-specific power / type-safe dynamic queries. |
| `@Transactional` — when does it roll back? | Unchecked exceptions and `Error` by default; add `rollbackFor` for checked ones. |
| Why doesn't `@Transactional` work on a self-invoked method? | The internal call bypasses the Spring proxy. |
| Propagation types? | REQUIRED (default), REQUIRES_NEW, SUPPORTS, NOT_SUPPORTED, MANDATORY, NEVER, NESTED. |
| Optimistic vs pessimistic locking? | `@Version` check at update (no DB lock) vs `SELECT ... FOR UPDATE` (blocks others). |
| What does `ddl-auto=update` do, and should you use it in prod? | Adds missing schema objects; **no** — use Flyway/Liquibase, and `validate` at most. |
| Why avoid returning entities from controllers? | Lazy-loading errors, recursive JSON, over-fetching, leaking internals. Return DTOs. |
| How do you paginate? | `Pageable`/`PageRequest.of(page, size, Sort)` → `Page<T>`; becomes LIMIT/OFFSET. |
| `Page` vs `Slice`? | `Page` runs an extra COUNT for total pages; `Slice` only knows if there's a next page. |
| What is `open-in-view` and why disable it? | Keeps the EntityManager open through view rendering, holding a connection and masking lazy issues. |
| How do you implement auditing? | `@EnableJpaAuditing` + `AuditingEntityListener` + `@CreatedDate`/`@CreatedBy` etc. |

---

## 📚 References
- Spring Data JPA Reference: https://docs.spring.io/spring-data/jpa/reference/
- Query derivation keywords: https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html
- Hibernate 6 User Guide: https://docs.jboss.org/hibernate/orm/6.4/userguide/html_single/Hibernate_User_Guide.html
- Jakarta Persistence Specification: https://jakarta.ee/specifications/persistence/
- Spring — Data Access & Transaction Management: https://docs.spring.io/spring-framework/reference/data-access/transaction.html
- **Vlad Mihalcea** (the best Hibernate performance resource): https://vladmihalcea.com/tutorials/hibernate/
- Baeldung — Spring Data JPA: https://www.baeldung.com/spring-data-jpa-query
- Baeldung — Hibernate N+1: https://www.baeldung.com/spring-hibernate-n1-problem
- Flyway: https://documentation.red-gate.com/fd
