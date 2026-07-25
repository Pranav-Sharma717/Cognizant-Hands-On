# Week 6 - ReactJS Hands-on Solutions

## Exercise 13: Blogger App - Conditional Rendering

Create the app and replace `src/App.js` with the following implementation. It demonstrates `if/else`, a ternary, and logical `&&` conditional rendering while rendering the three required components.

```bash
npx create-react-app bloggerapp
cd bloggerapp
npm start
```

```jsx
import { useState } from "react";

function BookDetails() {
  return <section><h2>Book Details</h2><p>React in Action</p></section>;
}

function BlogDetails() {
  return <section><h2>Blog Details</h2><p>Understanding conditional rendering.</p></section>;
}

function CourseDetails() {
  return <section><h2>Course Details</h2><p>Java FSE Deep Skilling</p></section>;
}

export default function App() {
  const [showBooks, setShowBooks] = useState(true);
  const [showBlogs, setShowBlogs] = useState(true);
  const [showCourses, setShowCourses] = useState(true);

  let books = null;
  if (showBooks) books = <BookDetails />;

  return (
    <main>
      <h1>Blogger App</h1>
      <button onClick={() => setShowBooks(!showBooks)}>Toggle books</button>
      <button onClick={() => setShowBlogs(!showBlogs)}>Toggle blogs</button>
      <button onClick={() => setShowCourses(!showCourses)}>Toggle courses</button>
      {books}
      {showBlogs ? <BlogDetails /> : <p>Blogs are hidden.</p>}
      {showCourses && <CourseDetails />}
    </main>
  );
}
```

## Exercise 14: Employee Theme with Context API

`src/ThemeContext.js`

```jsx
import { createContext } from "react";

const ThemeContext = createContext("light");
export default ThemeContext;
```

`src/EmployeeCard.js`

```jsx
import { useContext } from "react";
import ThemeContext from "./ThemeContext";

export default function EmployeeCard({ employee }) {
  const theme = useContext(ThemeContext);
  return <button className={theme}>{employee.name}</button>;
}
```

`src/EmployeesList.js`

```jsx
import EmployeeCard from "./EmployeeCard";

export default function EmployeesList({ employees }) {
  return <div>{employees.map((employee) => <EmployeeCard key={employee.id} employee={employee} />)}</div>;
}
```

`src/App.js`

```jsx
import { useState } from "react";
import ThemeContext from "./ThemeContext";
import EmployeesList from "./EmployeesList";

const employees = [{ id: 1, name: "Sajjad" }, { id: 2, name: "Asha" }];

export default function App() {
  const [theme, setTheme] = useState("light");
  return (
    <ThemeContext.Provider value={theme}>
      <button onClick={() => setTheme(theme === "light" ? "dark" : "light")}>Change theme</button>
      <EmployeesList employees={employees} />
    </ThemeContext.Provider>
  );
}
```

## Exercise 15: Ticket Raising App

```bash
npx create-react-app ticketraisingapp
cd ticketraisingapp
npm start
```

`src/ComplaintRegister.js`

```jsx
import { useState } from "react";

export default function ComplaintRegister() {
  const [employeeName, setEmployeeName] = useState("");
  const [complaint, setComplaint] = useState("");

  function handleSubmit(event) {
    event.preventDefault();
    const referenceNumber = `CMP-${Date.now()}`;
    alert(`Complaint raised successfully. Reference number: ${referenceNumber}`);
    setEmployeeName("");
    setComplaint("");
  }

  return (
    <form onSubmit={handleSubmit}>
      <h1>Raise a Complaint</h1>
      <input value={employeeName} onChange={(event) => setEmployeeName(event.target.value)} placeholder="Employee name" required />
      <textarea value={complaint} onChange={(event) => setComplaint(event.target.value)} placeholder="Complaint" required />
      <button type="submit">Submit complaint</button>
    </form>
  );
}
```

`src/App.js`

```jsx
import ComplaintRegister from "./ComplaintRegister";
export default function App() { return <ComplaintRegister />; }
```

## Exercise 16: Mail Registration Validation

```bash
npx create-react-app mailregisterapp
cd mailregisterapp
npm start
```

`src/register.js`

```jsx
import { useState } from "react";

export default function Register() {
  const [form, setForm] = useState({ name: "", email: "", password: "" });
  const [errors, setErrors] = useState({});

  function validate(values) {
    const nextErrors = {};
    if (values.name.trim().length < 5) nextErrors.name = "Name must contain at least 5 characters.";
    if (!values.email.includes("@") || !values.email.includes(".")) nextErrors.email = "Enter a valid email address.";
    if (values.password.length < 8) nextErrors.password = "Password must contain at least 8 characters.";
    return nextErrors;
  }

  function handleChange(event) {
    const nextForm = { ...form, [event.target.name]: event.target.value };
    setForm(nextForm);
    setErrors(validate(nextForm));
  }

  function handleSubmit(event) {
    event.preventDefault();
    const nextErrors = validate(form);
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length === 0) alert("Registration successful.");
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <h1>Mail Registration</h1>
      <input name="name" value={form.name} onChange={handleChange} placeholder="Name" /> {errors.name && <small>{errors.name}</small>}
      <input name="email" value={form.email} onChange={handleChange} placeholder="Email" /> {errors.email && <small>{errors.email}</small>}
      <input name="password" type="password" value={form.password} onChange={handleChange} placeholder="Password" /> {errors.password && <small>{errors.password}</small>}
      <button type="submit">Register</button>
    </form>
  );
}
```

`src/App.js`

```jsx
import Register from "./register";
export default function App() { return <Register />; }
```

## Exercise 17: Fetch User App

```bash
npx create-react-app fetchuserapp
cd fetchuserapp
npm start
```

`src/Getuser.js`

```jsx
import { Component } from "react";

export default class Getuser extends Component {
  state = { user: null, error: "" };

  async componentDidMount() {
    try {
      const response = await fetch("https://randomuser.me/api/");
      if (!response.ok) throw new Error("Unable to load user details.");
      const { results } = await response.json();
      this.setState({ user: results[0] });
    } catch (error) {
      this.setState({ error: error.message });
    }
  }

  render() {
    const { user, error } = this.state;
    if (error) return <p>{error}</p>;
    if (!user) return <p>Loading user...</p>;
    return <section><h1>{user.name.title} {user.name.first}</h1><img src={user.picture.large} alt={user.name.first} /></section>;
  }
}
```

`src/App.js`

```jsx
import Getuser from "./Getuser";
export default function App() { return <Getuser />; }
```

## Exercise 18: Cohort Details Unit Tests

Install dependencies compatible with the class-component Enzyme exercise:

```bash
npm install --save-dev enzyme enzyme-adapter-react-16
```

`src/setupTests.js`

```jsx
import { configure } from "enzyme";
import Adapter from "enzyme-adapter-react-16";
configure({ adapter: new Adapter() });
```

`src/CohortDetails.test.js`

```jsx
import { mount, shallow } from "enzyme";
import CohortDetails from "./CohortDetails";
import CohortData from "./Cohort";

describe("Cohort Details Component", () => {
  const cohort = CohortData[0];

  test("should create the component", () => {
    expect(shallow(<CohortDetails cohort={cohort} />).exists()).toBe(true);
  });

  test("should initialize the props", () => {
    expect(mount(<CohortDetails cohort={cohort} />).props().cohort).toEqual(cohort);
  });

  test("should display cohort code in h3", () => {
    expect(mount(<CohortDetails cohort={cohort} />).find("h3").text()).toBe(cohort.cohortCode);
  });

  test("should always render same html", () => {
    expect(shallow(<CohortDetails cohort={cohort} />)).toMatchSnapshot();
  });
});
```

Run the tests:

```bash
npm test
```

## Exercise 19: Git Client with Axios Mocking

```bash
npx create-react-app gitclientapp
cd gitclientapp
npm install axios
npm start
```

`src/GitClient.js`

```jsx
import axios from "axios";

export default class GitClient {
  async getRepositories(username) {
    const response = await axios.get(`https://api.github.com/users/${username}/repos`);
    return response.data.map((repository) => repository.name);
  }
}
```

`src/App.js`

```jsx
import { useEffect, useState } from "react";
import GitClient from "./GitClient";

export default function App() {
  const [repositories, setRepositories] = useState([]);
  useEffect(() => { new GitClient().getRepositories("techiesyed").then(setRepositories); }, []);
  return <main><h1>Repositories</h1><ul>{repositories.map((name) => <li key={name}>{name}</li>)}</ul></main>;
}
```

`src/GitClient.test.js`

```jsx
import axios from "axios";
import GitClient from "./GitClient";

jest.mock("axios");

describe("Git Client Tests", () => {
  test("should return repository names for techiesyed", async () => {
    axios.get.mockResolvedValue({ data: [{ name: "react-demo" }, { name: "git-client" }] });
    await expect(new GitClient().getRepositories("techiesyed")).resolves.toEqual(["react-demo", "git-client"]);
    expect(axios.get).toHaveBeenCalledWith("https://api.github.com/users/techiesyed/repos");
  });
});
```

```bash
npm test
```