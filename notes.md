
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

