# 14 — React JS (Frontend for the Java FSE — React track)

> Maps to [`Week 5/React.md`](../Week%205/React.md) (Exercises 1–12), [`Week 6/Week6.md`](../Week%206/Week6.md) (Exercises 13–19) and [`Notes/notes.md`](../Notes/notes.md).
> **References:** [react.dev](https://react.dev/), [React Router](https://reactrouter.com/), [Testing Library](https://testing-library.com/docs/react-testing-library/intro/)

---

## 1. SPA vs MPA ⭐

**SPA (Single-Page Application)** — the browser loads **one** HTML shell; JavaScript then swaps content and updates the URL without full page reloads. Data comes from REST APIs as JSON.

| | SPA | MPA |
|---|---|---|
| Page loads | one initial load, then client-side routing | full reload per navigation |
| Speed after load | **fast**, no flicker | slower, re-downloads shell each time |
| Server load | lower (serves JSON) | higher (renders HTML) |
| Initial load | **slower** (big JS bundle) | fast |
| SEO | harder (needs SSR/prerender) | **easy** |
| Offline | possible (service workers) | no |
| Complexity | client-side routing/state | simple |
| Examples | Gmail, Facebook, Trello | Amazon, Wikipedia, blogs |

**SPA benefits:** near-native responsiveness, clean separation of frontend/backend (a React SPA + a Spring Boot REST API is exactly the Cognizant FSE stack), reusable API for web + mobile, rich interactivity, reduced bandwidth.
**SPA drawbacks:** SEO effort, larger initial bundle, memory leaks in long sessions, browser history/back-button must be managed, needs JS enabled.

---

## 2. What React is & how it works ⭐

**React** is an open-source **JavaScript library for building user interfaces**, created by Jordan Walke at Facebook (2013). It is *not* a full framework — it does the view layer; you add routing, state management and HTTP separately.

**How it works**
1. You write **components** that return JSX describing the UI for the current props/state.
2. React builds a **Virtual DOM** tree from that.
3. On a state change React builds a new Virtual DOM tree.
4. **Diffing / reconciliation** compares the new tree with the previous one.
5. React computes the **minimal set of real DOM mutations** and applies them in a batch.

### Virtual DOM ⭐
A lightweight **in-memory JavaScript representation** of the real DOM.
**Why:** real DOM manipulation is slow (layout, reflow, repaint). Batching the minimal diff is far cheaper than re-rendering the page.
**Reconciliation heuristics:** different element types → discard the subtree and rebuild; same type → update attributes and recurse; lists use the **`key`** prop to match children across renders.
```jsx
{items.map(item => <li key={item.id}>{item.name}</li>)}   // ✅ stable unique key
{items.map((item, i) => <li key={i}>{item.name}</li>)}    // ❌ index keys break on reorder/insert
```

### Core features ⭐
| Feature | Meaning |
|---|---|
| **Component-based** | UI split into independent, reusable pieces |
| **JSX** | HTML-like syntax compiled to `React.createElement()` |
| **Virtual DOM** | efficient diff-and-patch updates |
| **One-way data binding** | data flows parent → child via props; predictable |
| **Declarative** | describe *what* the UI should look like, not *how* to mutate it |
| **Hooks** | state and lifecycle in function components (React 16.8+) |
| **Rich ecosystem** | Router, Redux, RTL, Next.js |
| **React Native** | the same model for mobile |

---

## 3. Setup & project structure (Exercise 1)
```bash
npx create-react-app myfirstreact      # CRA (used in this repo)
npm create vite@latest my-app -- --template react   # Vite — the modern, faster choice
cd myfirstreact && npm start           # dev server on :3000
npm run build                          # production bundle
npm test
```
```
myfirstreact/
├── public/index.html      ← the single page; contains <div id="root">
├── src/
│   ├── index.js           ← entry point: ReactDOM.createRoot(...).render(<App/>)
│   ├── App.js             ← root component
│   ├── components/        ← your components
│   └── App.css
└── package.json
```
```jsx
// src/index.js — React 18
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<React.StrictMode><App /></React.StrictMode>);
```

---

## 4. JSX
```jsx
const name = "Pranav";
const element = (
  <div className="container">                 {/* className, not class */}
    <h1>Hello, {name}!</h1>                    {/* {} embeds any JS expression */}
    <p>{2 + 2}</p>
    <input type="text" defaultValue={name} />
    <label htmlFor="x">Label</label>           {/* htmlFor, not for */}
    <br />                                     {/* tags must be closed */}
  </div>
);
```
**Rules:** one root element (or a `<>…</>` fragment) · `className`/`htmlFor` · camelCase attributes (`onClick`, `tabIndex`) · `{}` for expressions (not statements) · self-closing tags · comments as `{/* … */}`.
JSX compiles to `React.createElement(type, props, ...children)` — it is optional sugar, not required.

---

## 5. Components

```jsx
// FUNCTION component — ✅ the modern standard
function Welcome({ name, age = 18 }) {          // props destructured, with a default
  return <h1>Hello {name}, age {age}</h1>;
}
const Welcome2 = ({ name }) => <h1>Hello {name}</h1>;   // arrow form

// CLASS component — legacy, but you must be able to read it
class Welcome3 extends React.Component {
  render() { return <h1>Hello {this.props.name}</h1>; }
}

<Welcome name="Pranav" age={21} />
```

| | Function + Hooks | Class |
|---|---|---|
| Syntax | plain function | `extends React.Component` |
| State | `useState` | `this.state` / `setState` |
| Lifecycle | `useEffect` | `componentDidMount` etc. |
| `this` | none | required, needs binding |
| Verdict | ✅ recommended | legacy |

### Props ⭐
- **Read-only** — a component must never modify its own props (pure with respect to props).
- Flow **one way**: parent → child.
- Children: `props.children` renders whatever is nested inside the tag.
- Callbacks are how a child talks back to a parent (**lifting state up**).

```jsx
function Parent() {
  const [count, setCount] = useState(0);
  return <Child count={count} onIncrement={() => setCount(c => c + 1)} />;
}
function Child({ count, onIncrement }) {
  return <button onClick={onIncrement}>{count}</button>;
}
```

⭐ **Props vs State**
| Props | State |
|---|---|
| Passed **in** from the parent | Managed **inside** the component |
| Immutable (read-only) | Mutable via the setter |
| Makes a component configurable/reusable | Makes it interactive |
| Changing them re-renders the child | Changing it re-renders the component |

---

## 6. Hooks ⭐⭐

### `useState`
```jsx
const [count, setCount] = useState(0);
const [user, setUser] = useState({ name: '', email: '' });

setCount(count + 1);
setCount(prev => prev + 1);                        // ⭐ functional update — safe when batched
setUser(prev => ({ ...prev, name: 'Pranav' }));    // ⭐ NEVER mutate state directly
```
⚠️ **State updates are asynchronous and batched.** Reading `count` immediately after `setCount` gives the old value. Use the functional form when the new value depends on the old one.
⚠️ **Never mutate:** `user.name = 'x'` won't re-render — React compares by reference. Always create a new object/array.

### `useEffect` — side effects & lifecycle
```jsx
useEffect(() => { /* runs after EVERY render */ });

useEffect(() => { /* runs ONCE on mount — componentDidMount */ }, []);

useEffect(() => { /* runs when userId changes */ }, [userId]);

useEffect(() => {
  const timer = setInterval(tick, 1000);
  return () => clearInterval(timer);               // ⭐ CLEANUP — componentWillUnmount
}, []);
```
Use it for: data fetching, subscriptions, timers, manual DOM work, logging. **Not** for computing derived values (do that during render).
⚠️ Missing dependencies cause stale closures; the `react-hooks/exhaustive-deps` lint rule catches them.

### `useContext` (Exercise 14 — Employee theme) ⭐
Avoids **prop drilling** — passing props through many intermediate layers.
```jsx
// 1. create
const ThemeContext = createContext('light');

// 2. provide
function App() {
  const [theme, setTheme] = useState('light');
  return (
    <ThemeContext.Provider value={{ theme, setTheme }}>
      <Dashboard />
    </ThemeContext.Provider>
  );
}

// 3. consume — at any depth, no props in between
function EmployeeCard() {
  const { theme, setTheme } = useContext(ThemeContext);
  return (
    <div className={theme}>
      <button onClick={() => setTheme(t => t === 'light' ? 'dark' : 'light')}>Toggle</button>
    </div>
  );
}
```
> Context is for **low-frequency global** data (theme, auth user, locale). For complex, frequently-changing app state, use Redux/Zustand — every context consumer re-renders when the value changes.

### Other hooks
```jsx
const inputRef = useRef(null);          // DOM access or a mutable value that doesn't re-render
inputRef.current.focus();

const total = useMemo(() => expensiveCalc(items), [items]);       // memoize a VALUE
const handler = useCallback(() => doThing(id), [id]);             // memoize a FUNCTION

const [state, dispatch] = useReducer(reducer, initialState);      // complex state logic

// custom hook — reuse stateful logic; name MUST start with "use"
function useFetch(url) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  useEffect(() => {
    let cancelled = false;
    fetch(url).then(r => r.json())
      .then(d => { if (!cancelled) setData(d); })
      .catch(e => { if (!cancelled) setError(e); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [url]);
  return { data, loading, error };
}
```

### ⭐ Rules of Hooks
1. Only call hooks at the **top level** — never inside conditions, loops or nested functions.
2. Only call hooks from **React function components** or **custom hooks**.
> Why? React tracks hook state **by call order**. A conditional hook shifts the order between renders and corrupts the state mapping.

---

## 7. Class lifecycle (know it for legacy code)
```
MOUNTING:   constructor → render → componentDidMount
UPDATING:   render → componentDidUpdate
UNMOUNTING: componentWillUnmount
ERRORS:     getDerivedStateFromError → componentDidCatch
```
Hook equivalents:
```jsx
useEffect(() => {}, [])          // componentDidMount
useEffect(() => {}, [dep])       // componentDidUpdate (for dep)
useEffect(() => () => {}, [])    // componentWillUnmount
```

---

## 8. Rendering patterns

### Conditional rendering (Exercise 13 — Blogger app)
```jsx
{isLoggedIn ? <Dashboard /> : <Login />}
{posts.length > 0 && <PostList posts={posts} />}
{error && <p className="error">{error}</p>}
{loading ? <Spinner /> : <Content />}
{(() => { switch (status) { case 'loading': return <Spinner/>; default: return <Data/>; } })()}
```
⚠️ `{count && <X/>}` renders a literal `0` when count is 0 — use `{count > 0 && <X/>}`.

### Lists & keys (Exercises 2, 4, 6, 10)
```jsx
<ul>
  {students.map(s => (
    <li key={s.id}>{s.name} — {s.course}</li>     // ⭐ key must be stable & unique
  ))}
</ul>
```

### Events (Exercise 11)
```jsx
<button onClick={handleClick}>Click</button>
<button onClick={() => handleDelete(id)}>Delete</button>     {/* arrow to pass args */}
<form onSubmit={e => { e.preventDefault(); submit(); }}>      {/* prevent page reload */}
<input onChange={e => setName(e.target.value)} />
```
React uses a **SyntheticEvent** — a cross-browser wrapper over the native event, with delegation at the root for performance.

### Forms — controlled components (Exercise 16 — Mail registration validation) ⭐
```jsx
function RegistrationForm() {
  const [form, setForm] = useState({ email: '', password: '' });
  const [errors, setErrors] = useState({});

  const handleChange = e => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));       // computed property name
  };

  const validate = () => {
    const err = {};
    if (!form.email) err.email = 'Email is required';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) err.email = 'Invalid email';
    if (form.password.length < 8) err.password = 'Minimum 8 characters';
    setErrors(err);
    return Object.keys(err).length === 0;
  };

  const handleSubmit = e => {
    e.preventDefault();
    if (validate()) console.log('submit', form);
  };

  return (
    <form onSubmit={handleSubmit} noValidate>
      <input name="email" value={form.email} onChange={handleChange} />
      {errors.email && <span className="error">{errors.email}</span>}
      <input name="password" type="password" value={form.password} onChange={handleChange} />
      {errors.password && <span className="error">{errors.password}</span>}
      <button type="submit">Register</button>
    </form>
  );
}
```
⭐ **Controlled** — React state is the single source of truth (`value` + `onChange`). **Uncontrolled** — the DOM holds the value, read via a `ref`. Prefer controlled.

### Styling (Exercise 5 — Cohort dashboard)
```jsx
<div style={{ color: 'red', fontSize: '16px' }}>inline (camelCase, object)</div>
import './Dashboard.css';           <div className="card">CSS file</div>
import styles from './C.module.css'; <div className={styles.card}>CSS Modules</div>
<div className={`card ${isActive ? 'active' : ''}`}>conditional classes</div>
```

---

## 9. Routing (Exercise 6 — Trainers app)
```bash
npm install react-router-dom
```
```jsx
import { BrowserRouter, Routes, Route, Link, NavLink,
         useParams, useNavigate, Outlet } from 'react-router-dom';

function App() {
  return (
    <BrowserRouter>
      <nav>
        <Link to="/">Home</Link>
        <NavLink to="/trainers" className={({isActive}) => isActive ? 'active' : ''}>
          Trainers
        </NavLink>
      </nav>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/trainers" element={<TrainerList />} />
        <Route path="/trainers/:id" element={<TrainerDetail />} />
        <Route path="/admin" element={<Layout />}>            {/* nested */}
          <Route index element={<Dashboard />} />
          <Route path="users" element={<Users />} />
        </Route>
        <Route path="*" element={<NotFound />} />              {/* 404 */}
      </Routes>
    </BrowserRouter>
  );
}

function TrainerDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  return <button onClick={() => navigate('/trainers')}>Back</button>;
}
```
Hooks: `useParams`, `useNavigate`, `useLocation`, `useSearchParams`.
> `<Link>` does client-side navigation (no reload); a plain `<a href>` reloads the whole app.

---

## 10. Data fetching with Axios (Exercises 17, 19)
```bash
npm install axios
```
```jsx
import axios from 'axios';

function UserList() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const controller = new AbortController();
    axios.get('/api/users', { signal: controller.signal })
      .then(res => setUsers(res.data))
      .catch(err => { if (!axios.isCancel(err)) setError(err.message); })
      .finally(() => setLoading(false));
    return () => controller.abort();                  // cleanup on unmount
  }, []);

  if (loading) return <p>Loading…</p>;
  if (error)   return <p className="error">{error}</p>;
  return <ul>{users.map(u => <li key={u.id}>{u.name}</li>)}</ul>;
}
```
```jsx
// a configured instance with a JWT interceptor — how a React SPA talks to your Spring API
const api = axios.create({ baseURL: 'http://localhost:8080/api', timeout: 5000 });
api.interceptors.request.use(cfg => {
  const token = localStorage.getItem('token');
  if (token) cfg.headers.Authorization = `Bearer ${token}`;
  return cfg;
});
api.interceptors.response.use(
  res => res,
  err => { if (err.response?.status === 401) window.location = '/login';
           return Promise.reject(err); });
```
| Axios | fetch |
|---|---|
| auto JSON parse | manual `res.json()` |
| rejects on 4xx/5xx | only rejects on network errors |
| interceptors, timeout, cancel | manual |
| needs install | built into the browser |

---

## 11. State management beyond hooks
| Approach | When |
|---|---|
| `useState` | local component state |
| Lifting state up | shared by a few siblings |
| `useContext` + `useReducer` | app-wide, low-frequency (auth, theme) |
| **Redux Toolkit** | large apps, complex flows, time-travel debugging |
| Zustand / Jotai | lighter modern alternatives |
| React Query / TanStack Query | **server** state — caching, refetching, invalidation |

**Redux concepts:** single immutable **store** → **actions** describe what happened → **reducers** are pure functions `(state, action) => newState` → components `useSelector`/`useDispatch`. Redux Toolkit removes most of the boilerplate.

---

## 12. ES6+ JavaScript you must know ⭐ (Exercise 9 — Cricket app)
```js
// let / const — block scoped (var is function-scoped and hoisted)
const PI = 3.14; let count = 0;

// arrow functions — lexical `this`
const add = (a, b) => a + b;
const square = x => x * x;

// template literals
`Hello ${name}, you have ${n} messages`

// destructuring
const { name, age } = person;
const [first, second, ...rest] = arr;
const { name: userName = 'Guest' } = person;      // rename + default

// spread / rest
const merged = { ...obj1, ...obj2 };
const copy = [...arr];
function sum(...nums) { return nums.reduce((a,b) => a+b, 0); }

// array methods — the ones you use constantly in React
arr.map(x => x * 2)                    // transform  → new array
arr.filter(x => x > 5)                 // select
arr.reduce((acc, x) => acc + x, 0)     // aggregate
arr.find(x => x.id === 1)              // first match
arr.findIndex(...)  arr.some(...)  arr.every(...)  arr.includes(x)
arr.sort((a,b) => a.score - b.score)   // ⚠️ mutates — copy first: [...arr].sort()

// optional chaining & nullish coalescing
user?.address?.city
const port = config.port ?? 8080;      // ?? only falls back on null/undefined (0 and '' survive)

// modules
export default Component;  export const helper = () => {};
import Component, { helper } from './Component';

// promises & async/await
const data = await fetch(url).then(r => r.json());
async function load() {
  try { const res = await axios.get('/api'); return res.data; }
  catch (e) { console.error(e); }
}
const [a, b] = await Promise.all([fetchA(), fetchB()]);

// classes
class Player { constructor(name){ this.name = name; } get info(){ return this.name; } }
```

---

## 13. Testing React (Exercises 18, 19) ⭐
```jsx
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import CohortDetails from './CohortDetails';

test('renders cohort name', () => {
  render(<CohortDetails name="Java FSE" size={30} />);
  expect(screen.getByText('Java FSE')).toBeInTheDocument();
  expect(screen.getByRole('heading', { level: 2 })).toHaveTextContent('Java FSE');
});

test('increments the counter on click', async () => {
  render(<Counter />);
  await userEvent.click(screen.getByRole('button', { name: /increment/i }));
  expect(screen.getByText('Count: 1')).toBeInTheDocument();
});

// Mocking Axios (Exercise 19 — Git client)
jest.mock('axios');
import axios from 'axios';

test('displays fetched repositories', async () => {
  axios.get.mockResolvedValue({ data: [{ id: 1, name: 'repo-one' }] });

  render(<GitRepos user="Pranav-Sharma717" />);

  expect(await screen.findByText('repo-one')).toBeInTheDocument();
  expect(axios.get).toHaveBeenCalledWith(
    expect.stringContaining('Pranav-Sharma717'));
});

test('shows an error message when the request fails', async () => {
  axios.get.mockRejectedValue(new Error('Network Error'));
  render(<GitRepos user="x" />);
  expect(await screen.findByText(/network error/i)).toBeInTheDocument();
});
```
**Queries:** `getBy*` (throws if missing) · `queryBy*` (returns null — use to assert absence) · `findBy*` (async, returns a Promise).
**Priority:** `getByRole` > `getByLabelText` > `getByPlaceholderText` > `getByText` > `getByTestId` (last resort).
⭐ **RTL philosophy:** *"Test what the user sees and does, not implementation details."*

---

## 14. Performance & best practices
```jsx
const Memoized = React.memo(MyComponent);              // skip re-render if props are equal
const value = useMemo(() => heavy(a), [a]);            // memoize a computed value
const cb = useCallback(() => doThing(id), [id]);       // stable function identity
const Lazy = React.lazy(() => import('./Heavy'));      // code splitting
<Suspense fallback={<Spinner/>}><Lazy /></Suspense>
```
**Checklist**
1. Stable, unique `key`s — never array indices for dynamic lists
2. Never mutate state — always create new objects/arrays
3. Keep components small and single-purpose
4. Clean up effects (timers, subscriptions, aborted fetches)
5. Lift state only as high as it needs to go
6. Extract repeated logic into custom hooks
7. Don't over-optimise — measure with React DevTools Profiler first
8. Add an **Error Boundary** at the top level
9. Never put secrets in frontend code — anything in the bundle is public
10. Sanitise anything rendered with `dangerouslySetInnerHTML` (XSS)

---

## ⭐ Interview Gold — React Rapid Fire

| Q | A |
|---|---|
| What is React? | A JavaScript **library** for building component-based UIs, using a Virtual DOM and declarative rendering. |
| Library or framework? | A library — it handles the view; routing/state/HTTP are separate choices. |
| What is JSX and does the browser understand it? | HTML-like syntax compiled (by Babel) into `React.createElement` calls — the browser never sees JSX. |
| What is the Virtual DOM? | An in-memory tree React diffs against the previous tree to compute the minimal real-DOM updates. |
| What is reconciliation? | The diffing algorithm that decides what to update; uses element type and `key` to match nodes. |
| Why are keys needed? | To identify list items across renders so React updates instead of recreating them; index keys break on reorder. |
| Props vs state? | Read-only input from the parent vs internal mutable data owned by the component. |
| Can you modify props? | No — components must treat props as immutable. |
| SPA vs MPA? | One shell + client-side routing vs full page reloads per navigation. |
| Function vs class components? | Hooks-based functions are the modern standard; classes are legacy. |
| What are hooks and why were they added? | Functions that let function components use state/lifecycle — enabling logic reuse without HOCs/render props. |
| Rules of Hooks? | Top level only, and only in components or custom hooks — React tracks hooks by call order. |
| `useState` — why the functional update form? | Updates are batched and async; `setX(prev => prev+1)` avoids stale values. |
| `useEffect` dependency array — three cases? | Omitted = every render; `[]` = once on mount; `[a,b]` = when a or b changes. |
| How do you do cleanup in `useEffect`? | Return a cleanup function — it runs on unmount and before the next effect run. |
| `useMemo` vs `useCallback`? | Memoize a computed **value** vs a **function reference**. |
| What is prop drilling and how do you avoid it? | Passing props through many layers; solve with Context or a state library. |
| When would you use Context vs Redux? | Context for low-frequency global data; Redux for large, complex, frequently-updated state. |
| Controlled vs uncontrolled components? | React state is the source of truth (`value`+`onChange`) vs the DOM holds the value (read via ref). |
| How do you fetch data in React? | `useEffect` + axios/fetch with loading/error state and cleanup — or React Query for caching. |
| How do you handle forms and validation? | Controlled inputs in state, a validate function producing an errors object, `e.preventDefault()` on submit. |
| One-way vs two-way binding? | React is one-way (props down, events up) — more predictable and easier to debug than Angular's two-way. |
| How does routing work in a SPA? | React Router intercepts navigation, updates the History API, and renders the matching component — no server round trip. |
| How do you optimise React performance? | `React.memo`, `useMemo`/`useCallback`, stable keys, code splitting with `lazy`+`Suspense`, virtualised lists. |
| How do you test a React component? | React Testing Library — render, query by role/text, simulate user events, mock axios with Jest. |
| What is an Error Boundary? | A class component with `componentDidCatch`/`getDerivedStateFromError` that catches render errors in its subtree. |
| How does a React app talk to a Spring Boot API? | Axios to REST endpoints with a JWT `Authorization` header; the backend must enable CORS for the SPA origin. |

---

## 📚 References
- React official docs (rewritten, hooks-first): https://react.dev/
- React — Thinking in React: https://react.dev/learn/thinking-in-react
- React — Rules of Hooks: https://react.dev/reference/rules/rules-of-hooks
- React Router: https://reactrouter.com/
- Axios: https://axios-http.com/docs/intro
- React Testing Library: https://testing-library.com/docs/react-testing-library/intro/
- Jest: https://jestjs.io/docs/getting-started
- MDN — JavaScript: https://developer.mozilla.org/en-US/docs/Web/JavaScript
- Redux Toolkit: https://redux-toolkit.js.org/
- Vite: https://vite.dev/
- javascript.info (best free JS course): https://javascript.info/
