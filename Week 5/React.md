# week5_react

## ReactJS Hands-on Solutions

### Exercise 1: My First React Application

**App.js**

```jsx
function App() {
  return <h1>Welcome to the first session of React</h1>;
}

export default App;
```

Run the application:

```bash
npx create-react-app myfirstreact
cd myfirstreact
npm start
```

### Exercise 2: Student Management Portal

**src/Components/Home.js**

```jsx
import React, { Component } from "react";

class Home extends Component {
  render() {
    return <h2>Welcome to the Home page of Student Management Portal</h2>;
  }
}

export default Home;
```

**src/Components/About.js**

```jsx
import React, { Component } from "react";

class About extends Component {
  render() {
    return <h2>Welcome to the About page of the Student Management Portal</h2>;
  }
}

export default About;
```

**src/Components/Contact.js**

```jsx
import React, { Component } from "react";

class Contact extends Component {
  render() {
    return <h2>Welcome to the Contact page of the Student Management Portal</h2>;
  }
}

export default Contact;
```

**src/App.js**

```jsx
import Home from "./Components/Home";
import About from "./Components/About";
import Contact from "./Components/Contact";

function App() {
  return (
    <div>
      <Home />
      <About />
      <Contact />
    </div>
  );
}

export default App;
```

### Exercise 3: Score Calculator

**src/Components/CalculateScore.js**

```jsx
function CalculateScore({ name, school, total, goal }) {
  const average = (total / goal).toFixed(2);

  return (
    <div className="score-card">
      <h2>Student Details</h2>
      <p>Name: {name}</p>
      <p>School: {school}</p>
      <p>Total Score: {total}</p>
      <p>Number of Subjects: {goal}</p>
      <p>Average Score: {average}</p>
    </div>
  );
}

export default CalculateScore;
```

**src/Stylesheets/mystyle.css**

```css
.score-card {
  width: 340px;
  margin: 30px auto;
  padding: 20px;
  border: 1px solid #333;
  border-radius: 8px;
  font-family: Arial, sans-serif;
}

.score-card h2 {
  color: #1d4ed8;
}
```

**src/App.js**

```jsx
import CalculateScore from "./Components/CalculateScore";
import "./Stylesheets/mystyle.css";

function App() {
  return (
    <CalculateScore
      name="Sajjad"
      school="Cognizant Academy"
      total={450}
      goal={5}
    />
  );
}

export default App;
```

### Exercise 4: Blog Application

**src/Post.js**

```jsx
class Post {
  constructor(id, title, body) {
    this.id = id;
    this.title = title;
    this.body = body;
  }
}

export default Post;
```

**src/Posts.js**

```jsx
import React, { Component } from "react";
import Post from "./Post";

class Posts extends Component {
  constructor(props) {
    super(props);
    this.state = {
      posts: [],
    };
  }

  async loadPosts() {
    const response = await fetch("https://jsonplaceholder.typicode.com/posts");
    const data = await response.json();
    const posts = data.map((item) => new Post(item.id, item.title, item.body));
    this.setState({ posts });
  }

  componentDidMount() {
    this.loadPosts();
  }

  componentDidCatch(error) {
    alert(error.message);
  }

  render() {
    return (
      <div>
        <h1>Blog Posts</h1>
        {this.state.posts.map((post) => (
          <article key={post.id}>
            <h3>{post.title}</h3>
            <p>{post.body}</p>
          </article>
        ))}
      </div>
    );
  }
}

export default Posts;
```

**src/App.js**

```jsx
import Posts from "./Posts";

function App() {
  return <Posts />;
}

export default App;
```

### Exercise 5: Cohort Dashboard Styling

**src/CohortDetails.module.css**

```css
.box {
  width: 300px;
  display: inline-block;
  margin: 10px;
  padding: 10px 20px;
  border: 1px solid black;
  border-radius: 10px;
  vertical-align: top;
}

dt {
  font-weight: 500;
}
```

**src/CohortDetails.js**

```jsx
import styles from "./CohortDetails.module.css";

function CohortDetails({ cohort }) {
  const color =
    cohort.currentStatus.toLowerCase() === "ongoing" ? "green" : "blue";

  return (
    <div className={styles.box}>
      <h3 style={{ color }}>{cohort.cohortCode}</h3>
      <dl>
        <dt>Started On</dt>
        <dd>{cohort.startDate}</dd>
        <dt>Current Status</dt>
        <dd>{cohort.currentStatus}</dd>
        <dt>Coach</dt>
        <dd>{cohort.coach}</dd>
        <dt>Trainer</dt>
        <dd>{cohort.trainer}</dd>
      </dl>
    </div>
  );
}

export default CohortDetails;
```

### Exercise 6: Trainers Application with React Router

Install React Router:

```bash
npm install react-router-dom
```

**src/Trainer.js**

```jsx
class Trainer {
  constructor(trainerId, name, email, phone, technology, skills) {
    this.trainerId = trainerId;
    this.name = name;
    this.email = email;
    this.phone = phone;
    this.technology = technology;
    this.skills = skills;
  }
}

export default Trainer;
```

**src/TrainersMock.js**

```jsx
import Trainer from "./Trainer";

const trainers = [
  new Trainer(1, "Aathma", "aathma@cognizant.com", "9876543210", "Java", "Spring Boot, SQL"),
  new Trainer(2, "Apoorv", "apoorv@cognizant.com", "9876543211", "React", "React, JavaScript"),
  new Trainer(3, "Elisa Smith", "elisa@cognizant.com", "9876543212", ".NET", "C#, ASP.NET"),
];

export default trainers;
```

**src/Home.js**

```jsx
function Home() {
  return <h2>Welcome to Cognizant Academy Trainer Portal</h2>;
}

export default Home;
```

**src/TrainersList.js**

```jsx
import { Link } from "react-router-dom";

function TrainersList({ trainers }) {
  return (
    <div>
      <h2>Trainers List</h2>
      <ul>
        {trainers.map((trainer) => (
          <li key={trainer.trainerId}>
            <Link to={`/trainers/${trainer.trainerId}`}>{trainer.name}</Link>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default TrainersList;
```

**src/TrainerDetails.js**

```jsx
import { useParams } from "react-router-dom";
import trainers from "./TrainersMock";

function TrainerDetails() {
  const { id } = useParams();
  const trainer = trainers.find((item) => item.trainerId === Number(id));

  if (!trainer) {
    return <h2>Trainer not found</h2>;
  }

  return (
    <div>
      <h2>Trainer Details</h2>
      <p>Trainer ID: {trainer.trainerId}</p>
      <p>Name: {trainer.name}</p>
      <p>Email: {trainer.email}</p>
      <p>Phone: {trainer.phone}</p>
      <p>Technology: {trainer.technology}</p>
      <p>Skills: {trainer.skills}</p>
    </div>
  );
}

export default TrainerDetails;
```

**src/App.js**

```jsx
import { BrowserRouter, Link, Navigate, Route, Routes } from "react-router-dom";
import Home from "./Home";
import TrainersList from "./TrainersList";
import TrainerDetails from "./TrainerDetails";
import trainers from "./TrainersMock";

function App() {
  return (
    <BrowserRouter>
      <nav>
        <Link to="/home">Home</Link> | <Link to="/trainers">Trainers</Link>
      </nav>
      <Routes>
        <Route path="/" element={<Navigate to="/home" replace />} />
        <Route path="/home" element={<Home />} />
        <Route path="/trainers" element={<TrainersList trainers={trainers} />} />
        <Route path="/trainers/:id" element={<TrainerDetails />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
```

### Exercise 7: Online Shopping Cart

**src/Cart.js**

```jsx
import React, { Component } from "react";

class Cart extends Component {
  render() {
    return (
      <tr>
        <td>{this.props.itemName}</td>
        <td>Rs. {this.props.price}</td>
      </tr>
    );
  }
}

export default Cart;
```

**src/OnlineShopping.js**

```jsx
import React, { Component } from "react";
import Cart from "./Cart";

class OnlineShopping extends Component {
  constructor(props) {
    super(props);
    this.items = [
      { itemName: "Laptop", price: 65000 },
      { itemName: "Mobile", price: 30000 },
      { itemName: "Headphones", price: 2500 },
      { itemName: "Keyboard", price: 1800 },
      { itemName: "Mouse", price: 900 },
    ];
  }

  render() {
    return (
      <div>
        <h1>Online Shopping</h1>
        <table border="1" cellPadding="8">
          <thead>
            <tr>
              <th>Item Name</th>
              <th>Price</th>
            </tr>
          </thead>
          <tbody>
            {this.items.map((item) => (
              <Cart key={item.itemName} itemName={item.itemName} price={item.price} />
            ))}
          </tbody>
        </table>
      </div>
    );
  }
}

export default OnlineShopping;
```

**src/App.js**

```jsx
import OnlineShopping from "./OnlineShopping";

function App() {
  return <OnlineShopping />;
}

export default App;
```

### Exercise 8: Counter Application

**src/CountPeople.js**

```jsx
import React, { Component } from "react";

class CountPeople extends Component {
  constructor(props) {
    super(props);
    this.state = {
      entryCount: 0,
      exitCount: 0,
    };
  }

  updateEntry = () => {
    this.setState((previousState) => ({
      entryCount: previousState.entryCount + 1,
    }));
  };

  updateExit = () => {
    this.setState((previousState) => ({
      exitCount: previousState.exitCount + 1,
    }));
  };

  render() {
    return (
      <div>
        <h1>Mall Counter</h1>
        <p>People Entered: {this.state.entryCount}</p>
        <p>People Exited: {this.state.exitCount}</p>
        <button onClick={this.updateEntry}>Login</button>
        <button onClick={this.updateExit}>Exit</button>
      </div>
    );
  }
}

export default CountPeople;
```

**src/App.js**

```jsx
import CountPeople from "./CountPeople";

function App() {
  return <CountPeople />;
}

export default App;
```

### Exercise 9: Cricket Application using ES6

**src/ListofPlayers.js**

```jsx
function ListofPlayers() {
  const players = [
    { name: "Rohit Sharma", score: 82 },
    { name: "Virat Kohli", score: 65 },
    { name: "Shubman Gill", score: 91 },
    { name: "KL Rahul", score: 45 },
    { name: "Hardik Pandya", score: 72 },
    { name: "Ravindra Jadeja", score: 58 },
    { name: "Jasprit Bumrah", score: 25 },
    { name: "Mohammed Shami", score: 35 },
    { name: "Suryakumar Yadav", score: 78 },
    { name: "Rishabh Pant", score: 69 },
    { name: "Kuldeep Yadav", score: 41 },
  ];

  const playersBelow70 = players.filter((player) => player.score < 70);

  return (
    <div>
      <h2>List of Players</h2>
      {players.map((player) => (
        <p key={player.name}>{player.name} - {player.score}</p>
      ))}
      <h2>Players with Score Below 70</h2>
      {playersBelow70.map((player) => (
        <p key={player.name}>{player.name} - {player.score}</p>
      ))}
    </div>
  );
}

export default ListofPlayers;
```

**src/IndianPlayers.js**

```jsx
function IndianPlayers() {
  const teamPlayers = ["Rohit", "Virat", "Gill", "Rahul", "Hardik", "Jadeja"];
  const [oddOne, , oddThree, , oddFive] = teamPlayers;
  const [, evenTwo, , evenFour, , evenSix] = teamPlayers;
  const t20Players = ["Rohit", "Virat", "Hardik"];
  const ranjiTrophyPlayers = ["Prithvi", "Sarfaraz", "Mayank"];
  const allPlayers = [...t20Players, ...ranjiTrophyPlayers];

  return (
    <div>
      <h2>Odd Team Players</h2>
      <p>{oddOne}, {oddThree}, {oddFive}</p>
      <h2>Even Team Players</h2>
      <p>{evenTwo}, {evenFour}, {evenSix}</p>
      <h2>Merged Players</h2>
      <p>{allPlayers.join(", ")}</p>
    </div>
  );
}

export default IndianPlayers;
```

**src/App.js**

```jsx
import IndianPlayers from "./IndianPlayers";
import ListofPlayers from "./ListofPlayers";

function App() {
  const flag = true;

  if (flag) {
    return <ListofPlayers />;
  }

  return <IndianPlayers />;
}

export default App;
```

### Exercise 10: Office Space Rental Application

**src/App.js**

```jsx
function App() {
  const officeSpaces = [
    { id: 1, name: "Tech Park", rent: 55000, address: "Bengaluru", image: "/office1.jpg" },
    { id: 2, name: "Cyber Hub", rent: 75000, address: "Gurugram", image: "/office2.jpg" },
    { id: 3, name: "Hitech City", rent: 68000, address: "Hyderabad", image: "/office3.jpg" },
  ];

  return (
    <div>
      <h1>Office Space Rental</h1>
      {officeSpaces.map((office) => (
        <div key={office.id}>
          <img src={office.image} alt={office.name} width="300" height="180" />
          <h2>{office.name}</h2>
          <p style={{ color: office.rent < 60000 ? "red" : "green" }}>
            Rent: Rs. {office.rent}
          </p>
          <p>Address: {office.address}</p>
        </div>
      ))}
    </div>
  );
}

export default App;
```

Place `office1.jpg`, `office2.jpg`, and `office3.jpg` in the `public` folder.

### Exercise 11: Event Examples

**src/CurrencyConvertor.js**

```jsx
import React, { Component } from "react";

class CurrencyConvertor extends Component {
  constructor(props) {
    super(props);
    this.state = { rupees: "", euros: "" };
  }

  handleChange = (event) => {
    this.setState({ rupees: event.target.value });
  };

  handleSubmit = () => {
    const euros = (Number(this.state.rupees) / 90).toFixed(2);
    this.setState({ euros });
  };

  render() {
    return (
      <div>
        <h2>Currency Convertor</h2>
        <input
          type="number"
          value={this.state.rupees}
          onChange={this.handleChange}
          placeholder="Enter amount in INR"
        />
        <button onClick={this.handleSubmit}>Convert</button>
        {this.state.euros && <p>Euro: € {this.state.euros}</p>}
      </div>
    );
  }
}

export default CurrencyConvertor;
```

**src/App.js**

```jsx
import React, { Component } from "react";
import CurrencyConvertor from "./CurrencyConvertor";

class App extends Component {
  constructor(props) {
    super(props);
    this.state = { count: 0 };
  }

  increment = () => {
    this.setState((previousState) => ({ count: previousState.count + 1 }));
  };

  decrement = () => {
    this.setState((previousState) => ({ count: previousState.count - 1 }));
  };

  sayHello = () => {
    alert("Hello! Have a nice day.");
  };

  sayWelcome = (message) => {
    alert(message);
  };

  showClickMessage = (event) => {
    alert("I was clicked");
    console.log(event.type);
  };

  render() {
    return (
      <div>
        <h1>Counter: {this.state.count}</h1>
        <button onClick={() => { this.increment(); this.sayHello(); }}>Increment</button>
        <button onClick={this.decrement}>Decrement</button>
        <button onClick={() => this.sayWelcome("Welcome")}>Say Welcome</button>
        <button onClick={this.showClickMessage}>OnPress</button>
        <CurrencyConvertor />
      </div>
    );
  }
}

export default App;
```

### Exercise 12: Ticket Booking Application

**src/App.js**

```jsx
import React, { Component } from "react";

function GuestPage() {
  return (
    <div>
      <h2>Flight Details</h2>
      <p>Flight: Cognizant Airways 101</p>
      <p>From: Chennai</p>
      <p>To: Bengaluru</p>
      <p>Departure: 10:00 AM</p>
      <p>Please log in to book tickets.</p>
    </div>
  );
}

function UserPage() {
  return (
    <div>
      <h2>Book Your Ticket</h2>
      <p>Flight: Cognizant Airways 101</p>
      <button>Book Ticket</button>
    </div>
  );
}

class App extends Component {
  constructor(props) {
    super(props);
    this.state = { isLoggedIn: false };
  }

  login = () => {
    this.setState({ isLoggedIn: true });
  };

  logout = () => {
    this.setState({ isLoggedIn: false });
  };

  render() {
    const { isLoggedIn } = this.state;

    return (
      <div>
        <h1>Ticket Booking Application</h1>
        {isLoggedIn ? <UserPage /> : <GuestPage />}
        {isLoggedIn ? (
          <button onClick={this.logout}>Logout</button>
        ) : (
          <button onClick={this.login}>Login</button>
        )}
      </div>
    );
  }
}

export default App;
```