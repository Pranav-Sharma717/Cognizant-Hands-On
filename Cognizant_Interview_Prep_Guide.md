# Cognizant Interview Prep Guide
*Compiled from 45+ real interview responses shared by placed candidates (VIT/GBU batch)*

---

## 🎯 PANELIST PATTERNS — Read This First

After going through every response, here's the actual shape of a Cognizant interview:

| Stage | What happens | Frequency |
|---|---|---|
| Opener | "Introduce yourself" / small talk (where are you from, college life) | ~90% of interviews |
| Core Java + OOPs | Overloading/overriding, 4 pillars, abstract vs interface, exceptions, final/finally/finalize | Near universal |
| Live coding (1–2 qs) | String/array manipulation — count, duplicate, reverse, sort | ~80% of interviews |
| SQL | Joins, GROUP BY/HAVING, keys, DDL/DML, nth highest salary | ~75% of interviews |
| Resume deep-dive | Whatever tech is on your resume — Spring Boot, React, AWS, ML/GenAI | Always, depth varies |
| HR closer | Relocation, shifts/24-7, willingness to learn new tech | Near universal |
| Wildcard | Logical puzzle, DBMS internals, compiler internals (AOT/JIT), design patterns | ~15–20% of panels |

**Key strategic takeaways:**
- **Java OOPs + basic string/array coding is your highest-ROI prep area.** It is asked more than anything else, by a wide margin.
- **SQL is not optional.** Even non-DB-heavy candidates got SQL joins/keys/2nd-highest-salary questions. Know it cold.
- **Panelists probe your resume aggressively** — one candidate (Krishank) got asked about "Stream API" and "Prompt/Context Engineering" specifically because it was on their resume. Only put things on your resume you can defend at whiteboard-code level.
- **They test communication as much as correctness.** Several candidates (Rudransh, Arshav) were asked to "explain OOPs to someone who doesn't know coding" or had a chatty warm-up before the "real" questions — panelists are watching how you explain, not just what you know.
- **The HR tail is predictable and non-negotiable.** Nearly everyone was asked about relocation, shift flexibility, and willingness to learn new tech. Decide your honest answers *before* you walk in — don't improvise these.
- **Some candidates got a logical puzzle** (bulb-switch, coin-weighing). These aren't common but cost people who freeze — practice 3–4 classic ones so you're not caught flat.

---

## SECTION 1 — Opening / Self-Introduction

**Q: Introduce yourself / Tell me about yourself**
Structure (30–45 seconds):
1. Name, college, branch, current year/status
2. 1–2 line summary of your strongest project or skill area
3. Core tech stack (Java, SQL, one framework)
4. One line on what you're looking for / why Cognizant

> Sample: *"I'm [name], a final-year CSE student at [college]. I've worked mainly with Java and Spring Boot, and built [project name], a [one-line description] where I handled [specific part]. I'm comfortable with SQL and have recently been exploring React for the frontend side. I'm looking to start my career somewhere I can work across the full stack, which is what drew me to this role."*

**Tip:** Multiple candidates got asked to explain their *whole* project pipeline in depth (Prateek: "extensive project discussion," Charitra: "explain both your projects"). Be ready to go 3 levels deep on any project you mention: what it does → how you built it → why you made specific technical choices.

---

## SECTION 2 — Core Java & OOPs (highest-frequency topic)

**Q: What is overloading vs overriding?**
- **Overloading**: same method name, different parameter list, resolved at *compile time* (compile-time/static polymorphism). Can be in the same class.
- **Overriding**: subclass redefines a method with the *same signature* as its parent, resolved at *runtime* (runtime/dynamic polymorphism). Requires inheritance.

**Q: What are the 4 pillars of OOPs? Explain each.**
- **Encapsulation** — bundling data + methods into one unit (class) and restricting direct access via access modifiers (private fields + public getters/setters).
- **Inheritance** — a class acquires properties/behavior of another class (`extends`), enabling code reuse.
- **Polymorphism** — one interface, many forms — compile-time (overloading) and runtime (overriding).
- **Abstraction** — hiding implementation details, exposing only essential features (via abstract classes/interfaces).

**Q: What is an abstract class? Write one.**
An abstract class can have both abstract (unimplemented) and concrete methods, cannot be instantiated, and is extended by subclasses.
```java
abstract class Shape {
    abstract double area();          // abstract method
    void display() {                 // concrete method
        System.out.println("Area: " + area());
    }
}
class Circle extends Shape {
    double radius;
    Circle(double r) { radius = r; }
    double area() { return Math.PI * radius * radius; }
}
```

**Q: Abstract class vs Interface?**
| Abstract Class | Interface |
|---|---|
| Can have constructors, instance variables | Only constants (`public static final`) |
| Can have concrete + abstract methods | Traditionally only abstract (Java 8+ allows default/static methods) |
| Single inheritance only | A class can implement multiple interfaces |
| Use when classes share a base state/behavior | Use to define a contract/capability |

**Q: final vs finally vs finalize?**
- `final` — keyword: a `final` variable can't be reassigned, `final` method can't be overridden, `final` class can't be extended.
- `finally` — a block that always executes after try/catch, used for cleanup (closing resources).
- `finalize()` — a method called by the garbage collector before an object is destroyed (deprecated in modern Java, rarely used now — good to mention this nuance if asked).

**Q: Explain exception handling / how do you handle exceptions in Java?**
Java uses `try-catch-finally` and `throw/throws`. Checked exceptions (e.g., `IOException`) must be declared or handled; unchecked exceptions (e.g., `NullPointerException`) extend `RuntimeException` and don't require declaration.
```java
try {
    int result = 10 / 0;
} catch (ArithmeticException e) {
    System.out.println("Error: " + e.getMessage());
} finally {
    System.out.println("Cleanup runs regardless");
}
```

**Q: throw vs throws?**
- `throw` — used *inside* a method to actually throw an exception instance: `throw new IllegalArgumentException("bad input")`.
- `throws` — used in a method *signature* to declare it might throw a checked exception: `void readFile() throws IOException`.

**Q: Is Java platform independent? Why?**
Yes — Java source compiles to bytecode (`.class`), which runs on any machine with a JVM. "Write once, run anywhere." The JVM itself is platform-*dependent* (a different JVM build per OS), but the bytecode it runs is universal.

**Q: JDK vs JRE vs JVM?**
- **JVM** — Java Virtual Machine, executes bytecode.
- **JRE** — JVM + core libraries, needed to *run* Java programs.
- **JDK** — JRE + compiler/dev tools, needed to *develop* Java programs.

**Q: Static vs Final keyword?**
`static` belongs to the class, not an instance (shared across all objects, accessed via class name). `final` means "cannot change once set" (variable/method/class immutability). They solve different problems and can be combined (`static final` = a class-level constant).

**Q: Access modifiers in Java?**
- `private` — class only
- default (no modifier) — same package
- `protected` — same package + subclasses (even in other packages)
- `public` — everywhere

**Q: Primitive vs non-primitive (reference) data types?**
Primitives (`int`, `char`, `boolean`, `double`, etc.) hold actual values directly in stack memory, fixed size, not objects. Non-primitives (`String`, arrays, custom classes) are references pointing to objects in heap memory, and can be `null`.

**Q: Wrapper classes — what and why?**
Wrapper classes (`Integer`, `Character`, `Boolean`, etc.) wrap primitives into objects — needed because Java Collections only store objects, not primitives. Auto-boxing/unboxing converts between them automatically.

**Q: What are templates/generics in Java?** *(asked as "templates" — Java's answer is generics)*
Generics let you write classes/methods that operate on a type parameter, giving compile-time type safety without code duplication: `List<String> list = new ArrayList<>();`

**Q: Constructors — parameterized vs normal (default)?**
Default constructor takes no arguments and Java auto-generates one if you define none. A parameterized constructor accepts arguments to initialize fields with specific values at object creation.

**Q: Destructors in Java?**
Java has no explicit destructors — garbage collection handles memory cleanup automatically; `finalize()` was the closest historic equivalent (now discouraged in favor of `try-with-resources`/`AutoCloseable`).

---

## SECTION 3 — Strings & Collections

**Q: String vs StringBuilder vs StringBuffer — and why is String immutable?**
- `String` is **immutable** — every modification creates a new object. This is by design: for security (used in class loading, network connections), thread-safety, and to allow string pooling (memory efficiency via the String Constant Pool).
- `StringBuilder` — mutable, **not thread-safe**, faster — use in single-threaded contexts.
- `StringBuffer` — mutable, **thread-safe** (synchronized methods), slightly slower.

**Q: Difference between substring and split? What does split return?**
`substring(start, end)` extracts a portion of a string and returns a `String`. `split(regex)` breaks a string into pieces based on a delimiter/regex and returns a `String[]` array.

**Q: Array vs ArrayList?**
| Array | ArrayList |
|---|---|
| Fixed size | Dynamically resizable |
| Can hold primitives | Only holds objects (autoboxes primitives) |
| Faster (no overhead) | Slightly slower, more flexible (built-in methods) |

**Q: Can ArrayList hold primitive data types?**
Not directly — it stores objects, so primitives get auto-boxed (e.g., `int` → `Integer`) when added.

**Q: HashMap vs HashSet?**
`HashMap` stores key-value pairs, keys are unique. `HashSet` stores unique elements only (internally backed by a HashMap where your elements are the keys). Neither maintains insertion order (use `LinkedHashMap`/`LinkedHashSet` if you need that, or `TreeMap`/`TreeSet` for sorted order).

**Q: What are Java Collections? Types of DDL-like categorization (DDL/DML/TCL was actually SQL — see SQL section)**
Java Collections Framework provides `List` (ordered, duplicates allowed — `ArrayList`, `LinkedList`), `Set` (unique elements — `HashSet`, `TreeSet`), `Map` (key-value — `HashMap`, `TreeMap`), and `Queue`/`Deque`.

**Q: Mutability vs Immutability?**
Mutable objects can change state after creation (`StringBuilder`, `ArrayList`). Immutable objects cannot (`String`, wrapper classes, records) — any "change" creates a new object. Immutability aids thread-safety and predictability.

---

## SECTION 4 — Live Coding Questions (the most-repeated set — practice these by hand)

**1. Count vowels in a string** *(asked constantly — practically guaranteed)*
```java
String str = "Cognizant Interview";
int count = 0;
for (char c : str.toLowerCase().toCharArray()) {
    if ("aeiou".indexOf(c) != -1) count++;
}
System.out.println("Vowel count: " + count);
```

**2. Reverse a string**
```java
String str = "hello";
String reversed = new StringBuilder(str).reverse().toString();
```

**3. Find duplicate characters in a string with their count**
```java
String str = "programming";
Map<Character, Integer> freq = new HashMap<>();
for (char c : str.toCharArray())
    freq.put(c, freq.getOrDefault(c, 0) + 1);
for (Map.Entry<Character, Integer> e : freq.entrySet())
    if (e.getValue() > 1) System.out.println(e.getKey() + ": " + e.getValue());
```

**4. Remove duplicate characters from a string**
```java
String str = "programming";
LinkedHashSet<Character> set = new LinkedHashSet<>();
for (char c : str.toCharArray()) set.add(c);
StringBuilder sb = new StringBuilder();
for (char c : set) sb.append(c);
System.out.println(sb);
```

**5. Remove duplicates from an array**
```java
int[] arr = {1, 2, 2, 3, 4, 4, 5};
LinkedHashSet<Integer> set = new LinkedHashSet<>();
for (int x : arr) set.add(x);
System.out.println(set);
```

**6. Find 2nd largest element in an array**
```java
int[] arr = {12, 35, 1, 10, 34, 1};
int first = Integer.MIN_VALUE, second = Integer.MIN_VALUE;
for (int x : arr) {
    if (x > first) { second = first; first = x; }
    else if (x > second && x != first) second = x;
}
System.out.println("2nd largest: " + second);
```

**7. Check palindrome**
```java
String str = "madam";
String rev = new StringBuilder(str).reverse().toString();
System.out.println(str.equals(rev) ? "Palindrome" : "Not palindrome");
```

**8. Check anagram**
```java
String a = "listen", b = "silent";
char[] c1 = a.toCharArray(), c2 = b.toCharArray();
Arrays.sort(c1); Arrays.sort(c2);
System.out.println(Arrays.equals(c1, c2) ? "Anagram" : "Not anagram");
```

**9. Sort a list of strings by length (and implement using Comparator)**
```java
List<String> words = Arrays.asList("banana", "fig", "apple", "kiwi");
words.sort(Comparator.comparingInt(String::length));
// or with a full Comparator implementation:
Collections.sort(words, new Comparator<String>() {
    public int compare(String a, String b) {
        return a.length() - b.length();
    }
});
```

**10. Print odd numbers / print even numbers in descending order**
```java
for (int i = 20; i >= 1; i--) {
    if (i % 2 == 0) System.out.println(i);   // even, descending
}
```

**11. Frequency of a letter in a string**
```java
String str = "mississippi";
char target = 's';
long count = str.chars().filter(ch -> ch == target).count();
```

**12. Replace vowels in a string with `*`**
```java
String str = "hello world";
String result = str.replaceAll("(?i)[aeiou]", "*");
```

**13. Armstrong number check**
```java
int num = 153, sum = 0, temp = num;
while (temp != 0) {
    int digit = temp % 10;
    sum += digit * digit * digit;
    temp /= 10;
}
System.out.println(sum == num ? "Armstrong" : "Not Armstrong");
```

**14. Counter with a shared count (thread-safety flavored question)**
```java
class Counter {
    private int count = 0;
    public synchronized void increment() { count++; }
    public int getCount() { return count; }
}
// Point: synchronized prevents race conditions when multiple threads share this object
```

**15. Call a class within another class *without* using inheritance (i.e., composition)**
```java
class Engine {
    void start() { System.out.println("Engine started"); }
}
class Car {
    private Engine engine = new Engine(); // composition, not inheritance
    void drive() {
        engine.start();
        System.out.println("Car driving");
    }
}
```

**16. Regex — extract username from an email (replace `@` and `.`)**
```java
String email = "john.doe@gmail.com";
String cleaned = email.replaceAll("[@.]", " ");
```

**17. Print first name in descending order, last name in ascending order (character sort)**
```java
String first = "John", last = "Doe";
char[] f = first.toCharArray(); Arrays.sort(f);
StringBuilder fDesc = new StringBuilder(new String(f)).reverse();
char[] l = last.toCharArray(); Arrays.sort(l);
System.out.println(fDesc + " " + new String(l));
```

**Python variant** *(one candidate got a Python sort-names question — know the syntax difference)*
```python
names = ["Charlie", "Alice", "Bob"]
names.sort()  # ascending; use sort(reverse=True) for descending
```

**Coding-round tip:** Every single one of these is a variant of "manipulate a string or array + print/count something." Practice writing them **without an IDE** — panelists watch you write on a whiteboard/shared doc, so muscle memory on syntax (not just logic) matters.

---

## SECTION 5 — SQL (2nd most-tested area — know this cold)

**Q: Categories of SQL commands (DDL, DML, DCL, TCL)?**
- **DDL** (Data Definition Language): `CREATE`, `ALTER`, `DROP`, `TRUNCATE` — defines structure
- **DML** (Data Manipulation Language): `INSERT`, `UPDATE`, `DELETE`, `SELECT`(sometimes classed as DQL) — manipulates data
- **DCL** (Data Control Language): `GRANT`, `REVOKE` — permissions
- **TCL** (Transaction Control Language): `COMMIT`, `ROLLBACK`, `SAVEPOINT` — transaction management

**Q: DELETE vs DROP vs TRUNCATE?**
| | DELETE | TRUNCATE | DROP |
|---|---|---|---|
| Type | DML | DDL | DDL |
| Removes | Specific rows (with WHERE) | All rows, keeps structure | Entire table + structure |
| Rollback | Yes | Usually no (auto-commit) | No |
| Speed | Slower (row-by-row, logged) | Faster (deallocates pages) | Instant |

**Q: WHERE vs HAVING?**
`WHERE` filters rows *before* grouping/aggregation; `HAVING` filters groups *after* `GROUP BY` is applied. `WHERE` can't use aggregate functions (`COUNT`, `SUM`); `HAVING` can.

**Q: Primary Key vs Unique Key vs Foreign Key vs Composite Key?**
- **Primary Key** — uniquely identifies each row, no NULLs, one per table.
- **Unique Key** — enforces uniqueness but allows one NULL, can have multiple per table.
- **Foreign Key** — references a primary key in another table to enforce referential integrity.
- **Composite Key** — a primary key made of two or more columns combined.

**Q: Types of joins in SQL?**
- `INNER JOIN` — only matching rows in both tables
- `LEFT JOIN` — all rows from left + matches from right (NULL if none)
- `RIGHT JOIN` — all rows from right + matches from left
- `FULL OUTER JOIN` — all rows from both, matched where possible

**Q: Write a query for the Nth highest salary (classic — asked as 2nd/10th highest repeatedly)**
```sql
-- Nth highest salary, generic pattern:
SELECT DISTINCT salary
FROM employee
ORDER BY salary DESC
LIMIT 1 OFFSET (N-1);   -- N=2 for 2nd highest, N=10 for 10th

-- Alternative (portable across DBs, no LIMIT/OFFSET):
SELECT salary FROM employee e1
WHERE (N-1) = (
    SELECT COUNT(DISTINCT salary) FROM employee e2
    WHERE e2.salary > e1.salary
);
```

**Q: Write a query using GROUP BY and HAVING**
```sql
SELECT department, COUNT(*) AS emp_count
FROM employee
GROUP BY department
HAVING COUNT(*) > 5;
```

**Q: Write a query using a subquery**
```sql
SELECT name FROM employee
WHERE salary > (SELECT AVG(salary) FROM employee);
```

**Q: What is ACID and why does it matter?**
- **Atomicity** — a transaction fully completes or fully fails (no partial state).
- **Consistency** — a transaction moves the DB from one valid state to another.
- **Isolation** — concurrent transactions don't interfere with each other.
- **Durability** — once committed, changes survive system failure.
It matters because it guarantees reliable, predictable behavior for concurrent, real-world database operations (e.g., banking transactions).

**Q: DBMS vs RDBMS?**
DBMS stores data as files/collections, no relationships enforced. RDBMS (Relational DBMS) stores data in tables with defined relationships, enforces ACID properties, and supports SQL/normalization (e.g., MySQL, PostgreSQL vs. plain file-based systems).

**Q: PostgreSQL vs Neo4j?**
PostgreSQL is a relational (SQL) database — data in tables with defined schemas and relationships via keys. Neo4j is a graph database — data as nodes and edges, optimized for highly connected data (social networks, recommendation engines) where relationship traversal matters more than tabular joins.

---

## SECTION 6 — Design Patterns / SOLID Principles

**Q: Explain SOLID design principles**
- **S**ingle Responsibility — a class should have one reason to change.
- **O**pen/Closed — open for extension, closed for modification.
- **L**iskov Substitution — subclasses should be substitutable for their base class without breaking behavior.
- **I**nterface Segregation — prefer many small, specific interfaces over one large one.
- **D**ependency Inversion — depend on abstractions, not concrete implementations.

**Q: How do you achieve Interface Segregation?**
Break large interfaces into smaller, role-specific ones so implementing classes only depend on methods they actually use, rather than being forced to implement irrelevant methods.

**Q: Explain the Singleton design pattern. How do you restrict instance creation?**
Ensures a class has only one instance, with a global access point. Achieved with a private constructor, a static instance variable, and a static `getInstance()` method.
```java
class Singleton {
    private static Singleton instance;
    private Singleton() {}  // private constructor restricts external instantiation
    public static Singleton getInstance() {
        if (instance == null) instance = new Singleton();
        return instance;
    }
}
```

---

## SECTION 7 — Web / Frameworks (Spring Boot, React) — if it's on your resume, expect depth

**Q: What is a Controller in Spring Boot?**
A `@Controller`/`@RestController` handles incoming HTTP requests, maps them to handler methods via `@RequestMapping`/`@GetMapping`/`@PostMapping`, and returns a response (view or JSON/data directly for REST APIs).

**Q: Common Spring Boot annotations?**
`@SpringBootApplication`, `@RestController`, `@Autowired` (dependency injection), `@RequestMapping`/`@GetMapping`/`@PostMapping`, `@Service`, `@Repository`, `@Entity`.

**Q: What's a database manipulation workflow in a Spring Boot app?**
Typically: Controller (receives request) → Service (business logic) → Repository/DAO (via Spring Data JPA) → Entity mapped to a DB table. JPA/Hibernate handles the ORM layer between Java objects and SQL.

**Q: What is a Component in React? Frameworks in React ecosystem?**
A Component is a reusable, self-contained piece of UI (function or class) that returns JSX. React itself is a library, not a framework, but is often paired with Next.js. Related state-management tools: Redux, Context API.

**Q: Angular vs React?**
Angular is a full MVC framework (opinionated, TypeScript-first, two-way data binding). React is a UI library (unopinionated, JSX, one-way data flow, needs additional libraries for routing/state).

**Q: React props vs component basics?**
Props are read-only data passed from a parent component to a child, enabling reusable, configurable components.

**Q: Conditional rendering in React?**
Rendering different UI based on a condition, typically via ternary operators or `&&` inside JSX:
```jsx
{isLoggedIn ? <Dashboard /> : <Login />}
```

**Q: JavaScript Promises?**
An object representing the eventual completion (or failure) of an async operation, with `.then()`/`.catch()`, or `async/await` syntax as a cleaner alternative.

---

## SECTION 8 — Cloud, APIs, Architecture (if resume-relevant)

**Q: What is AWS EC2 / VPC?**
**EC2** (Elastic Compute Cloud) — virtual servers in the cloud you can configure and scale on demand. **VPC** (Virtual Private Cloud) — an isolated network environment within AWS where you control IP ranges, subnets, and routing for your resources.

**Q: How does an API work? REST APIs in depth?**
A REST API exposes resources via URLs, using HTTP methods (`GET`, `POST`, `PUT`, `DELETE`) that map to CRUD operations. It's stateless — each request contains all info needed, typically exchanging JSON, following resource-based URL conventions (`/users/123`).

**Q: What is JWT and authorization?**
JSON Web Token — a compact, signed token used to securely transmit claims (like user identity) between parties. After login, the server issues a JWT; the client sends it in the `Authorization` header on subsequent requests, and the server verifies its signature instead of re-checking credentials each time.

**Q: Microservices — what and why?**
An architectural style where an application is split into small, independently deployable services, each owning its own data and communicating via APIs (often REST or message queues) — improves scalability and fault isolation vs. a single monolith.

**Q: AOT vs JIT compiler?**
- **JIT (Just-In-Time)** — Java's default: compiles bytecode to native machine code *at runtime*, as the program executes, enabling runtime optimizations.
- **AOT (Ahead-Of-Time)** — compiles bytecode to native code *before* execution (at build time), reducing startup time at the cost of some runtime optimization flexibility.

---

## SECTION 9 — AI/ML (asked when it's on your resume)

**Q: AI vs ML vs DL?**
AI is the broad field of making machines simulate intelligence. ML is a subset — algorithms that learn patterns from data rather than being explicitly programmed. DL is a subset of ML using multi-layer neural networks, especially strong for unstructured data (images, text).

**Q: Explain Deep Learning and CNNs**
Deep Learning uses layered neural networks to progressively extract higher-level features from raw data. A **CNN** (Convolutional Neural Network) is specialized for grid-like data (images) — using convolutional layers to detect spatial patterns (edges, shapes) and pooling layers to reduce dimensionality.

**Q: Gen AI vs RAG?**
Generative AI creates new content (text, images) from learned patterns. **RAG** (Retrieval-Augmented Generation) enhances a generative model by first retrieving relevant external documents/data and feeding them into the model's context — grounding responses in real, up-to-date information instead of relying purely on the model's trained knowledge.

**Q: What is numpy? Inboxing vs outboxing?**
`numpy` is a Python library for efficient numerical computation with n-dimensional arrays. "Inboxing/outboxing" likely refers to **boxing/unboxing** — converting between primitive types and their object wrapper equivalents (e.g., `int` ↔ `Integer` in Java).

---

## SECTION 10 — Logical Puzzles (low frequency, but freeze-inducing if unprepared)

**Q: You can enter the room only once. There are 3 switches outside and 1 bulb inside. How do you determine which switch controls the bulb?**
Turn switch 1 ON, wait a few minutes, turn it OFF. Turn switch 2 ON, leave switch 3 OFF, then enter the room:
- Bulb ON → switch 2
- Bulb OFF but warm → switch 1
- Bulb OFF and cold → switch 3

**Q: 8 coins puzzle (find the odd one out)**
Classic setup: 8 identical-looking coins, one is heavier/lighter, using a balance scale minimum times. Approach: split into groups of 3-3-2, weigh the two groups of 3 first — if balanced, the odd coin is in the group of 2 (weigh those two directly); if unbalanced, narrow down within the heavier/lighter group of 3.

**General puzzle strategy:** Think out loud. Panelists are evaluating your reasoning process, not just the final answer — silence is worse than a wrong first guess you then correct.

---

## SECTION 11 — HR / Closing Questions (near-guaranteed — decide answers in advance)

These came up repeatedly, almost verbatim, across candidates:

- **"Are you willing to relocate?"**
- **"Are you okay working in different tech stacks (even if not your primary skill, e.g. testing)?"**
- **"Are you willing to work night shifts / 24×7 rotational shifts?"**
- **"Are you willing to learn new technologies?"**

**Tip:** Answer these honestly but affirmatively where you can — Cognizant is a services company with rotational, client-driven staffing, and hesitation here is a common reason strong technical candidates don't get selected. If you have a genuine constraint, phrase it as a preference, not a refusal ("I'd prefer to start in X, but I'm open to relocating as needed").

---

## FINAL DAY-OF CHECKLIST

1. **Nail these 4 without hesitation:** OOPs 4 pillars, overloading vs overriding, abstract class vs interface, exception handling — asked in almost every single interview reviewed.
2. **Hand-code these 5 without an IDE:** count vowels, reverse string, find/remove duplicates, 2nd largest element, palindrome/anagram check.
3. **SQL: memorize the nth-highest-salary query pattern** and be able to explain joins + GROUP BY/HAVING + key types out loud.
4. **Know every line of your resume at code-level depth** — panelists will ask you to explain *why*, not just *what*, for any tech you list.
5. **Prepare your relocation/shift/new-tech answers in advance** — don't improvise these live.
6. **Practice explaining one technical concept to a non-technical person** — several candidates were tested on communication this way.
7. **If you freeze on a puzzle, think out loud** — the process is being evaluated, not just the answer.

Good luck — you've got a strong, realistic picture of exactly what's coming.
