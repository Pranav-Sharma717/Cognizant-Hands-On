# 08 — Java Advanced (Exceptions · Java 8+ · Concurrency · JVM)

> This file takes you from "knows Java" to **interview-ready**. Every section here appears in Cognizant technical rounds.

---

# PART 1 — Exception Handling

## 1.1 The hierarchy ⭐

```
                    Throwable
                   ┌────┴─────┐
                Error       Exception
             (don't catch)  ┌────┴──────────────┐
    StackOverflowError   RuntimeException    (checked)
    OutOfMemoryError     (unchecked)         IOException
    NoClassDefFoundError │                   SQLException
                         ├ NullPointerException      FileNotFoundException
                         ├ ArithmeticException       ClassNotFoundException
                         ├ ArrayIndexOutOfBounds     InterruptedException
                         ├ ClassCastException
                         ├ NumberFormatException
                         ├ IllegalArgumentException
                         ├ IllegalStateException
                         └ ConcurrentModificationException
```

| | Checked | Unchecked (Runtime) | Error |
|---|---|---|---|
| Compiler enforces handling | ✅ must `catch` or `throws` | ❌ | ❌ |
| Represents | recoverable external conditions | programming bugs | JVM-level failure |
| Examples | `IOException`, `SQLException` | `NPE`, `IllegalArgumentException` | `OutOfMemoryError` |
| Should you catch? | yes | fix the bug instead | no |

## 1.2 try-catch-finally
```java
try {
    int r = 10 / divisor;
} catch (ArithmeticException | NullPointerException e) {   // multi-catch (Java 7)
    log.error("bad input", e);
    throw new BusinessException("Calculation failed", e);   // chaining preserves the cause
} catch (Exception e) {                                     // broader LAST — order matters
    log.error("unexpected", e);
} finally {
    // ALWAYS runs — cleanup
}
```
**Rules**
- `catch` blocks must go **most specific → most general**, else "exception has already been caught".
- `finally` runs even after `return`/`break`/`throw`. It does **not** run on `System.exit()`, a JVM crash, or an infinite loop / daemon-thread kill.
- ⚠️ Never `return` from `finally` — it swallows exceptions and overrides the try's return value.

```java
int f() {
    try { return 1; }
    finally { return 2; }      // returns 2, silently loses the 1 — NEVER do this
}
```

## 1.3 throw vs throws
```java
void withdraw(double amt) throws InsufficientFundsException {   // DECLARES (method signature)
    if (amt > balance)
        throw new InsufficientFundsException("Balance too low");  // THROWS an instance
}
```
| `throw` | `throws` |
|---|---|
| Statement inside a method | Clause in the signature |
| Throws **one** instance | Declares 1..n exception **types** |
| Followed by an object | Followed by class names |

## 1.4 try-with-resources (Java 7+)
```java
try (Connection c = ds.getConnection();
     PreparedStatement ps = c.prepareStatement(sql)) {
    ...
}   // closed in REVERSE order, even on exception
```
Requires `AutoCloseable`. If both the body and `close()` throw, the body's exception wins and the close exception is attached as **suppressed** (`e.getSuppressed()`).

## 1.5 Custom exceptions
```java
public class ResourceNotFoundException extends RuntimeException {   // unchecked
    private final String resource;
    public ResourceNotFoundException(String resource, Object id) {
        super("%s not found with id %s".formatted(resource, id));
        this.resource = resource;
    }
}
```
> **Spring convention:** make business exceptions **unchecked** (extend `RuntimeException`) — checked exceptions pollute signatures, and `@Transactional` rolls back on `RuntimeException` by default (see §Spring notes).

## 1.6 Best practices
1. Catch the **most specific** exception; never `catch (Throwable)`.
2. Never swallow: `catch (Exception e) { }` ← the worst code smell in a review.
3. Log **or** rethrow, not both (avoids duplicate stack traces).
4. Always chain the cause: `new MyEx("msg", e)`.
5. Don't use exceptions for control flow.
6. Clean up in `finally` or try-with-resources.
7. Validate early and fail fast with `IllegalArgumentException`.
8. In Spring, centralise with `@RestControllerAdvice` + `@ExceptionHandler`.

⭐ **Interview:** *"What's the difference between `final`, `finally`, `finalize`?"* → keyword (constant/no-override/no-extend) · block (always executes) · deprecated `Object` method called before GC (removed in Java 18+; use `Cleaner`/try-with-resources).

---

# PART 2 — Java 8: Lambdas, Functional Interfaces, Streams ⭐⭐

## 2.1 Lambda expressions
```java
// old
Runnable r = new Runnable(){ public void run(){ System.out.println("hi"); } };
// lambda
Runnable r = () -> System.out.println("hi");

(a, b) -> a + b
x -> x * x
(String s) -> { System.out.println(s); return s.length(); }
```
A lambda is an implementation of a **functional interface** (exactly one abstract method). It can capture **effectively final** local variables only.

## 2.2 Built-in functional interfaces (`java.util.function`) ⭐
| Interface | Method | Signature | Use |
|---|---|---|---|
| `Predicate<T>` | `test` | T → boolean | filtering |
| `BiPredicate<T,U>` | `test` | (T,U) → boolean | |
| `Function<T,R>` | `apply` | T → R | mapping |
| `BiFunction<T,U,R>` | `apply` | (T,U) → R | |
| `UnaryOperator<T>` | `apply` | T → T | |
| `BinaryOperator<T>` | `apply` | (T,T) → T | reducing |
| `Consumer<T>` | `accept` | T → void | side effects |
| `BiConsumer<T,U>` | `accept` | (T,U) → void | `map.forEach` |
| `Supplier<T>` | `get` | () → T | lazy creation |
| `Runnable` | `run` | () → void | threads |
| `Callable<V>` | `call` | () → V (throws) | executors |
| `Comparator<T>` | `compare` | (T,T) → int | sorting |
Primitive variants avoid boxing: `IntPredicate`, `ToIntFunction`, `IntSupplier`, `IntUnaryOperator`…

```java
Predicate<String> notEmpty = s -> !s.isEmpty();
Predicate<String> isLong   = s -> s.length() > 5;
notEmpty.and(isLong).test("hello!");
notEmpty.or(isLong); notEmpty.negate();

Function<Integer,Integer> dbl = x -> x*2, inc = x -> x+1;
dbl.andThen(inc).apply(5);   // 11  (double first)
dbl.compose(inc).apply(5);   // 12  (increment first)
```

## 2.3 Method references
| Kind | Syntax | Equivalent lambda |
|---|---|---|
| Static | `Integer::parseInt` | `s -> Integer.parseInt(s)` |
| Instance of a particular object | `System.out::println` | `x -> System.out.println(x)` |
| Instance of an arbitrary object | `String::toUpperCase` | `s -> s.toUpperCase()` |
| Constructor | `ArrayList::new` | `() -> new ArrayList<>()` |

## 2.4 Streams ⭐⭐

A **Stream** is a sequence of elements supporting a pipeline of aggregate operations. It is **not** a data structure, doesn't store elements, is **lazy**, and is **single-use**.

```
SOURCE ──► INTERMEDIATE (lazy, returns Stream) ──► TERMINAL (eager, triggers execution)
```

### Creation
```java
list.stream(); list.parallelStream();
Arrays.stream(arr);
Stream.of("a","b","c");
Stream.iterate(1, n -> n * 2).limit(10);
Stream.generate(Math::random).limit(5);
IntStream.range(0, 10);       // 0..9
IntStream.rangeClosed(1, 10); // 1..10
Files.lines(path);
"abc".chars();
```

### Intermediate operations
```java
.filter(p)        .map(f)          .flatMap(f)
.distinct()       .sorted()        .sorted(cmp)
.limit(n)         .skip(n)         .peek(c)       // peek = debugging only
.mapToInt(f)      .boxed()         .takeWhile(p)  .dropWhile(p)   // Java 9
```

### Terminal operations
```java
.forEach(c)  .forEachOrdered(c)
.collect(Collectors.toList())  .toList()          // Java 16, immutable
.reduce(identity, accumulator)
.count()  .min(cmp)  .max(cmp)
.anyMatch(p)  .allMatch(p)  .noneMatch(p)
.findFirst()  .findAny()
.toArray()  .sum()  .average()  .summaryStatistics()
```

### `map` vs `flatMap` ⭐
```java
// map : 1 → 1
List<Integer> lens = names.stream().map(String::length).toList();

// flatMap : 1 → many, then flattened
List<List<String>> nested = List.of(List.of("a","b"), List.of("c"));
List<String> flat = nested.stream().flatMap(List::stream).toList();  // [a,b,c]

// all words from all sentences
sentences.stream().flatMap(s -> Arrays.stream(s.split(" "))).distinct().toList();
```

### Collectors ⭐
```java
Collectors.toList() / toSet() / toCollection(TreeSet::new)
Collectors.toMap(Emp::getId, Emp::getName)
Collectors.toMap(k, v, (a,b) -> a)                 // merge fn avoids IllegalStateException on dup keys
Collectors.joining(", ", "[", "]")
Collectors.counting()
Collectors.summingDouble(Emp::getSalary)  averagingInt  summarizingInt
Collectors.groupingBy(Emp::getDept)
Collectors.groupingBy(Emp::getDept, Collectors.counting())
Collectors.groupingBy(Emp::getDept, TreeMap::new, Collectors.averagingDouble(Emp::getSalary))
Collectors.partitioningBy(e -> e.getSalary() > 50000)   // → Map<Boolean, List<Emp>>
Collectors.mapping(Emp::getName, Collectors.toList())
Collectors.teeing(...)                                  // Java 12
```

### Worked stream problems (rehearse these)
```java
// group employees by department
Map<String,List<Employee>> byDept =
    emps.stream().collect(Collectors.groupingBy(Employee::getDept));

// count per department
Map<String,Long> counts =
    emps.stream().collect(Collectors.groupingBy(Employee::getDept, Collectors.counting()));

// highest paid per department
Map<String,Optional<Employee>> top = emps.stream().collect(
    Collectors.groupingBy(Employee::getDept,
        Collectors.maxBy(Comparator.comparingDouble(Employee::getSalary))));

// second highest salary
emps.stream().map(Employee::getSalary).distinct()
    .sorted(Comparator.reverseOrder()).skip(1).findFirst();

// word frequency
Map<String,Long> freq = Arrays.stream(text.toLowerCase().split("\\W+"))
    .filter(w -> !w.isBlank())
    .collect(Collectors.groupingBy(w -> w, Collectors.counting()));

// first non-repeating char
str.chars().mapToObj(c -> (char)c)
   .collect(Collectors.groupingBy(c->c, LinkedHashMap::new, Collectors.counting()))
   .entrySet().stream().filter(e -> e.getValue()==1).map(Map.Entry::getKey).findFirst();

// sum of even squares
IntStream.rangeClosed(1,10).filter(i -> i%2==0).map(i -> i*i).sum();

// sort a map by value desc
map.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
   .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a,b)->a, LinkedHashMap::new));

// flatten and dedupe
lists.stream().flatMap(List::stream).distinct().sorted().toList();

// statistics
IntSummaryStatistics st = emps.stream().mapToInt(Employee::getAge).summaryStatistics();
st.getMax(); st.getAverage(); st.getCount();
```

### Parallel streams — use carefully
```java
list.parallelStream().filter(...).collect(...);   // uses the common ForkJoinPool
```
Good for: large datasets, CPU-bound, stateless & associative operations, splittable sources (`ArrayList`, arrays).
Bad for: small collections, I/O-bound work, stateful lambdas, ordered/`findFirst` semantics, inside a web request (it shares the common pool with the whole JVM).

## 2.5 Optional ⭐
```java
Optional<Employee> e = repo.findById(1);

e.isPresent(); e.isEmpty();
e.get();                                    // ❌ avoid — NoSuchElementException
e.orElse(defaultEmp);                       // eager default
e.orElseGet(() -> buildDefault());          // lazy — prefer for expensive defaults
e.orElseThrow(() -> new ResourceNotFoundException("Employee", 1));
e.ifPresent(x -> log.info(x.getName()));
e.ifPresentOrElse(x -> ..., () -> ...);     // Java 9
e.map(Employee::getName).filter(n -> n.startsWith("P")).orElse("Unknown");
e.flatMap(Employee::getManagerOptional);
e.stream();                                 // Java 9

Optional.of(x);            // NPE if x is null
Optional.ofNullable(x);    // empty if null
Optional.empty();
```
**Rules:** use as a **return type** for "may be absent"; do **not** use as a field, method parameter, or in an entity — and never as a collection element (return an empty collection instead).

---

# PART 3 — Multithreading & Concurrency ⭐⭐

## 3.1 Process vs Thread
- **Process** — an independent program with its own memory space.
- **Thread** — a lightweight unit of execution inside a process; threads **share heap/method area** but have their **own stack, PC and registers**.

## 3.2 Creating threads
```java
// 1. extend Thread
class MyThread extends Thread { public void run(){ ... } }
new MyThread().start();

// 2. implement Runnable  ← preferred (leaves your class free to extend something else)
class MyTask implements Runnable { public void run(){ ... } }
new Thread(new MyTask()).start();
new Thread(() -> System.out.println("lambda")).start();

// 3. Callable + ExecutorService — returns a value & can throw
ExecutorService pool = Executors.newFixedThreadPool(4);
Future<Integer> f = pool.submit(() -> 42);
Integer result = f.get();            // blocks
pool.shutdown();

// 4. Virtual threads (Java 21 LTS) — millions of cheap threads
Thread.startVirtualThread(() -> System.out.println("virtual"));
try (var ex = Executors.newVirtualThreadPerTaskExecutor()) { ex.submit(task); }
```
⭐ **`start()` vs `run()`** — `start()` asks the JVM to create a **new OS thread** and invoke `run()` on it. Calling `run()` directly just executes it on the current thread (no concurrency). Calling `start()` twice → `IllegalThreadStateException`.

## 3.3 Thread lifecycle
```
NEW ──start()──► RUNNABLE ◄──────────────┐
                   │  │                  │
       wait()/join()│  │sleep(t)/join(t)  │notify()/timeout/
       /lock miss   │  │                  │lock acquired
                    ▼  ▼                  │
              WAITING  TIMED_WAITING ─────┤
                    BLOCKED ──────────────┘
                   │
             run() completes
                   ▼
              TERMINATED
```
| State | Meaning |
|---|---|
| NEW | created, not started |
| RUNNABLE | running or ready to run |
| BLOCKED | waiting for a monitor lock |
| WAITING | `wait()`, `join()`, `park()` — indefinite |
| TIMED_WAITING | `sleep(n)`, `wait(n)`, `join(n)` |
| TERMINATED | finished or threw |

## 3.4 Thread methods
```java
t.start(); t.join(); t.join(1000); t.setDaemon(true); t.setPriority(1..10);
t.interrupt(); t.isAlive(); t.getName(); Thread.currentThread();
Thread.sleep(1000);  Thread.onSpinWait();  Thread.yield();
```
⭐ **`sleep()` vs `wait()`**
| | `Thread.sleep(ms)` | `object.wait()` |
|---|---|---|
| Class | `Thread` (static) | `Object` |
| Releases the lock? | **NO** | **YES** |
| Wakes on | timeout / interrupt | `notify()`/`notifyAll()`/timeout |
| Must hold a monitor? | no | **yes — inside `synchronized`** |

**Daemon thread** — a background thread (GC, etc.); the JVM exits when only daemons remain.

## 3.5 Race conditions & synchronization
```java
class Counter {
    private int count = 0;
    public void inc(){ count++; }   // NOT atomic: read → add → write
}
```
Two threads can interleave and lose an update.

```java
// 1. synchronized method — locks 'this'
public synchronized void inc(){ count++; }

// 2. synchronized block — narrower, better
private final Object lock = new Object();
public void inc(){ synchronized (lock) { count++; } }

// 3. static synchronized — locks the Class object
public static synchronized void m(){ }

// 4. Atomic classes — lock-free CAS, fastest for counters
private final AtomicInteger count = new AtomicInteger();
count.incrementAndGet();

// 5. explicit Lock — tryLock, timeout, fairness, interruptible
private final Lock lock = new ReentrantLock();
lock.lock();
try { count++; } finally { lock.unlock(); }     // ALWAYS unlock in finally
```

| `synchronized` | `ReentrantLock` |
|---|---|
| Keyword, implicit release | API, **must** unlock in `finally` |
| Cannot try/timeout | `tryLock()`, `tryLock(t, unit)`, `lockInterruptibly()` |
| No fairness option | `new ReentrantLock(true)` |
| One implicit condition (`wait/notify`) | multiple `Condition` objects |
| Simpler | more flexible |

**`volatile`** — guarantees **visibility** and prevents reordering (happens-before), but **not atomicity**. Use for a flag; use `Atomic*`/locks for compound actions.
```java
private volatile boolean running = true;   // ✅ another thread will see the change
private volatile int count;  count++;      // ❌ still not atomic
```

⭐ **`volatile` vs `synchronized` vs `Atomic`**: visibility only / visibility + mutual exclusion + atomicity / visibility + atomicity via CAS (no blocking).

## 3.6 Deadlock, livelock, starvation
```java
// DEADLOCK — two threads acquire locks in opposite order
Thread A: synchronized(l1){ synchronized(l2){ } }
Thread B: synchronized(l2){ synchronized(l1){ } }
```
**4 Coffman conditions:** mutual exclusion, hold-and-wait, no preemption, circular wait.
**Prevention:** consistent global lock ordering, `tryLock` with timeout, minimise lock scope, avoid nested locks.
**Detect:** `jstack <pid>`, JConsole, VisualVM.
- **Livelock** — threads keep responding to each other and make no progress.
- **Starvation** — a low-priority thread never gets CPU/lock; fix with fair locks.

## 3.7 Executor framework ⭐
```java
ExecutorService fixed  = Executors.newFixedThreadPool(10);
ExecutorService cached = Executors.newCachedThreadPool();     // unbounded — risky
ExecutorService single = Executors.newSingleThreadExecutor();
ScheduledExecutorService sch = Executors.newScheduledThreadPool(2);
sch.scheduleAtFixedRate(task, 0, 5, TimeUnit.SECONDS);

// production: configure explicitly
ThreadPoolExecutor pool = new ThreadPoolExecutor(
    5, 10, 60L, TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(100),
    new ThreadPoolExecutor.CallerRunsPolicy());

pool.shutdown();                             // no new tasks, finish existing
pool.awaitTermination(30, TimeUnit.SECONDS);
pool.shutdownNow();                          // interrupt running tasks
```
**Why pools?** Thread creation is expensive; pools bound resource use and reuse threads.

## 3.8 CompletableFuture (async composition)
```java
CompletableFuture.supplyAsync(() -> fetchUser(id))
    .thenApply(User::getName)
    .thenCompose(name -> CompletableFuture.supplyAsync(() -> lookup(name)))
    .thenCombine(otherFuture, (a,b) -> a + b)
    .exceptionally(ex -> "fallback")
    .thenAccept(System.out::println);

CompletableFuture.allOf(f1, f2, f3).join();
```

## 3.9 Concurrency utilities
| Class | Use |
|---|---|
| `CountDownLatch` | wait for N events (one-shot) |
| `CyclicBarrier` | N threads wait for each other (reusable) |
| `Semaphore` | limit concurrent access to N permits |
| `Exchanger` | swap objects between 2 threads |
| `ConcurrentHashMap` | thread-safe map |
| `CopyOnWriteArrayList` | read-mostly list |
| `BlockingQueue` | producer-consumer |
| `ForkJoinPool` | divide-and-conquer, work stealing |

## 3.10 Producer-Consumer (classic exercise)
```java
BlockingQueue<Integer> q = new LinkedBlockingQueue<>(10);
Runnable producer = () -> { for(int i=0;i<100;i++) q.put(i); };   // blocks if full
Runnable consumer = () -> { while(true) process(q.take()); };     // blocks if empty
```

## 3.11 Thread safety toolkit
1. **Immutability** — the strongest guarantee (no shared mutable state at all)
2. **Confinement** — `ThreadLocal`, stack variables
3. **Synchronization** — `synchronized`, `Lock`
4. **Atomics** — `AtomicInteger`, `AtomicReference`, `LongAdder`
5. **Concurrent collections** — `ConcurrentHashMap` etc.

---

# PART 4 — JVM Internals, Memory & GC ⭐

## 4.1 JVM architecture
```
┌───────────── Class Loader Subsystem ─────────────┐
│  Loading (Bootstrap→Platform→Application)        │
│  Linking (Verify → Prepare → Resolve)            │
│  Initialization (static blocks/fields)           │
└──────────────────────┬───────────────────────────┘
┌──────────────────────▼─── Runtime Data Areas ────┐
│  SHARED:   Heap  |  Method Area (Metaspace)      │
│  PER-THREAD: JVM Stack | PC Register | Native Stack │
└──────────────────────┬───────────────────────────┘
┌──────────────────────▼─── Execution Engine ──────┐
│  Interpreter  |  JIT (C1/C2)  |  Garbage Collector│
└──────────────────────────────────────────────────┘
              JNI ──► Native Method Libraries
```

**Class loaders — parent delegation:** a loader first asks its parent; only if the parent fails does it load the class itself. Prevents core classes being spoofed.
`Bootstrap` (java.base) → `Platform` (ext) → `Application` (classpath) → custom.

## 4.2 Memory areas ⭐
| Area | Shared? | Holds | Error on exhaustion |
|---|---|---|---|
| **Heap** | ✅ all threads | objects, arrays, String pool (since 7) | `OutOfMemoryError: Java heap space` |
| **Method Area / Metaspace** | ✅ | class metadata, static vars, constant pool | `OutOfMemoryError: Metaspace` |
| **JVM Stack** | ❌ per thread | frames: local vars, operand stack, refs | `StackOverflowError` |
| **PC Register** | ❌ | address of the current instruction | — |
| **Native Method Stack** | ❌ | JNI calls | — |

**Stack vs Heap ⭐**
| Stack | Heap |
|---|---|
| Per-thread | Shared |
| Primitives + **references** | The objects themselves |
| LIFO, auto-freed on method exit | Managed by GC |
| Fast | Slower |
| `StackOverflowError` | `OutOfMemoryError` |

> Since Java 8 **PermGen was replaced by Metaspace**, which lives in native memory and grows dynamically — the old `OutOfMemoryError: PermGen space` is gone.

## 4.3 Garbage Collection
**Generational hypothesis:** most objects die young.
```
        YOUNG GENERATION                 OLD GENERATION
 ┌──────────┬────────┬────────┐        ┌────────────────┐
 │   Eden   │  S0    │   S1   │  ───►  │    Tenured     │
 └──────────┴────────┴────────┘        └────────────────┘
   Minor GC (frequent, fast)             Major/Full GC (slow, stop-the-world)
```
1. New objects go to **Eden**.
2. Minor GC: live objects copied to a **Survivor** space; age++.
3. After `MaxTenuringThreshold` (default 15) survivals → promoted to **Old**.
4. Old gen fills → Major/Full GC.

**Reachability:** an object is garbage when it is unreachable from any **GC root** (stack locals, static fields, JNI refs, active threads).

**Collectors**
| GC | Traits | Flag |
|---|---|---|
| Serial | single-threaded, small heaps | `-XX:+UseSerialGC` |
| Parallel (Throughput) | multi-threaded, Java 8 default | `-XX:+UseParallelGC` |
| **G1** | region-based, low pause target, **default since Java 9** | `-XX:+UseG1GC` |
| ZGC / Shenandoah | sub-millisecond pauses, huge heaps | `-XX:+UseZGC` |

**Key flags:** `-Xms512m -Xmx2g -XX:MaxMetaspaceSize=256m -XX:+HeapDumpOnOutOfMemoryError -Xlog:gc*`

⭐ **Can you force GC?** `System.gc()` is only a *request*; the JVM may ignore it. Never rely on it.
⭐ **Memory leaks in Java?** Yes — objects that are reachable but no longer needed: static collections that grow forever, unclosed resources, listeners never deregistered, `ThreadLocal` in pooled threads, non-static inner classes holding outer refs, mutated `HashMap` keys.

**Reference types:** Strong (never collected while reachable) → Soft (collected under memory pressure — caches) → Weak (collected at next GC — `WeakHashMap`) → Phantom (post-mortem cleanup).

---

# PART 5 — Java Version Features (know the highlights)

| Version | Headline features |
|---|---|
| **Java 8** (2014, LTS) | **Lambdas, Streams, Functional interfaces, `Optional`, default/static interface methods, `java.time` API, Method references, Nashorn, Metaspace replaces PermGen** |
| Java 9 | Module System (JPMS), JShell, `List.of`/`Map.of`, private interface methods, `Stream.takeWhile/dropWhile`, `Optional.stream` |
| Java 10 | `var` (local type inference), `List.copyOf`, G1 parallel full GC |
| **Java 11** (LTS) | `String.isBlank/lines/strip/repeat`, `Files.readString/writeString`, `var` in lambda params, HTTP Client API, run single-file source, `Collection.toArray(IntFunction)` |
| Java 12–13 | Switch expressions (preview), text blocks (preview) |
| Java 14 | **Records** (preview), **helpful NullPointerExceptions**, switch expressions final |
| Java 15 | **Text blocks** final, sealed classes (preview) |
| Java 16 | **Records final**, **pattern matching for `instanceof`** final, `Stream.toList()` |
| **Java 17** (LTS) | **Sealed classes** final, enhanced pseudo-random generators, strong encapsulation of JDK internals — *the Spring Boot 3 baseline* |
| Java 18–20 | UTF-8 by default, simple web server, virtual threads (preview) |
| **Java 21** (LTS) | **Virtual Threads (Project Loom)**, **Pattern matching for switch**, **Record patterns**, Sequenced Collections, Structured Concurrency (preview), Generational ZGC |
| Java 22–24 | Unnamed variables `_`, stream gatherers, class-file API |
| **Java 25** (LTS, Sept 2025) | Module import declarations, compact source files & instance `main`, flexible constructor bodies, scoped values, further Loom/ZGC work. Premier support to 2030. |

⭐ **Say this in an interview:** *"The project used Java 17 with Spring Boot 3. I'm comfortable with the Java 8 functional stack — streams, lambdas, Optional — and I've read up on Java 21 virtual threads and pattern matching, plus Java 25 which is now the current LTS."*

---

## ⭐ Interview Gold — Advanced Rapid Fire

| Q | A |
|---|---|
| Checked vs unchecked exceptions? | Compiler-enforced recoverable conditions vs programming bugs extending `RuntimeException`. |
| Can `finally` be skipped? | Yes — `System.exit()`, JVM crash, infinite loop, thread kill. |
| Exception in `finally`? | It masks the try block's exception (unless via try-with-resources suppression). |
| Custom checked or unchecked? | Unchecked for business errors in Spring; checked only when the caller can genuinely recover. |
| What is a functional interface? | Exactly one abstract method; annotate `@FunctionalInterface`; the target type of a lambda. |
| Stream vs Collection? | Collection stores data; a Stream is a lazily-evaluated, single-use pipeline over a source. |
| Intermediate vs terminal? | Lazy, returns a Stream vs eager, triggers the pipeline and produces a result. |
| `map` vs `flatMap`? | 1→1 vs 1→many-then-flattened. |
| `findFirst` vs `findAny`? | Ordered first vs any element (useful in parallel). |
| Why `Optional`? | Explicit "may be absent" return type — makes null-handling part of the API, not a surprise. |
| `Runnable` vs `Callable`? | `run()` returns void and can't throw checked exceptions; `call()` returns V and can throw. |
| `start()` vs `run()`? | New thread vs plain method call on the current thread. |
| `sleep()` vs `wait()`? | Doesn't release the lock vs releases it; Thread vs Object; needs `synchronized`. |
| `wait/notify` vs `Condition`? | Object monitor (one wait set) vs `Lock`-based with multiple condition queues. |
| What is `volatile`? | Visibility + ordering guarantee; not atomicity. |
| What is a race condition? | Correctness depends on thread scheduling of unsynchronised shared state. |
| How to prevent deadlock? | Consistent lock ordering, `tryLock` with timeout, minimal lock scope, no nested locks. |
| Why `ExecutorService` over `new Thread()`? | Reuse, bounded resources, lifecycle control, `Future` results, rejection policies. |
| Thread-safe singleton? | Enum, or double-checked locking with a `volatile` instance, or a static holder class. |
| Stack vs Heap? | Per-thread frames vs shared object store; SOE vs OOM. |
| What is Metaspace? | Native-memory class metadata region replacing PermGen in Java 8. |
| How does GC decide what to collect? | Unreachability from GC roots; generational young/old collection. |
| Can Java have memory leaks? | Yes — unintentionally retained references (static caches, listeners, ThreadLocals). |
| What is JIT? | Runtime compiler that turns hot bytecode into optimised native code (C1 quick, C2 aggressive). |
| Virtual threads? | Java 21 lightweight JVM-scheduled threads — millions of concurrent blocking tasks with no pool tuning. |

---

## 📚 References
- Oracle — Exceptions: https://docs.oracle.com/javase/tutorial/essential/exceptions/
- Oracle — Concurrency: https://docs.oracle.com/javase/tutorial/essential/concurrency/
- `java.util.stream` package Javadoc (read the package summary — it's excellent): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/package-summary.html
- `java.util.function` Javadoc: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/function/package-summary.html
- JVM Specification: https://docs.oracle.com/javase/specs/jvms/se21/html/
- Oracle — HotSpot GC Tuning Guide: https://docs.oracle.com/en/java/javase/21/gctuning/
- JEP 444 Virtual Threads: https://openjdk.org/jeps/444
- Java 25 release notes: https://www.oracle.com/java/technologies/javase/25-relnote-issues.html
- Baeldung — Java Streams: https://www.baeldung.com/java-8-streams
- Baeldung — Java Concurrency: https://www.baeldung.com/java-concurrency
- *Java Concurrency in Practice*, Brian Goetz — the definitive concurrency book
