
---

## 1. Define SPA (Single‑Page Application) & its benefits  

**SPA**:  
A web app that loads a single HTML page once and then updates the content dynamically using JavaScript, without triggering browser reloads for every navigation.

### Benefits  

| Benefit | Why it matters |
|---------|----------------|
| **Fast navigation** | No full page reload → smoother UX |
| **App‑like UI** | Instant transitions, animateable components |
| **Reduced server load** | Only static assets + REST/GraphQL calls |
| **Offline/Hybrid readiness** | Works well with service workers or mobile wrappers (Cordova/Capacitor) |
| **Easier state management** | Single source of truth in the front‑end |

---

## 2. Define React & identify its working  

**React** (by Facebook) – a *JavaScript library* for building reusable UI components.

### How it works

1. **Components** – smallest UI units (functional or class based).  
2. **Declarative render** – write *what* you want; React handles *how*.  
3. **Virtual DOM** – lightweight copy of the real DOM.  
4. **Diffing algorithm** – compares new VDOM to old → minimal patches applied to the real DOM.  
5. **One‑way data flow** – data flows from *props* (parent ➜ child) and local *state* (within component).

---

## 3. Identify the differences between SPA and MPA (Multi‑Page Application)

| Feature | SPA | MPA |
|---------|-----|-----|
| **Page loads** | One initial page; subsequent updates via JS | Full page reload on every navigation |
| **URL handling** | Uses the History API/Hash to manage routes | Traditional server‑rendered pages |
| **Performance** | Faster navigation but heavier first load | Lightweight first load, slower navigation |
| **SEO** | Requires client‑side rendering + prerendering or isSSR | Naturally SEO‑friendly |
| **State persistence** | State stays in memory (unless persisted) | Lost on page reload |
| **Complexity** | Often more JavaScript, state mgmt libraries | Simpler backend routing |
| **Use cases** | Rich interactive apps (e.g., dashboards) | Content‑heavy sites (blogs, e‑commerce catalog) |

---

## 4. Pros & Cons of Single‑Page Applications

| Pros | Cons |
|------|------|
| **Smooth UX** – no flicker / reloads | **SEO challenges** (unless CSR + prerendering or SSR) |
| **Offline support** – service workers | **Longer initial load time** (bundle size) |
| **Rich interactivity** – stateful UIs | **Complex routing** & deep linking |
| **Reduced server traffic** | **Browser memory usage** | 
| **Consistency across devices** | **SEO/analytics tagging** may need extra work |
| | **Potential for code‑bloat** if not modularized |

---

## 5. Explain about React  

- **Component‑centric**: Build UI from small, composable pieces.  
- **JSX syntax**: JavaScript + XML (e.g., `<div>Hello</div>`).  
- **Virtual DOM**: Keeps updates efficient.  
- **Hooks** (`useState`, `useEffect`, etc.) enable side‑effects and state in functional components.  
- **Unidirectional data flow**: Props immutably pass data, state encapsulates component‑specific data.  
- **Rich ecosystem**: Redux, React‑Query, React‑Router, Next.js (SSR) etc.

---

## 6. Define Virtual DOM  

The **Virtual DOM (VDOM)** is an *in‑memory* representation of the real DOM.  
When the state changes:  

1. React creates a new VDOM tree.  
2. It **diffs** this new tree against the previous tree.  
3. Generates a minimal set of DOM operations (insert, delete, modify).  
4. Applies the changes to the real DOM in one go, minimizing reflows/repaints.

Result: *faster updates* and *less janky UI*.

---

## 7. Explain Features of React  

| Feature | Description |
|---------|-------------|
| **JSX** | Write HTML‑like syntax directly in JS; transpiled to `React.createElement`. |
| **Components** | Reusable, testable UI units; can be stateful or stateless. |
| **Hooks** | `useState`, `useEffect`, custom hooks – state & side‑effect logic in functions. |
| **Virtual DOM & Diffing** | Efficient UI updates. |
| **Unidirectional Data Flow** | Data flows down (`props`); events bubble up (`callbacks`). |
| **Context API** | Pass data through component tree without prop drilling. |
| **Fiber Architecture** | Incremental rendering, better handling of abrupt UI changes. |
| **Server‑Side Rendering (Next.js, Remix, etc.)** | Pre-render pages on server for SEO & performance. |
| **Ecosystem** | Huge library of components, state managers, routing solutions. |
| **Rich Tooling** | React DevTools, profiling, hot‑module reloading. |

---

### Quick Summary (for quick reference)

- **SPA** = Single page, dynamic, fast navigation.  
- **React** = Component library + Virtual DOM → efficient UI.  
- **SPA vs MPA**: Load once vs multiple loads; client‑side vs server‑side routing.  
- **Pros/Cons**: UX vs SEO, bundle size vs load time, etc.  
- **Virtual DOM** = Diff‑and‑patch to keep updates fast.  
- **React features** = JSX, hooks, context, fiber, ecosystem.



---

# 🎨 Software Design Patterns & Principles

Design patterns are standard, reusable solutions to common software design problems. They describe templates for how to structure classes and objects to achieve clean, maintainable, and extensible code.

---

## 🏗️ 1. Creational Design Patterns

Creational patterns deal with object creation mechanisms, optimizing object creation for specific situations.

### A. Singleton Pattern
*   **Intent**: Ensures that a class has only one instance and provides a global point of access to it.
*   **Analogy**: A single office printer manager spooler or a single database connection manager.
*   **Key Components**:
    *   `Private Constructor`: Prevents external instantiation.
    *   `Static Instance Reference`: Holds the single instance.
    *   `getInstance() Method`: Controls thread-safe lazy/eager initialization.
*   **UML Structure**:
    ```text
    +--------------------------------+
    |       DatabaseConnection       |
    +--------------------------------+
    | - instance: DatabaseConnection |
    +--------------------------------+
    | - DatabaseConnection()         |
    | + getInstance(): DatabaseConnection |
    | + log(msg: String): void       |
    +--------------------------------+
    ```

### B. Factory Method Pattern
*   **Intent**: Defines an interface for creating an object, but lets subclasses decide which class to instantiate. Decouples creator from concrete products.
*   **Analogy**: A document factory where PDF factories create PDF documents and Word factories create Word documents.
*   **Key Components**:
    *   `Product Interface`: Defines the method(s) the product supports (e.g. `open()`).
    *   `Concrete Product`: Implements the interface.
    *   `Creator (Abstract Class)`: Declares the factory method.
    *   `Concrete Creators`: Override factory method to return concrete product instances.

### C. Builder Pattern
*   **Intent**: Separates the construction of a complex object from its representation, enabling step-by-step assembly of optional attributes.
*   **Analogy**: Building a custom computer (where RAM, graphics cards, and Bluetooth are optional).
*   **Key Components**:
    *   `Target Class`: The object to build (with private constructor and getters).
    *   `Static Nested Builder`: Contains fluent method setters that return the builder instance (`return this`) and a final `build()` method.

---

## 🧩 2. Structural Design Patterns

Structural patterns focus on how classes and objects are composed to form larger, more flexible structures.

### A. Adapter Pattern
*   **Intent**: Converts the interface of a class into another interface clients expect, enabling incompatible interfaces to collaborate.
*   **Analogy**: A USB-C to USB-A dongle.
*   **Key Components**:
    *   `Target Interface`: The modern interface the client code uses.
    *   `Adaptee (Legacy Class)`: The class with the incompatible interface.
    *   `Adapter`: Implements the target interface and translates calls to the adaptee.

### B. Decorator Pattern
*   **Intent**: Dynamically adds new behaviors or responsibilities to an object without altering its original structure.
*   **Analogy**: Adding toppings to a pizza or chaining notification wrappers (Email -> SMS -> Slack).
*   **Key Components**:
    *   `Component Interface`: Base interface for both wrappers and original target.
    *   `Concrete Component`: Base implementation.
    *   `Decorator Class`: Implements component interface, holding a reference to a wrapped component.
    *   `Concrete Decorators`: Extends the decorator to append extra behaviors.

### C. Proxy Pattern
*   **Intent**: Provides a surrogate or placeholder for another object to control access, perform lazy initialization, or cache requests.
*   **Analogy**: A bank card acting as a proxy for access to a physical bank account.
*   **Key Components**:
    *   `Subject Interface`: Shared interface for both Real Subject and Proxy.
    *   `Real Subject`: The resource-heavy object.
    *   `Proxy`: Intercepts calls, controls Real Subject instantiation, and lazy loads it when required.

---

## ⚙️ 3. Behavioral Design Patterns

Behavioral patterns manage algorithms, communication, and assignment of responsibilities between objects.

### A. Observer Pattern
*   **Intent**: Defines a one-to-many dependency between objects so that when one object changes state, all its dependents are notified automatically.
*   **Analogy**: A newsletter subscription or stock market ticker notifying apps and web portals of changes.
*   **Key Components**:
    *   `Subject Interface`: Standard methods to add, remove, and notify observers.
    *   `Concrete Subject`: The state owner that notifies observers.
    *   `Observer Interface`: Interface defining the update protocol.
    *   `Concrete Observers`: React to notifications from the subject.

### B. Strategy Pattern
*   **Intent**: Defines a family of algorithms, encapsulates each one, and makes them interchangeable at runtime.
*   **Analogy**: Choosing different routes (driving, walking, transit) on a maps app or choosing different payment options (Credit Card, PayPal) at checkout.
*   **Key Components**:
    *   `Strategy Interface`: Common interface for all algorithms.
    *   `Concrete Strategies`: Implement individual algorithms.
    *   `Context`: References a strategy object and executes it.

### C. Command Pattern
*   **Intent**: Encapsulates a request as an object, allowing parameterization of clients with different requests, request queuing, and undo operations.
*   **Analogy**: A smart home remote button which can trigger different command objects (e.g. LightOnCommand, LightOffCommand).
*   **Key Components**:
    *   `Command Interface`: Defines the `execute()` action.
    *   `Concrete Command`: Binds a receiver action to `execute()`.
    *   `Receiver`: Performs the actual operation (e.g. `Light`).
    *   `Invoker`: Triggers the command (e.g. `RemoteControl`).

---

## 🏛️ 4. Architectural & Dependency Patterns

### A. MVC (Model-View-Controller)
*   **Intent**: Decouples application logic, user interface formatting, and input processing into three separate layers.
*   **Layers**:
    *   `Model`: Represents data and business logic.
    *   `View`: Displays representation of data.
    *   `Controller`: Listens to user inputs, alters model data, and triggers view updates.

### B. Dependency Injection (DI)
*   **Intent**: Promotes loose coupling by passing dependencies into a client class rather than letting the client create them.
*   **Implementation Types**:
    *   `Constructor Injection`: Passing dependencies as constructor parameters (highly recommended).
    *   `Setter Injection`: Injecting dependencies via setter methods.
    *   `Interface Injection`: Declaring injection method on interface.
