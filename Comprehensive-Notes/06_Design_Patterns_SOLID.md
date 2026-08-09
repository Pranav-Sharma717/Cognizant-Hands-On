# 06 — Design Patterns & SOLID Principles

> Maps to the [`design-patterns/`](../design-patterns/) Maven project in this repo — all 12 patterns below have working code there.
> **References:** [Refactoring Guru](https://refactoring.guru/design-patterns), [java-design-patterns.com](https://java-design-patterns.com/), *Design Patterns: Elements of Reusable Object-Oriented Software* (Gang of Four, 1994)

---

## 1. What is a design pattern?

A **reusable, named solution to a recurring design problem** — not code you copy, but a template you adapt. Benefits: shared vocabulary ("just use a Strategy here"), proven structure, easier maintenance and extension.

**GoF classification**
| Category | Concern | Patterns in this repo |
|---|---|---|
| **Creational** | *how objects are created* | Singleton, Factory Method, Builder |
| **Structural** | *how objects are composed* | Adapter, Decorator, Proxy |
| **Behavioural** | *how objects interact & distribute responsibility* | Observer, Strategy, Command |
| **Architectural / DI** | *application structure* | MVC, Dependency Injection |

---

# PART 1 — CREATIONAL PATTERNS

## 1.1 Singleton — one instance, globally accessible
📁 `design-patterns/singleton/DatabaseConnection.java`

**Intent:** ensure a class has exactly one instance and provide a global access point.
**Use when:** a single shared resource — DB connection pool, config, logger, cache, thread pool.

```java
// ❌ NOT thread-safe (lazy, naive)
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private DatabaseConnection() { }                       // private ctor blocks 'new'
    public static DatabaseConnection getInstance() {
        if (instance == null) instance = new DatabaseConnection();  // race condition!
        return instance;
    }
}

// ✅ Eager — simple, thread-safe, but created even if never used
public class Eager {
    private static final Eager INSTANCE = new Eager();
    private Eager() { }
    public static Eager getInstance() { return INSTANCE; }
}

// ✅ Double-Checked Locking — lazy + thread-safe. 'volatile' is MANDATORY.
public class DCL {
    private static volatile DCL instance;
    private DCL() { }
    public static DCL getInstance() {
        if (instance == null) {                            // 1st check — no lock
            synchronized (DCL.class) {
                if (instance == null)                      // 2nd check — with lock
                    instance = new DCL();
            }
        }
        return instance;
    }
}

// ✅✅ Bill Pugh static holder — lazy, thread-safe, no synchronization cost
public class Holder {
    private Holder() { }
    private static class H { static final Holder INSTANCE = new Holder(); }
    public static Holder getInstance() { return H.INSTANCE; }
}

// ✅✅✅ Enum — the best (Effective Java Item 3): serialization- & reflection-safe
public enum EnumSingleton {
    INSTANCE;
    public void query(String sql) { }
}
```

⭐ **Why must `instance` be `volatile` in DCL?** Without it, the JVM may reorder `instance = new DCL()` (allocate → assign reference → run constructor), so another thread could see a **non-null but partially constructed** object. `volatile` establishes the happens-before relationship and forbids that reordering.

**Breaking a singleton:** reflection (`setAccessible(true)` on the private ctor), serialization (each `readObject` makes a new instance — fix with `readResolve()`), cloning, multiple classloaders. **Enum defeats all of these.**

**Criticisms:** global mutable state, hard to unit-test (can't substitute a mock), hides dependencies. **In Spring, don't hand-roll this** — `@Component`/`@Service` beans are singleton-scoped *per container*, which is testable and injectable.

---

## 1.2 Factory Method — delegate instantiation to subclasses
📁 `design-patterns/factorymethod/` — `Document`, `PdfDocument`, `WordDocument`, `DocumentFactory`, `PdfDocumentFactory`, `WordDocumentFactory`

**Intent:** define an interface for creating an object, but let subclasses decide which class to instantiate.
**Use when:** the exact type isn't known until runtime, or you want to decouple creation from use.

```java
interface Document { void open(); }
class PdfDocument  implements Document { public void open(){ System.out.println("Opening PDF");  } }
class WordDocument implements Document { public void open(){ System.out.println("Opening Word"); } }

abstract class DocumentFactory {
    public abstract Document createDocument();          // the FACTORY METHOD
    public Document newOpenedDocument() {               // template using it
        Document d = createDocument(); d.open(); return d;
    }
}
class PdfDocumentFactory  extends DocumentFactory { public Document createDocument(){ return new PdfDocument();  } }
class WordDocumentFactory extends DocumentFactory { public Document createDocument(){ return new WordDocument(); } }

// client depends only on the abstractions
DocumentFactory f = new PdfDocumentFactory();
f.newOpenedDocument();
```

| Variant | Difference |
|---|---|
| **Simple Factory** | one static method with a `switch` — not a real GoF pattern, but common |
| **Factory Method** | one product; subclasses override the creator |
| **Abstract Factory** | a *family* of related products (`GuiFactory` → `Button` + `Checkbox`) |

**In the JDK/Spring:** `Calendar.getInstance()`, `NumberFormat.getInstance()`, `DocumentBuilderFactory`, `BeanFactory`, `SqlSessionFactory`.
⭐ Adding a new document type requires **no change** to existing code → **Open/Closed Principle**.

---

## 1.3 Builder — construct complex objects step by step
📁 `design-patterns/builder/Computer.java`

**Intent:** separate the construction of a complex object from its representation.
**Use when:** many optional parameters (avoids the "telescoping constructor" anti-pattern), or you want immutability.

```java
public class Computer {
    private final String cpu, ram;          // required
    private final String storage, gpu;      // optional
    private final boolean bluetooth;

    private Computer(Builder b) {           // private ctor — only the builder can call it
        this.cpu = b.cpu; this.ram = b.ram;
        this.storage = b.storage; this.gpu = b.gpu; this.bluetooth = b.bluetooth;
    }

    public static class Builder {
        private final String cpu, ram;      // required → in the builder's constructor
        private String storage = "256GB SSD";
        private String gpu = "Integrated";
        private boolean bluetooth;

        public Builder(String cpu, String ram) { this.cpu = cpu; this.ram = ram; }

        public Builder storage(String s)  { this.storage = s; return this; }   // fluent
        public Builder gpu(String g)      { this.gpu = g;     return this; }
        public Builder bluetooth(boolean b){ this.bluetooth = b; return this; }

        public Computer build() {
            if (cpu == null) throw new IllegalStateException("CPU required");   // validate here
            return new Computer(this);
        }
    }
}

Computer pc = new Computer.Builder("i9", "32GB")
                    .storage("1TB NVMe")
                    .gpu("RTX 4080")
                    .bluetooth(true)
                    .build();
```

**Benefits:** readable named parameters, immutable result, validation in one place, no constructor explosion.
**In the JDK/libraries:** `StringBuilder`, `Stream.Builder`, `HttpRequest.newBuilder()`, `Calendar.Builder`, Lombok `@Builder`, `UriComponentsBuilder`, `MockMvcRequestBuilders`.

---

# PART 2 — STRUCTURAL PATTERNS

## 2.1 Adapter — make incompatible interfaces work together
📁 `design-patterns/adapter/` — `PaymentProcessor`, `StripePaymentProcessor`, `LegacyPayPalGateway`, `PayPalAdapter`

**Intent:** convert a class's interface into another interface clients expect. Also called **Wrapper**.
**Use when:** integrating a legacy system or third-party library whose API doesn't match yours.

```java
// Target — what our application expects
interface PaymentProcessor { void processPayment(double amount); }

// Existing implementation that already fits
class StripePaymentProcessor implements PaymentProcessor {
    public void processPayment(double amount){ System.out.println("Stripe: " + amount); }
}

// Adaptee — legacy, incompatible signature, we cannot modify it
class LegacyPayPalGateway {
    public void makePayment(String amountInPaise){ System.out.println("PayPal: " + amountInPaise); }
}

// Adapter — implements Target, DELEGATES to Adaptee (object adapter / composition)
class PayPalAdapter implements PaymentProcessor {
    private final LegacyPayPalGateway paypal;
    PayPalAdapter(LegacyPayPalGateway paypal){ this.paypal = paypal; }
    @Override public void processPayment(double amount) {
        paypal.makePayment(String.valueOf((long)(amount * 100)));   // TRANSLATE
    }
}

List<PaymentProcessor> gateways = List.of(
    new StripePaymentProcessor(),
    new PayPalAdapter(new LegacyPayPalGateway()));
gateways.forEach(g -> g.processPayment(499.99));   // uniform client code ✅
```
**Object adapter (composition, shown above) vs class adapter (inheritance)** — prefer composition; Java's single inheritance limits the class variant anyway.
**In the JDK:** `Arrays.asList()`, `InputStreamReader` (bytes→chars), `Collections.list(Enumeration)`, Spring's `HandlerAdapter`, `MessageConverter`.

---

## 2.2 Decorator — add behaviour dynamically
📁 `design-patterns/decorator/` — `Notifier`, `EmailNotifier`, `NotifierDecorator`, `SMSNotifierDecorator`, `SlackNotifierDecorator`

**Intent:** attach additional responsibilities to an object **at runtime**, without subclass explosion.
**Use when:** you'd otherwise need `EmailSmsNotifier`, `EmailSlackNotifier`, `EmailSmsSlackNotifier`… (2ⁿ classes).

```java
interface Notifier { void send(String message); }

class EmailNotifier implements Notifier {                      // concrete component
    public void send(String m){ System.out.println("Email: " + m); }
}

abstract class NotifierDecorator implements Notifier {          // base decorator
    protected final Notifier wrapped;                           // HAS-A the same interface
    protected NotifierDecorator(Notifier wrapped){ this.wrapped = wrapped; }
    @Override public void send(String m){ wrapped.send(m); }    // delegate
}

class SMSNotifierDecorator extends NotifierDecorator {
    SMSNotifierDecorator(Notifier n){ super(n); }
    @Override public void send(String m){ super.send(m); System.out.println("SMS: " + m); }
}
class SlackNotifierDecorator extends NotifierDecorator {
    SlackNotifierDecorator(Notifier n){ super(n); }
    @Override public void send(String m){ super.send(m); System.out.println("Slack: " + m); }
}

// stack them at RUNTIME, in any order/combination
Notifier n = new SlackNotifierDecorator(new SMSNotifierDecorator(new EmailNotifier()));
n.send("Server down!");    // → Email, SMS, Slack
```

⭐ **Decorator vs Inheritance:** inheritance is static and compile-time; a decorator composes at runtime and can be applied any number of times in any order.
⭐ **Decorator vs Proxy:** *identical structure, different intent* — Decorator **adds behaviour**, Proxy **controls access**.
**In the JDK:** the whole `java.io` package — `new BufferedReader(new InputStreamReader(new FileInputStream(f)))`. Also `Collections.unmodifiableList()`, `HttpServletRequestWrapper`.

---

## 2.3 Proxy — control access to an object
📁 `design-patterns/proxy/` — `Image`, `RealImage`, `ProxyImage`

**Intent:** provide a placeholder/surrogate that controls access to the real object.

```java
interface Image { void display(); }

class RealImage implements Image {                       // expensive to create
    private final String filename;
    RealImage(String f){ this.filename = f; loadFromDisk(); }
    private void loadFromDisk(){ System.out.println("Loading " + filename); }
    public void display(){ System.out.println("Displaying " + filename); }
}

class ProxyImage implements Image {                      // VIRTUAL proxy → lazy loading
    private final String filename;
    private RealImage realImage;
    ProxyImage(String f){ this.filename = f; }
    public void display() {
        if (realImage == null) realImage = new RealImage(filename);  // created on FIRST use
        realImage.display();
    }
}

Image img = new ProxyImage("photo.jpg");   // no disk I/O yet
img.display();                              // loads + displays
img.display();                              // cached — displays only
```

**Proxy types**
| Type | Purpose |
|---|---|
| **Virtual** | lazy-load expensive objects (above; also Hibernate lazy loading) |
| **Protection** | access control / authorisation (Spring Security `@PreAuthorize`) |
| **Remote** | represent an object in another JVM (RMI, Feign clients) |
| **Caching** | memoize results (`@Cacheable`) |
| **Logging/Smart** | add cross-cutting behaviour (Spring AOP) |

⭐ **This is the single most important pattern for Spring:** `@Transactional`, `@Cacheable`, `@Async` and all AOP advice work by wrapping your bean in a **JDK dynamic proxy** (if it implements an interface) or a **CGLIB subclass proxy**. That's also *why* calling an `@Transactional` method from another method **in the same class** doesn't start a transaction — the call bypasses the proxy.

---

# PART 3 — BEHAVIOURAL PATTERNS

## 3.1 Observer — publish/subscribe
📁 `design-patterns/observer/` — `Subject`, `Observer`, `StockMarket`, `MobileAppObserver`, `WebPortalObserver`

**Intent:** define a one-to-many dependency so that when one object changes state, all its dependents are notified automatically.

```java
interface Observer { void update(String stock, double price); }
interface Subject {
    void register(Observer o);
    void deregister(Observer o);
    void notifyObservers();
}

class StockMarket implements Subject {                       // concrete subject
    private final List<Observer> observers = new ArrayList<>();
    private String stock; private double price;

    public void register(Observer o){ observers.add(o); }
    public void deregister(Observer o){ observers.remove(o); }
    public void notifyObservers(){ observers.forEach(o -> o.update(stock, price)); }

    public void setPrice(String stock, double price) {       // state change
        this.stock = stock; this.price = price;
        notifyObservers();                                   // → push to everyone
    }
}

class MobileAppObserver implements Observer {
    public void update(String s, double p){ System.out.println("📱 " + s + " = " + p); }
}
class WebPortalObserver implements Observer {
    public void update(String s, double p){ System.out.println("🌐 " + s + " = " + p); }
}

StockMarket market = new StockMarket();
market.register(new MobileAppObserver());
market.register(new WebPortalObserver());
market.setPrice("INFY", 1523.40);        // both notified — subject knows only the interface
```

**Push vs pull:** push sends the data in `update()` (as above); pull passes the subject so observers query what they need.
**Pitfalls:** notification order isn't guaranteed; a forgotten `deregister` leaks memory (the *lapsed listener* problem); exceptions in one observer can break the chain; beware infinite update loops.
**In Java/Spring:** `java.util.Observer` (deprecated since Java 9), Swing `ActionListener`, `PropertyChangeListener`, **Spring `ApplicationEvent` + `@EventListener`**, Reactive Streams `Publisher`/`Subscriber`, message brokers (Kafka topics).

---

## 3.2 Strategy — swap algorithms at runtime
📁 `design-patterns/strategy/` — `PaymentStrategy`, `CreditCardPayment`, `PayPalPayment`, `PaymentContext`

**Intent:** define a family of interchangeable algorithms, encapsulate each one, and make them swappable.
**Use when:** you have a `switch`/`if-else` chain selecting behaviour → replace it with polymorphism.

```java
interface PaymentStrategy { void pay(double amount); }

class CreditCardPayment implements PaymentStrategy {
    private final String cardNumber;
    CreditCardPayment(String c){ this.cardNumber = c; }
    public void pay(double a){ System.out.println("Paid " + a + " by card " + mask(cardNumber)); }
}
class PayPalPayment implements PaymentStrategy {
    private final String email;
    PayPalPayment(String e){ this.email = e; }
    public void pay(double a){ System.out.println("Paid " + a + " via PayPal " + email); }
}

class PaymentContext {                                     // the CONTEXT
    private PaymentStrategy strategy;
    public void setStrategy(PaymentStrategy s){ this.strategy = s; }   // swap at runtime
    public void checkout(double amount) {
        if (strategy == null) throw new IllegalStateException("no payment method");
        strategy.pay(amount);
    }
}

PaymentContext ctx = new PaymentContext();
ctx.setStrategy(new CreditCardPayment("4111111111111111"));
ctx.checkout(999.00);
ctx.setStrategy(new PayPalPayment("user@mail.com"));       // switched without touching context
ctx.checkout(499.00);
```
With Java 8 a strategy is often just a lambda: `ctx.setStrategy(a -> System.out.println("UPI " + a));`

⭐ **Strategy vs State:** identical structure. Strategy = the *client* chooses the algorithm; State = the object changes its own behaviour as its internal state transitions.
⭐ **Strategy vs Factory:** Factory decides *which object to create*; Strategy decides *which algorithm to run*.
**In Java/Spring:** `Comparator`, `ThreadPoolExecutor` rejection policies, `Collections.sort(list, cmp)`, Spring's `Resource` loaders, `PasswordEncoder` implementations.
**Spring bonus:** inject `Map<String, PaymentStrategy>` and Spring populates it with bean-name → strategy — a clean runtime lookup with zero `switch`.

---

## 3.3 Command — encapsulate a request as an object
📁 `design-patterns/command/` — `Command`, `Light`, `LightOnCommand`, `LightOffCommand`, `RemoteControl`

**Intent:** turn a request into a standalone object, enabling queueing, logging, and **undo**.

```java
interface Command { void execute(); default void undo(){} }

class Light {                                            // RECEIVER — does the real work
    public void on(){ System.out.println("Light ON"); }
    public void off(){ System.out.println("Light OFF"); }
}

class LightOnCommand implements Command {                // CONCRETE COMMAND
    private final Light light;
    LightOnCommand(Light l){ this.light = l; }
    public void execute(){ light.on(); }
    public void undo(){ light.off(); }
}
class LightOffCommand implements Command {
    private final Light light;
    LightOffCommand(Light l){ this.light = l; }
    public void execute(){ light.off(); }
    public void undo(){ light.on(); }
}

class RemoteControl {                                    // INVOKER — knows nothing about Light
    private final Deque<Command> history = new ArrayDeque<>();
    public void press(Command c){ c.execute(); history.push(c); }
    public void undoLast(){ if(!history.isEmpty()) history.pop().undo(); }
}

Light light = new Light();
RemoteControl remote = new RemoteControl();
remote.press(new LightOnCommand(light));
remote.press(new LightOffCommand(light));
remote.undoLast();                                       // → Light ON
```
**Roles:** Command (interface) · ConcreteCommand · Receiver (`Light`) · Invoker (`RemoteControl`) · Client (wires them).
**Uses:** undo/redo, macro/batch commands, task queues, transactions, GUI buttons, job scheduling.
**In Java:** `Runnable`/`Callable` submitted to an `ExecutorService` **is** the Command pattern.

---

# PART 4 — ARCHITECTURAL PATTERNS

## 4.1 MVC — Model-View-Controller
📁 `design-patterns/mvc/` — `Student`, `StudentView`, `StudentController`

**Intent:** separate data, presentation, and control flow.

```java
class Student {                                    // MODEL — data + business rules
    private String name, rollNo;
    // getters/setters
}
class StudentView {                                // VIEW — presentation only
    void printStudentDetails(String name, String rollNo) {
        System.out.println("Student: " + name + " (" + rollNo + ")");
    }
}
class StudentController {                          // CONTROLLER — mediates
    private final Student model; private final StudentView view;
    StudentController(Student m, StudentView v){ model = m; view = v; }
    void setStudentName(String n){ model.setName(n); }
    void updateView(){ view.printStudentDetails(model.getName(), model.getRollNo()); }
}
```
```
User ──action──► Controller ──updates──► Model
                     │                     │
                     └──selects──► View ◄──reads state
```

**In Spring Boot** (the shape you'll actually build — see [12_Spring_Boot_REST_Security.md](12_Spring_Boot_REST_Security.md)):
```
@RestController  →  @Service  →  @Repository  →  @Entity
  (Controller)      (business)     (data access)   (Model)
        │
        └─ View = the JSON response / React SPA
```
Benefits: separation of concerns, parallel development, independently testable layers, swappable views (JSON, HTML, mobile).
**Related:** MVP (Presenter holds all view logic), MVVM (data binding — Angular/Vue), Flux/Redux (React).

---

## 4.2 Dependency Injection & IoC
📁 `design-patterns/dependencyinjection/` — `Engine`, `V8Engine`, `ElectricEngine`, `Car`

**Intent:** a class receives its dependencies from outside instead of creating them — **Inversion of Control**.

```java
interface Engine { void start(); }
class V8Engine       implements Engine { public void start(){ System.out.println("V8 roaring"); } }
class ElectricEngine implements Engine { public void start(){ System.out.println("Silent EV"); } }

// ❌ TIGHT COUPLING — Car controls creation, can't be tested or reconfigured
class BadCar {
    private final Engine engine = new V8Engine();   // hard-wired
}

// ✅ CONSTRUCTOR INJECTION — dependency handed in
class Car {
    private final Engine engine;                    // depends on the ABSTRACTION
    Car(Engine engine){ this.engine = engine; }     // injected
    void drive(){ engine.start(); }
}

new Car(new V8Engine()).drive();
new Car(new ElectricEngine()).drive();              // swapped, zero changes to Car
new Car(mock(Engine.class)).drive();                // trivially testable
```

**Three injection types**
| Type | Example | Verdict |
|---|---|---|
| **Constructor** | `Car(Engine e)` | ✅ **recommended** — enforces required deps, supports `final`, immutable, testable, fails fast |
| Setter | `setEngine(Engine e)` | for optional/reconfigurable deps |
| Field | `@Autowired private Engine e;` | ❌ discouraged — hides deps, no `final`, can't construct in a plain unit test |

**Same idea in Spring:**
```java
@Service
public class CarService {
    private final Engine engine;
    public CarService(Engine engine) { this.engine = engine; }   // @Autowired optional since 4.3
}
```
DI is the practical realisation of the **Dependency Inversion Principle** (the D in SOLID) and is the entire foundation of the Spring Framework. See [10_Spring_Core_Maven_AOP.md](10_Spring_Core_Maven_AOP.md).

---

# PART 5 — SOLID PRINCIPLES ⭐⭐

Coined by Robert C. Martin ("Uncle Bob"). Expect to be asked to name all five *and* give an example of one.

### S — Single Responsibility Principle
> *A class should have only one reason to change.*
```java
// ❌ three responsibilities: business logic, persistence, notification
class Employee {
    double calculatePay(){ }
    void saveToDatabase(){ }
    void sendEmail(){ }
}
// ✅ split
class Employee { double calculatePay(){ } }
class EmployeeRepository { void save(Employee e){ } }
class EmailService { void send(Employee e){ } }
```
This is exactly why Spring separates Controller / Service / Repository.

### O — Open/Closed Principle
> *Open for extension, closed for modification.*
```java
// ❌ every new type edits this method
double area(Shape s){
    if (s instanceof Circle) ... else if (s instanceof Square) ...
}
// ✅ new shapes just implement the interface — no existing code changes
interface Shape { double area(); }
class Circle implements Shape { public double area(){ return Math.PI*r*r; } }
```
Achieved via polymorphism, Strategy, Factory Method, Decorator.

### L — Liskov Substitution Principle
> *Subtypes must be substitutable for their base types without breaking correctness.*
```java
// ❌ classic violation
class Rectangle { void setWidth(int w); void setHeight(int h); }
class Square extends Rectangle {              // a Square isn't a substitutable Rectangle
    void setWidth(int w){ this.w = this.h = w; }   // breaks setW(5);setH(4) → expect area 20
}
// ❌ another violation
class Ostrich extends Bird { void fly(){ throw new UnsupportedOperationException(); } }
```
**Rules:** don't strengthen preconditions, don't weaken postconditions, don't throw new exception types, preserve invariants.

### I — Interface Segregation Principle
> *No client should be forced to depend on methods it does not use.*
```java
// ❌ fat interface
interface Worker { void work(); void eat(); void attendMeeting(); }
class Robot implements Worker { public void eat(){ throw new UnsupportedOperationException(); } }
// ✅ segregate
interface Workable { void work(); }
interface Eatable  { void eat(); }
class Robot implements Workable { }
class Human implements Workable, Eatable { }
```

### D — Dependency Inversion Principle
> *High-level modules should not depend on low-level modules. Both should depend on abstractions.*
```java
// ❌
class OrderService { private final MySqlOrderRepository repo = new MySqlOrderRepository(); }
// ✅
class OrderService {
    private final OrderRepository repo;                 // interface
    OrderService(OrderRepository repo){ this.repo = repo; }
}
```
> **DI vs DIP:** DIP is the *principle* (depend on abstractions); DI is the *technique* (hand the dependency in); IoC is the *container* doing it for you.

---

## 6. Other principles worth naming
| Principle | Meaning |
|---|---|
| **DRY** | Don't Repeat Yourself — one authoritative representation of each piece of knowledge |
| **KISS** | Keep It Simple, Stupid |
| **YAGNI** | You Aren't Gonna Need It — don't build speculative features |
| **Composition over Inheritance** | prefer HAS-A for flexibility |
| **Program to an interface, not an implementation** | the GoF's first principle |
| **Law of Demeter** | talk only to immediate friends: avoid `a.getB().getC().doIt()` |
| **Separation of Concerns** | each module addresses one concern |
| **Fail Fast** | validate early, throw immediately |
| **GRASP** | Information Expert, Creator, Controller, Low Coupling, High Cohesion, Polymorphism, Pure Fabrication, Indirection, Protected Variations |

## 7. Anti-patterns to be able to name
| Anti-pattern | Symptom |
|---|---|
| God Object / Blob | one class does everything |
| Spaghetti Code | no structure, tangled control flow |
| Golden Hammer | one tool applied to every problem |
| Magic Numbers | unexplained literals — use named constants |
| Copy-Paste Programming | violates DRY |
| Premature Optimization | complexity with no measured benefit |
| Anemic Domain Model | entities with only getters/setters, all logic in services (common in Spring — debatable) |
| Singleton overuse | global mutable state, untestable |
| Circular Dependency | A→B→A (Spring throws `BeanCurrentlyInCreationException`) |

---

## ⭐ Interview Gold — Patterns Rapid Fire

| Q | A |
|---|---|
| What is a design pattern? | A named, reusable solution template for a recurring design problem. |
| Three GoF categories? | Creational, Structural, Behavioural. |
| How do you make a Singleton thread-safe? | Eager init, double-checked locking with `volatile`, Bill Pugh static holder, or **enum** (best). |
| Why `volatile` in DCL? | Prevents instruction reordering exposing a partially-constructed object. |
| How can a Singleton be broken? | Reflection, serialization, cloning, multiple classloaders — enum resists all. |
| Factory vs Abstract Factory? | One product vs a family of related products. |
| When would you use Builder? | Many optional params + immutability; avoids telescoping constructors. |
| Adapter vs Decorator vs Proxy? | Convert interface / add behaviour / control access — Decorator and Proxy share structure but differ in intent. |
| Where is Decorator used in the JDK? | `java.io` — `new BufferedReader(new FileReader(f))`. |
| Where is Proxy used in Spring? | AOP: `@Transactional`, `@Cacheable`, `@Async`, Security — JDK dynamic proxy or CGLIB. |
| Why doesn't `@Transactional` work on a self-invoked method? | The internal call bypasses the proxy, so no advice runs. |
| Observer real-world example? | Stock ticker, Spring `ApplicationEvent`, Kafka topics, UI listeners. |
| Strategy vs State? | Client selects the algorithm vs the object transitions its own behaviour. |
| How would you remove a big `switch` statement? | Strategy (+ a Map of beans) or Factory Method + polymorphism. |
| Which pattern is `Runnable` + `ExecutorService`? | Command. |
| Name the SOLID principles. | SRP, OCP, LSP, ISP, DIP (with one example each — see §5). |
| Give an LSP violation. | `Square extends Rectangle`; `Ostrich extends Bird` throwing on `fly()`. |
| DI vs IoC vs DIP? | Technique / container-driven control flow / principle. |
| Which injection type is best and why? | Constructor — required deps enforced, `final` fields, immutable, unit-testable without a container. |
| What patterns does Spring itself use? | Singleton (beans), Factory (`BeanFactory`), Proxy (AOP), Template Method (`JdbcTemplate`, `RestTemplate`), Observer (events), Front Controller (`DispatcherServlet`), MVC, Builder, Strategy. |

---

## 📚 References
- Refactoring Guru — Design Patterns: https://refactoring.guru/design-patterns
- Java Design Patterns (iluwatar, open source): https://java-design-patterns.com/
- Baeldung — Design Patterns series: https://www.baeldung.com/design-patterns-series
- SOLID (Uncle Bob's original articles): https://blog.cleancoder.com/uncle-bob/2020/10/18/Solid-Relevance.html
- Martin Fowler — IoC & DI: https://martinfowler.com/articles/injection.html
- Spring Framework Reference (patterns in practice): https://docs.spring.io/spring-framework/reference/
- *Design Patterns* — Gamma, Helm, Johnson, Vlissides (GoF)
- *Head First Design Patterns* — the friendliest introduction
- *Clean Code* / *Clean Architecture* — Robert C. Martin
