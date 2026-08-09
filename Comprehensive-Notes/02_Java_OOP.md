# 02 — Object-Oriented Programming in Java

> **The single most-asked interview area for a Java FSE fresher.** Expect 20–30 minutes on this alone.
> **References:** [Oracle OOP Concepts](https://docs.oracle.com/javase/tutorial/java/concepts/), [JLS §8 Classes](https://docs.oracle.com/javase/specs/jls/se21/html/jls-8.html)

---

## 1. Classes and Objects

- **Class** — a blueprint/template. Defines *state* (fields) and *behaviour* (methods). No memory for fields until instantiated.
- **Object** — a runtime instance of a class, living on the **heap**, with its own copy of instance fields.

```java
public class Employee {
    // ---- state ----
    private int id;
    private String name;
    private double salary;
    private static int count = 0;          // shared by ALL objects

    // ---- constructors ----
    public Employee() {                    // no-arg
        this(0, "Unknown", 0.0);           // constructor chaining — must be 1st stmt
    }
    public Employee(int id, String name, double salary) {
        this.id = id;                      // 'this' disambiguates field vs param
        this.name = name;
        this.salary = salary;
        count++;
    }

    // ---- behaviour ----
    public double annualSalary() { return salary * 12; }

    // ---- encapsulation: getters/setters ----
    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name required");
        this.name = name;
    }
    public static int getCount() { return count; }   // static method → static field only
}

Employee e = new Employee(1, "Pranav", 50000);
```

### What `new` actually does
1. Allocates memory on the heap
2. Initialises fields to defaults (`0`/`null`/`false`)
3. Runs instance initialiser blocks & field initialisers (in source order)
4. Runs the constructor body
5. Returns the reference

### Initialisation order ⭐
```java
class Parent {
    static { System.out.println("1 parent static"); }
    { System.out.println("3 parent instance"); }
    Parent() { System.out.println("4 parent ctor"); }
}
class Child extends Parent {
    static { System.out.println("2 child static"); }
    { System.out.println("5 child instance"); }
    Child() { System.out.println("6 child ctor"); }
}
// new Child()  →  1, 2, 3, 4, 5, 6
```
**Rule:** all static (parent→child, once at class load) → then for each instantiation: parent instance blocks → parent ctor → child instance blocks → child ctor.

### Constructors — facts
- Same name as the class, **no return type** (not even `void`).
- If you write **no** constructor, the compiler adds a **default no-arg** one. If you write **any** constructor, it does not.
- Can be overloaded, can be `private` (Singleton / static factory), **cannot** be `static`, `final`, `abstract`, or `synchronized`.
- **Cannot be inherited**, and cannot be overridden.
- `this(...)` calls a sibling constructor; `super(...)` calls the parent's — either must be the **first statement**, and you can't use both.
- If you write neither, the compiler inserts an implicit `super()`. If the parent has no no-arg constructor → compile error.

---

## 2. The Four Pillars ⭐⭐

### 2.1 Encapsulation — "data hiding"
Bundle data + methods, restrict direct access via `private` fields and `public` accessors.

**Benefits:** validation in setters, read-only/write-only fields, freedom to change internals, easier debugging, thread-safety hooks.

```java
public class BankAccount {
    private double balance;                       // hidden
    public double getBalance() { return balance; } // read-only from outside
    public void deposit(double amt) {
        if (amt <= 0) throw new IllegalArgumentException("amount must be > 0");
        balance += amt;                            // invariant protected
    }
}
```
> **Encapsulation vs Abstraction:** encapsulation hides *data* (implementation-level, `private`); abstraction hides *complexity/implementation* (design-level, `abstract`/`interface`). Encapsulation is **how**, abstraction is **what**.

### 2.2 Inheritance — "IS-A"
```java
class Vehicle {
    protected String brand;
    void start() { System.out.println("Vehicle starting"); }
}
class Car extends Vehicle {
    int doors;
    @Override void start() { System.out.println("Car starting"); }
    void honk() { super.start(); }   // access parent version
}
```
**Types supported in Java**
| Type | Classes | Interfaces |
|---|---|---|
| Single | ✅ | ✅ |
| Multilevel (A→B→C) | ✅ | ✅ |
| Hierarchical (A→B, A→C) | ✅ | ✅ |
| **Multiple** (B,C→A) | ❌ | ✅ (`implements X, Y`) |
| Hybrid | ❌ (via classes) | ✅ (via interfaces) |

⭐ **Why no multiple class inheritance?** The **Diamond Problem** — if `B` and `C` both extend `A` and override `m()`, which `m()` does `D extends B, C` inherit? Ambiguous state and method resolution. Interfaces avoided this originally (no state, no bodies); Java 8 default methods reintroduced ambiguity, so the compiler forces you to override and disambiguate:
```java
interface A { default void hi(){ System.out.println("A"); } }
interface B { default void hi(){ System.out.println("B"); } }
class C implements A, B {
    @Override public void hi() { A.super.hi(); }   // mandatory
}
```

**What is NOT inherited:** private members, constructors, static initialisers. (Private members *exist* in the subclass object but aren't accessible.)

**`final` class** → cannot be extended (`String`, `Integer`, `LocalDate`).
**`sealed` class** (Java 17) → restricts *who* may extend:
```java
public sealed interface Shape permits Circle, Square, Rectangle {}
public final class Circle implements Shape {}
public non-sealed class Square implements Shape {}   // reopens the hierarchy
```

### 2.3 Polymorphism — "many forms"

#### Compile-time (static) — Method **Overloading**
Same name, **different parameter list** (count, type, or order) in the same class. Return type alone is NOT enough.
```java
class Calc {
    int add(int a, int b) { return a + b; }
    double add(double a, double b) { return a + b; }
    int add(int a, int b, int c) { return a + b + c; }
    // int add(int x, int y)  ❌ duplicate
}
```
Resolution order the compiler uses: exact match → **widening** → **autoboxing** → **varargs**.
```java
void f(long x){}  void f(Integer x){}  void f(int... x){}
f(5);   // calls f(long)  — widening beats boxing beats varargs
```

#### Runtime (dynamic) — Method **Overriding**
Subclass provides a new body for an inherited instance method. Dispatch happens at **runtime** on the actual object type (dynamic method dispatch / virtual invocation).
```java
Vehicle v = new Car();   // upcasting
v.start();               // → "Car starting"  (runtime type wins)
// v.honk();             // ❌ compile error — reference type limits the API
((Car) v).honk();        // downcast (unsafe unless instanceof-checked)

if (v instanceof Car c) c.honk();   // Java 16 pattern matching for instanceof
```

**Overriding rules ⭐**
1. Same name, same parameter list.
2. Return type: same or **covariant** (a subtype).
3. Access modifier: **cannot be more restrictive** (can widen: `protected` → `public`).
4. Checked exceptions: cannot throw **new or broader** checked exceptions (can throw fewer/narrower, or any unchecked).
5. `private`, `static`, `final` methods **cannot** be overridden.
   - Redeclaring a `static` method in a subclass = **method hiding**, resolved at compile time by reference type.
6. Constructors can't be overridden.

| | Overloading | Overriding |
|---|---|---|
| Binding | Compile-time (static) | Runtime (dynamic) |
| Where | Same class (or subclass) | Superclass ↔ subclass |
| Params | Must differ | Must be identical |
| Return type | Can differ freely | Same or covariant |
| Access | Any | Can't reduce |
| `private`/`static`/`final` | Can overload | Cannot override |
| Also called | Static polymorphism | Dynamic polymorphism |

### 2.4 Abstraction — "hide the how, expose the what"

**Abstract class**
```java
abstract class Shape {
    protected String name;                     // state allowed
    Shape(String name){ this.name = name; }    // constructor allowed
    abstract double area();                    // no body — subclass must implement
    void describe(){ System.out.println(name + " area = " + area()); }  // concrete
}
class Circle extends Shape {
    private final double r;
    Circle(double r){ super("Circle"); this.r = r; }
    @Override double area(){ return Math.PI * r * r; }
}
```
- Cannot be instantiated (but **can** have a constructor, called via `super()`).
- May have 0 abstract methods; may have static/final/concrete methods, fields, constructors.
- A subclass must implement all abstract methods **or** be declared abstract itself.

**Interface**
```java
public interface Payable {
    double TAX = 0.1;                            // implicitly public static final
    double calculatePay();                       // implicitly public abstract
    default double net() { return calculatePay() * (1 - TAX); }   // Java 8
    static Payable zero() { return () -> 0; }                     // Java 8
    private double helper() { return 1; }                         // Java 9
}
```

### ⭐ Abstract class vs Interface (the #1 OOP question)

| | Abstract class | Interface |
|---|---|---|
| Keyword | `abstract class` / `extends` | `interface` / `implements` |
| Multiple inheritance | ❌ one only | ✅ many |
| Fields | any (instance, static, final) | only `public static final` constants |
| Constructors | ✅ | ❌ |
| Method bodies | ✅ concrete methods | ✅ since Java 8 (`default`, `static`), Java 9 (`private`) |
| Access modifiers on methods | any | `public` (or `private` for helpers) |
| State | ✅ can hold instance state | ❌ no instance state |
| Purpose | share **code + state** across closely related classes ("IS-A") | define a **contract/capability** across unrelated classes ("CAN-DO") |

**When to use which:** Abstract class when you have common implementation and fields for a tight family (`AbstractList`). Interface when you're defining a capability that unrelated types may adopt (`Comparable`, `Runnable`, `Serializable`), or for DI/testing seams — which is why **Spring beans are almost always programmed to interfaces**.

**Interface types**
- **Marker** — no members (`Serializable`, `Cloneable`)
- **Functional** — exactly one abstract method → lambda target (`@FunctionalInterface`)
- **Normal** — 2+ abstract methods

---

## 3. `this` and `super`

| | `this` | `super` |
|---|---|---|
| Reference | current object | parent-class part of the current object |
| Field | `this.x` disambiguates | `super.x` accesses shadowed parent field |
| Method | `this.m()` | `super.m()` calls parent's version |
| Constructor | `this(...)` chains to sibling ctor | `super(...)` calls parent ctor |
| Position | must be first statement (as ctor call) | must be first statement |
| In static context | ❌ not available | ❌ not available |

---

## 4. `Object` — the root class ⭐

Every class implicitly extends `java.lang.Object`.

| Method | Purpose |
|---|---|
| `boolean equals(Object o)` | logical equality (default: `==`) |
| `int hashCode()` | hash bucket value |
| `String toString()` | default `ClassName@hexHashCode` |
| `Class<?> getClass()` | runtime class (final) |
| `protected Object clone()` | shallow copy (requires `Cloneable`) |
| `void notify() / notifyAll() / wait()` | thread coordination (final) |
| `protected void finalize()` | deprecated, removed in Java 18+ |

### The `equals` / `hashCode` contract ⭐⭐

**Rules**
1. If `a.equals(b)` → `a.hashCode() == b.hashCode()` **(must)**
2. Equal hash codes do **not** imply equality (collisions are legal)
3. `equals` must be reflexive, symmetric, transitive, consistent, and `x.equals(null)` is `false`
4. **Always override both together**

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Employee that = (Employee) o;
    return id == that.id && Objects.equals(name, that.name);
}
@Override
public int hashCode() { return Objects.hash(id, name); }
@Override
public String toString() { return "Employee{id=" + id + ", name='" + name + "'}"; }
```

**What breaks if you don't:** put an object in a `HashSet`/`HashMap`, then look it up — you get `false`/`null` because the default `hashCode` is identity-based, so it lands in a different bucket.

> **Real trap in this program:** JPA `@Entity` classes. Never use a generated `id` in `hashCode()` (it's `null` before persist, then changes). Use a business key, or a constant `hashCode` + id-based `equals`. See [Vlad Mihalcea's guide](https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/).

---

## 5. `static` in depth

```java
class Counter {
    static int total;                        // class variable
    static final String APP = "FSE";         // constant
    static { total = 0; }                    // static block — runs once at class load
    static int getTotal(){ return total; }   // static method
    static class Nested { }                  // static nested class
}
```
**Rules**
- Static members belong to the **class**, accessed as `Counter.total`.
- A static method **cannot** use `this`, `super`, or instance members directly.
- Static methods **cannot be overridden** (only hidden).
- Static blocks run in source order at class initialisation (first active use).
- `main` is static so the JVM can call it without an object.

---

## 6. Nested & Inner classes

```java
class Outer {
    private int x = 10;

    static class StaticNested {                  // no reference to Outer instance
        void p(){ System.out.println("nested"); }
    }
    class Inner {                                 // holds implicit Outer.this
        void p(){ System.out.println(x); }
    }
    void method() {
        class Local { }                           // local class
        Runnable r = new Runnable() {             // ANONYMOUS inner class
            @Override public void run(){ System.out.println(x); }
        };
        Runnable r2 = () -> System.out.println(x); // lambda (Java 8) — preferred
    }
}
new Outer.StaticNested();
new Outer().new Inner();     // note the syntax
```
> Inner (non-static) classes keep a reference to the outer instance → a common **memory leak** source. Prefer `static` nested classes (Effective Java, Item 24).

---

## 7. Enums

```java
public enum Status {
    ACTIVE("A", 1), INACTIVE("I", 0), PENDING("P", 2);

    private final String code;
    private final int rank;
    Status(String code, int rank){ this.code = code; this.rank = rank; }  // implicitly private
    public String getCode(){ return code; }

    public static Status fromCode(String c) {
        return Arrays.stream(values())
                     .filter(s -> s.code.equals(c))
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException(c));
    }
}
```
- Enums are implicitly `final` and extend `java.lang.Enum` — so they **cannot extend** another class, but **can implement** interfaces.
- Built-in: `values()`, `valueOf(String)`, `name()`, `ordinal()`, `compareTo`.
- Enums can have **abstract methods** with per-constant bodies:
```java
enum Op {
    PLUS { int apply(int a,int b){ return a+b; } },
    TIMES{ int apply(int a,int b){ return a*b; } };
    abstract int apply(int a, int b);
}
```
- Use `EnumMap`/`EnumSet` for enum keys — far faster than `HashMap`.
- ⭐ **Best singleton implementation** (Effective Java Item 3) — serialization- and reflection-safe:
```java
public enum Registry { INSTANCE; public void doWork(){} }
```
- ⭐ **JPA tip:** always `@Enumerated(EnumType.STRING)` — `ORDINAL` breaks the moment you reorder constants.

---

## 8. Records (Java 16+) — immutable data carriers

```java
public record EmployeeDto(int id, String name, double salary) {
    // compact canonical constructor for validation
    public EmployeeDto {
        if (salary < 0) throw new IllegalArgumentException("salary < 0");
    }
    public double annual(){ return salary * 12; }
}

var d = new EmployeeDto(1, "Pranav", 50000);
d.name();                    // accessor (no "get" prefix)
d.equals(other); d.hashCode(); d.toString();   // auto-generated
```
Auto-generates: private final fields, canonical constructor, accessors, `equals`, `hashCode`, `toString`.
Records are implicitly `final`, cannot extend a class, can implement interfaces.
**Perfect for:** DTOs, API responses, value objects — replaces a lot of Lombok `@Value`.

---

## 9. Immutability — how to write an immutable class ⭐

```java
public final class ImmutablePerson {                 // 1. final class
    private final String name;                       // 2. private final fields
    private final Date birth;                        // mutable field!
    private final List<String> tags;

    public ImmutablePerson(String name, Date birth, List<String> tags) {
        this.name  = name;
        this.birth = new Date(birth.getTime());      // 3. DEFENSIVE COPY in
        this.tags  = List.copyOf(tags);
    }
    public String getName(){ return name; }
    public Date getBirth(){ return new Date(birth.getTime()); }  // 4. defensive copy out
    public List<String> getTags(){ return tags; }    // already unmodifiable
    // 5. NO setters
}
```
**Checklist:** final class · private final fields · no setters · defensive copies in *and* out for mutable members · initialise everything in the constructor.

**Benefits:** thread-safe by construction, safe as `Map` keys / `Set` elements, cacheable, no defensive-copy needed by callers.
**Examples in the JDK:** `String`, all wrappers, `BigDecimal`, `BigInteger`, `LocalDate`/`LocalDateTime`, `UUID`, `Path`.

---

## 10. Composition vs Inheritance ⭐

```java
// INHERITANCE — "IS-A", tight coupling, breaks encapsulation
class Car extends Engine { }      // ❌ a Car is not an Engine

// COMPOSITION — "HAS-A", flexible, testable
class Car {
    private final Engine engine;                   // HAS-A
    Car(Engine engine){ this.engine = engine; }    // ← this is Dependency Injection
    void start(){ engine.ignite(); }
}
```
> **"Favour composition over inheritance"** (Gang of Four / Effective Java Item 18). Inheritance couples you to the parent's implementation; a parent change can silently break subclasses (the fragile base class problem). Composition lets you swap behaviour at runtime — which is exactly the **Strategy pattern** and the basis of **Spring DI**. See `design-patterns/dependencyinjection/` and `design-patterns/strategy/` in this repo.

---

## 11. Coupling & Cohesion
- **Coupling** — degree of interdependence between modules. **Want: LOW.** Achieved with interfaces + DI.
- **Cohesion** — how focused a single module is. **Want: HIGH.** One class, one responsibility (SRP).

---

## 12. Worked example — full OOP model

```java
// abstraction
interface Payable { double calculatePay(); }

// abstract base — shared state + behaviour
abstract class Employee implements Payable, Comparable<Employee> {
    private final int id;                       // encapsulation
    private final String name;
    protected double baseSalary;

    protected Employee(int id, String name, double baseSalary) {
        this.id = id; this.name = name; this.baseSalary = baseSalary;
    }
    public int getId(){ return id; }
    public String getName(){ return name; }

    public abstract String role();              // must be implemented

    @Override public int compareTo(Employee o) {         // natural ordering
        return Double.compare(o.calculatePay(), this.calculatePay()); // desc
    }
    @Override public String toString() {
        return "%s[%d] %s earns %.2f".formatted(role(), id, name, calculatePay());
    }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee e)) return false;    // pattern matching
        return id == e.id;
    }
    @Override public int hashCode(){ return Integer.hashCode(id); }
}

// inheritance + polymorphism
class Developer extends Employee {
    private final int overtimeHours;
    Developer(int id, String n, double s, int ot){ super(id,n,s); overtimeHours = ot; }
    @Override public double calculatePay(){ return baseSalary + overtimeHours * 500; }
    @Override public String role(){ return "Developer"; }
}
class Manager extends Employee {
    private final double bonusPct;
    Manager(int id, String n, double s, double b){ super(id,n,s); bonusPct = b; }
    @Override public double calculatePay(){ return baseSalary * (1 + bonusPct); }
    @Override public String role(){ return "Manager"; }
}

// usage — runtime polymorphism
List<Employee> staff = List.of(
    new Developer(1, "Pranav", 60000, 10),
    new Manager  (2, "Asha",   80000, 0.2));

staff.stream().sorted().forEach(System.out::println);   // uses compareTo
double payroll = staff.stream().mapToDouble(Payable::calculatePay).sum();
```

---

## ⭐ Interview Gold — OOP Rapid Fire

| Q | Model answer |
|---|---|
| Four pillars? | Encapsulation, Inheritance, Polymorphism, Abstraction. |
| Abstract class vs interface? | (see table §2.4) — key: state & constructors vs multiple inheritance & contracts. |
| Can an interface have a constructor? | No — it has no state to initialise and can't be instantiated. |
| Can an abstract class have no abstract methods? | Yes — you just want to prevent instantiation. |
| Overloading vs overriding? | Compile-time vs runtime; different params vs identical params. |
| Can we override a static method? | No — it's **hidden**, resolved by reference type at compile time. |
| Can we override `private`? | No — it isn't visible to the subclass; declaring the same name creates a new method. |
| Covariant return type? | An override may return a **subtype** of the parent's return type (since Java 5). |
| Why is multiple inheritance not allowed? | Diamond problem — ambiguous state/method resolution. |
| Can a constructor be `final`/`static`/`abstract`? | No to all three. |
| What is dynamic method dispatch? | JVM picks the override based on the **actual object type** at runtime via the vtable. |
| `super()` vs `this()`? | Parent ctor vs sibling ctor; both must be the first statement; mutually exclusive. |
| Object class methods? | equals, hashCode, toString, getClass, clone, wait, notify, notifyAll, finalize. |
| `hashCode` contract? | equal objects → equal hash codes; unequal hash codes → objects are unequal. |
| Shallow vs deep copy? | Shallow copies references (shared nested objects); deep clones nested objects too. |
| How do you make a class immutable? | (see §9 checklist) |
| Composition vs inheritance? | HAS-A vs IS-A; prefer composition for flexibility & low coupling. |
| Association / Aggregation / Composition? | Association = general "uses". Aggregation = weak HAS-A, parts survive the whole (Department–Employee). Composition = strong HAS-A, parts die with the whole (House–Room). |
| What is upcasting/downcasting? | Upcast = subclass ref → parent ref (implicit, always safe). Downcast = parent → subclass (explicit, `ClassCastException` if wrong; guard with `instanceof`). |
| Marker interface? | No methods; tags a class for special JVM/framework treatment (`Serializable`). |
| Can enums extend a class? | No — they already extend `Enum`. They can implement interfaces. |
| Record vs class? | Record = immutable, auto-generated boilerplate, final, for pure data. |

---

## 📚 References
- Oracle — OOP Concepts: https://docs.oracle.com/javase/tutorial/java/concepts/
- Oracle — Interfaces & Inheritance: https://docs.oracle.com/javase/tutorial/java/IandI/
- JLS §8 (Classes): https://docs.oracle.com/javase/specs/jls/se21/html/jls-8.html
- Baeldung — equals & hashCode: https://www.baeldung.com/java-equals-hashcode-contracts
- Baeldung — Immutable Objects: https://www.baeldung.com/java-immutable-object
- Baeldung — Records: https://www.baeldung.com/java-record-keyword
- Oracle — Sealed Classes (JEP 409): https://openjdk.org/jeps/409
- *Effective Java* 3rd ed., Joshua Bloch — Items 10–18 (the definitive source)
- Refactoring Guru — Composition over Inheritance: https://refactoring.guru/design-patterns/composite
