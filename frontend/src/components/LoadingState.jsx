function LoadingState({ message = "Loading…" }) {
  return (
    <div className="loading-container">
      <div className="loading-spinner" role="status" aria-label="Loading" />
      <p className="loading-text">{message}</p>
    </div>
  );
}

export default LoadingState;
