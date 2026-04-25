import { useState, useEffect } from 'react'
import './App.css'

function App() {
  const [todos, setTodos] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    // Fetch todos from backend on component mount
    fetch('/api/todos')
      .then(response => {
        if (!response.ok) throw new Error('Failed to fetch todos')
        return response.json()
      })
      .then(data => {
        setTodos(data)
        setLoading(false)
      })
      .catch(err => {
        setError(err.message)
        setLoading(false)
      })
  }, [])

  if (loading) return <div className="container"><p>Loading todos...</p></div>
  if (error) return <div className="container"><p className="error">Error: {error}</p></div>

  return (
    <div className="container">
      <h1>Todo App</h1>
      <p>Backend connection established.</p>
      <p>Total todos: {todos.length}</p>

      {todos.length === 0 ? (
        <p className="empty-state">No todos yet. Create one to get started!</p>
      ) : (
        <ul className="todo-list">
          {todos.map(todo => (
            <li key={todo.id} className={`todo-item ${todo.completed ? 'completed' : ''}`}>
              <span>{textContent(todo.title)}</span>
              <span className="status">{todo.completed ? 'Done' : 'Active'}</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

// Helper function to safely set text content
function textContent(text) {
  return text
}

export default App
