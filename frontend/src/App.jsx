import "./App.css";

function App() {
  return (
    <div className="container">
      <div className="login-card">
        <h1>Purchase Order Management System</h1>
        <p className="subtitle">
          Login to continue
        </p>

        <form>
          <div className="input-group">
            <label>Email</label>
            <input
              type="email"
              placeholder="Enter your email"
            />
          </div>

          <div className="input-group">
            <label>Password</label>
            <input
              type="password"
              placeholder="Enter your password"
            />
          </div>

          <button type="submit">
            Login
          </button>
        </form>
      </div>
    </div>
  );
}

export default App;