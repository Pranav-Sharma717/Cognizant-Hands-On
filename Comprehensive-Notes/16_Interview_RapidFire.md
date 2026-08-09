# 16 — Interview Rapid-Fire & Cognizant Drive Guide

> The last thing you read before the interview. Every answer here is compressed to what you should actually *say*.

---

## 1. The Cognizant Digital Nurture / GenC Java FSE process

A typical Cognizant campus/DN drive runs roughly:

| Stage | What to expect |
|---|---|
| **1. Aptitude & Reasoning** | Quantitative, logical, verbal — time-boxed, negative marking possible |
| **2. Technical MCQs** | Java, OOP, DBMS/SQL, DSA, OS/networking basics, sometimes React/JS |
| **3. Coding round** | 1–2 problems, easy→medium: arrays, strings, HashMap, sorting, recursion. Java allowed |
| **4. Technical interview** | Your projects + Java/Spring/SQL fundamentals + live code on paper/shared editor |
| **5. Managerial / HR** | Behavioural, relocation, bond/service agreement, communication |

**What they're really checking in the technical round:** can you explain *your own* repository code, do you understand OOP and collections deeply, can you write a correct SQL query, do you know how a Spring Boot request flows end to end, and can you reason out loud.

> ⚠️ Exact stage names and formats vary by year and by the specific drive. Confirm against the official communication you received — treat this as a preparation guide, not the published process.

---

## 2. The 60-second self-introduction (rehearse out loud)

> "I'm Pranav Sharma, a final-year student and a participant in the Cognizant Digital Nurture 5.0 Java Full Stack Engineer track. Over the program I built hands-on modules covering the full stack: core Java and DSA, design patterns, PL/SQL, test-driven development with JUnit 5 and Mockito, Spring Core and Maven, Spring Data JPA with Hibernate, REST APIs secured with Spring Security and JWT, a set of microservices using Spring Cloud — Eureka, API Gateway, OpenFeign and Resilience4j — and a React front end consuming those APIs. Everything is in a public GitHub repository with week-by-week exercises and notes. I'm most comfortable on the backend, particularly Spring Boot and SQL, and I'd like to grow into designing services end to end."

**Why it works:** names the program, lists the exact stack, cites evidence (the repo), states a preference and a growth direction.

---

## 3. Explaining your project — the STAR script

Pick **one** module (the microservices one is the strongest) and prepare this:

**Situation** — "In Week 4 I built a set of microservices simulating an order-management domain: user-service, order-service, payment-service and inventory-service."
**Task** — "They had to communicate reliably, be discoverable without hard-coded URLs, and survive a downstream service being unavailable."
**Action** — "Each service is a Spring Boot 3 app with its own MySQL schema. They register with Eureka and call each other through OpenFeign using logical service names, so Spring Cloud LoadBalancer handles instance selection client-side. All external traffic goes through Spring Cloud Gateway, which does routing, path rewriting, rate limiting and centralises authentication. I wrapped the payment call in a Resilience4j circuit breaker with a fallback so that when payment-service is down, orders queue instead of the whole flow failing. Authentication is JWT-based so the services stay stateless."
**Result** — "The system stayed responsive with a service deliberately killed, and each service could be deployed independently. What I'd add next is distributed tracing with Micrometer and Zipkin, because debugging a request across four services from logs alone was the hardest part."

⭐ **Always end with what you'd improve** — it shows self-assessment rather than overclaiming.

**Have ready for every project:** why you chose that design, one hard bug you fixed, one thing you'd do differently, and the exact commands to run it.

---

## 4. THE TOP 50 — if you only revise one thing

### Java (1–20)
1. **JDK vs JRE vs JVM** — dev kit ⊃ runtime ⊃ virtual machine that executes bytecode.
2. **Is Java pass-by-value?** Always. For objects the *reference value* is copied — you can mutate the object, not reassign the caller's variable.
3. **`==` vs `.equals()`** — reference/primitive comparison vs logical content comparison.
4. **`equals`/`hashCode` contract** — equal objects must have equal hash codes; always override together or `HashMap`/`HashSet` break.
5. **Why is `String` immutable?** String-pool sharing, thread safety, cached hashCode, security.
6. **String vs StringBuilder vs StringBuffer** — immutable / mutable fast / mutable synchronized.
7. **Overloading vs overriding** — compile-time, different params vs runtime, same signature.
8. **Abstract class vs interface** — state + constructors + single inheritance vs contract + multiple inheritance + default methods.
9. **Why no multiple class inheritance?** The diamond problem — ambiguous state and method resolution.
10. **ArrayList vs LinkedList** — array, O(1) access vs doubly-linked, O(1) head insert.
11. **How does HashMap work?** hash spreading → `(n-1)&hash` bucket → chain → **treeify at 8** (capacity ≥64) → resize at load factor **0.75** from default capacity **16**.
12. **HashMap vs Hashtable vs ConcurrentHashMap** — unsynchronized / fully synchronized legacy / bucket-level CAS+sync with weakly-consistent iterators.
13. **fail-fast vs fail-safe** — `modCount` check → `ConcurrentModificationException` vs snapshot/weakly-consistent iteration.
14. **Checked vs unchecked exceptions** — compiler-enforced recoverable conditions vs programming bugs.
15. **`final` / `finally` / `finalize`** — keyword / always-executed block / deprecated GC hook.
16. **What's new in Java 8?** Lambdas, Streams, functional interfaces, `Optional`, default methods, `java.time`.
17. **`map` vs `flatMap`** — 1→1 vs 1→many flattened.
18. **`start()` vs `run()`** — new thread vs plain method call.
19. **`synchronized` vs `volatile` vs `Atomic`** — mutual exclusion / visibility only / lock-free CAS atomicity.
20. **Stack vs Heap** — per-thread frames (`StackOverflowError`) vs shared object store (`OutOfMemoryError`).

### SQL (21–30)
21. **DELETE vs TRUNCATE vs DROP** — DML rollbackable / DDL fast reset / removes the structure.
22. **WHERE vs HAVING** — rows before grouping vs groups after grouping.
23. **All join types** — INNER, LEFT, RIGHT, FULL (not in MySQL), CROSS, SELF.
24. **SQL logical execution order** — FROM → JOIN → WHERE → GROUP BY → HAVING → SELECT → ORDER BY → LIMIT.
25. **What's an index and its downside?** B+Tree lookup structure; slows writes and costs space.
26. **When is an index not used?** Function on the column, leading `%`, type mismatch, low selectivity.
27. **ACID** — Atomicity, Consistency, Isolation, Durability.
28. **Isolation levels** — READ UNCOMMITTED/COMMITTED/REPEATABLE READ (MySQL default)/SERIALIZABLE, preventing dirty/non-repeatable/phantom reads respectively.
29. **Normalization to 3NF** — atomic values, no partial dependency, no transitive dependency.
30. **Nth highest salary** — `DENSE_RANK() OVER (ORDER BY salary DESC)` filtered to N, or `LIMIT 1 OFFSET N-1` on distinct salaries.

### Spring (31–42)
31. **IoC and DI** — the container controls creation; dependencies are handed in rather than constructed.
32. **Which injection type and why?** Constructor — required deps enforced, `final` fields, testable without Spring.
33. **`@Component` vs `@Bean`** — scanned class vs explicit factory method (works for third-party classes).
34. **Bean scopes** — singleton (default), prototype, request, session, application, websocket.
35. **Are singleton beans thread-safe?** No — one shared instance; keep them stateless.
36. **What does `@SpringBootApplication` do?** `@SpringBootConfiguration` + `@EnableAutoConfiguration` + `@ComponentScan`.
37. **How does auto-configuration work?** Conditional configuration classes applied based on classpath and existing beans.
38. **Spring MVC request flow** — DispatcherServlet → HandlerMapping → Controller → Service → Repository → Entity, then a message converter serialises the response.
39. **`@Transactional` gotchas** — rolls back on unchecked exceptions only by default; self-invocation bypasses the proxy; public methods only.
40. **N+1 problem and fixes** — `JOIN FETCH`, `@EntityGraph`, batch fetch size, DTO projection.
41. **LAZY vs EAGER, and the defaults** — `@ManyToOne`/`@OneToOne` are EAGER; `@OneToMany`/`@ManyToMany` are LAZY. Prefer LAZY everywhere.
42. **How does AOP work in Spring?** Runtime proxies (JDK dynamic or CGLIB) that wrap beans — the mechanism behind `@Transactional`, `@Cacheable`, `@Async`.

### REST / Security / Microservices / React (43–50)
43. **REST principles** — stateless, client-server, cacheable, uniform interface, layered.
44. **Idempotent methods** — GET, PUT, DELETE, HEAD, OPTIONS (not POST).
45. **401 vs 403** — not authenticated vs authenticated but not permitted.
46. **JWT structure & flow** — header.payload.signature; login → signed token → `Authorization: Bearer` on every request; stateless, hard to revoke.
47. **Circuit breaker states** — CLOSED → OPEN → HALF_OPEN; prevents cascading failure.
48. **Why an API Gateway?** One entry point for routing, auth, rate limiting, CORS and tracing.
49. **Virtual DOM & reconciliation** — in-memory tree diffed against the previous one to compute minimal real-DOM updates; `key` matches list children.
50. **`useState` vs `useEffect` vs `useContext`** — local state / side effects with a dependency array and cleanup / consume shared context without prop drilling.

---

## 5. Coding round — the patterns that actually appear

**Practise writing these from memory, in Java, without an IDE.**

| Pattern | Problems |
|---|---|
| **HashMap frequency** | duplicates, first non-repeating char, anagram, two-sum, majority element |
| **Two pointers** | pair sum in a sorted array, reverse, palindrome, remove duplicates, container with most water |
| **Sliding window** | max sum of k elements, longest substring without repeats |
| **String manipulation** | reverse words, count vowels, capitalise, compress, valid parentheses |
| **Sorting + Comparator** | sort objects by multiple fields, top-N |
| **Recursion** | factorial, fibonacci (+memo), power, sum of digits, Hanoi |
| **Matrix** | transpose, rotate 90°, spiral print |
| **Number theory** | prime check, Armstrong, palindrome number, GCD, reverse digits |
| **Streams** | group by, sum, filter+map+collect, sort a map by value |

**Template answers to have muscle memory for:**
```java
// two-sum with a map — O(n)
Map<Integer,Integer> seen = new HashMap<>();
for (int i = 0; i < a.length; i++) {
    if (seen.containsKey(target - a[i])) return new int[]{seen.get(target - a[i]), i};
    seen.put(a[i], i);
}

// frequency map
Map<Character,Integer> f = new HashMap<>();
for (char c : s.toCharArray()) f.merge(c, 1, Integer::sum);

// reverse a string
new StringBuilder(s).reverse().toString();

// sort objects
list.sort(Comparator.comparing(Emp::getDept).thenComparing(Emp::getSalary, reverseOrder()));

// group with streams
map = list.stream().collect(groupingBy(Emp::getDept, counting()));
```

**How to behave in the round**
1. Restate the problem and confirm the input/output with an example.
2. Ask about edge cases: empty, null, negatives, duplicates, size limits.
3. State the brute force + its complexity, then propose the optimisation.
4. **Think out loud** — the interviewer scores your reasoning, not just the final code.
5. Write clean code: meaningful names, no magic numbers.
6. Dry-run on the example, then state the time and space complexity yourself.

---

## 6. SQL round — five queries to have memorised

```sql
-- 1. Nth highest salary
SELECT salary FROM (SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) r
                    FROM employees) t WHERE r = 3;

-- 2. Employees with no department (anti-join)
SELECT e.* FROM employees e LEFT JOIN departments d ON e.dept_id = d.dept_id
WHERE d.dept_id IS NULL;

-- 3. Duplicates
SELECT email, COUNT(*) c FROM employees GROUP BY email HAVING c > 1;

-- 4. Department-wise average, only above a threshold
SELECT d.dept_name, AVG(e.salary) avg_sal, COUNT(*) headcount
FROM employees e JOIN departments d ON e.dept_id = d.dept_id
GROUP BY d.dept_name HAVING AVG(e.salary) > 60000 ORDER BY avg_sal DESC;

-- 5. Top 3 earners per department
SELECT * FROM (SELECT *, DENSE_RANK() OVER (PARTITION BY dept_id ORDER BY salary DESC) r
               FROM employees) t WHERE r <= 3;
```

---

## 7. Questions you should ask them

Never say "no questions". Have three ready:
- "What does a typical first six months look like for a GenC on a Java project — training, shadowing, then delivery?"
- "Which parts of the stack are teams currently modernising? Are projects mostly Spring Boot 3 and Java 17+, or is there legacy migration work?"
- "How is code quality maintained — are there code reviews, SonarQube gates, automated pipelines?"
- "What would make someone stand out in this role in the first year?"

---

## 8. HR round — prepared answers

| Question | Approach |
|---|---|
| Tell me about yourself | The 60-second script in §2 |
| Why Cognizant? | Scale and variety of client projects, structured GenC training (Digital Nurture itself), exposure to full-stack delivery, strong engineering practices |
| Strengths | Pick two with evidence: "structured learning — I completed and documented every weekly module in a public repo"; "debugging persistence — the N+1 problem in my JPA module" |
| Weakness | A real one plus the correction: "I used to over-engineer solutions early. I now write the simplest working version first and refactor once tests pass." Never say "perfectionist". |
| Where in 5 years? | Deep backend expertise → designing services and mentoring, ideally with client-facing exposure |
| Are you willing to relocate / work in any technology? | Answer honestly. Flexibility helps, but don't commit to something you'll regret. |
| Service agreement / bond | Ask clearly about the duration and terms; it's a legitimate question |
| Why should we hire you? | Fundamentals + proof of self-directed learning + you can already build the exact stack they deliver on |

---

## 9. Weak-answer traps to avoid

| ❌ Don't say | ✅ Say instead |
|---|---|
| "Spring Boot is better than Spring" | "Spring Boot is Spring plus auto-configuration, starters and an embedded server" |
| "Microservices are always better" | "They trade simplicity for independent deployability — worth it when scale or team size justifies it" |
| "I know everything about X" | "I've used X for A and B; I haven't worked with C yet" |
| "JWT is encrypted" | "JWT is signed and Base64-encoded — readable, but tamper-evident" |
| "100% test coverage" | "High branch coverage on business logic, with meaningful assertions" |
| Silence when stuck | "I'm not sure — my instinct is X because Y. Can I reason through it?" |
| Blaming a teammate/tool | Own the problem and describe what you changed |

⭐ **Saying "I don't know, but here's how I'd find out" is a strong answer.** Bluffing is the fastest way to fail a technical round.

---

## 10. Final-week revision plan

| Day | Focus |
|---|---|
| **7** | [01](01_Java_Core_Basics.md) + [02](02_Java_OOP.md) — Java basics & OOP. Write 10 small programs by hand. |
| **6** | [04](04_Java_Collections_Generics.md) — Collections. Draw the HashMap internals from memory. |
| **5** | [03](03_MySQL.md) — SQL. Solve 20 queries (LeetCode Top SQL 50). |
| **4** | [05](05_DSA_Java.md) — DSA. Code sorting, searching, linked list, recursion without an IDE. |
| **3** | [10](10_Spring_Core_Maven_AOP.md) + [11](11_Spring_Data_JPA_Hibernate.md) + [12](12_Spring_Boot_REST_Security.md) — the Spring stack. Trace one request end to end. |
| **2** | [06](06_Design_Patterns_SOLID.md) + [09](09_TDD_JUnit_Mockito_Logging.md) + [13](13_Microservices.md) + [14](14_React.md) — patterns, testing, microservices, React. |
| **1** | **This file only.** Rehearse the intro and the project script out loud. Sleep. |

---

## 11. Interview-day checklist

- [ ] Repo pushed, README current, `target/` and `node_modules/` gitignored
- [ ] Can run every project locally in under a minute (`mvn spring-boot:run`, `npm start`)
- [ ] Resume matches the repo — no technology listed that you can't discuss
- [ ] Intro and project script rehearsed **out loud**, not just read
- [ ] Pen and paper ready for whiteboard code
- [ ] Stable internet, quiet room, camera on, plain background (if virtual)
- [ ] Three questions prepared for them
- [ ] `git log --oneline` skimmed so you remember what you actually built

---

## 📚 Practice resources
- LeetCode — Top Interview 150: https://leetcode.com/studyplan/top-interview-150/
- LeetCode — Top SQL 50: https://leetcode.com/studyplan/top-sql-50/
- HackerRank — Java & SQL tracks: https://www.hackerrank.com/domains/java
- NeetCode roadmap: https://neetcode.io/roadmap
- Baeldung — Spring interview questions: https://www.baeldung.com/spring-interview-questions
- GeeksforGeeks — Java interview questions: https://www.geeksforgeeks.org/java-interview-questions/
- Spring Guides: https://spring.io/guides
- *Effective Java* — Joshua Bloch
- *Cracking the Coding Interview* — Gayle Laakmann McDowell

---

> **The one thing to remember:** interviewers are not testing recall, they're testing whether you can *reason* about a system. Explain your thinking, admit gaps honestly, and connect every answer back to something you actually built in this repository. You have the evidence — use it.
