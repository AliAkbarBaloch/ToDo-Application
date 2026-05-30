import { useState, useEffect, useCallback } from 'react'
import AddTodoForm from './components/AddTodoForm'
import EditTodoForm from './components/EditTodoForm'
import TodoItem from './components/TodoItem'
import FilterBar from './components/FilterBar'
import './App.css'

const API = '/api/todos'

/**
 * Root component — owns all state and API calls.
 *
 * US-01: createTodo   — POST /api/todos
 * US-02: loadTodos    — GET  /api/todos (initial + on filter/search change)
 * US-03: toggleTodo   — PATCH /api/todos/{id}/status
 * US-04: updateTodo   — PUT   /api/todos/{id}
 * US-05: deleteTodo   — DELETE /api/todos/{id}  (with confirmation — MN-02)
 * US-07: filter state — status query param
 * US-09: search state — search query param
 * US-10: persistence  — all data lives on Spring Boot / H2 server
 */
export default function App() {
  const [todos, setTodos] = useState([])
  const [allCount, setAllCount] = useState(0)
  const [activeCount, setActiveCount] = useState(0)
  const [completedCount, setCompletedCount] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [filter, setFilter] = useState('all')
  const [search, setSearch] = useState('')
  const [editingId, setEditingId] = useState(null)

  // Fetch counts for the filter bar badges (always from unfiltered list)
  const refreshCounts = useCallback(() => {
    fetch(API)
      .then((r) => r.json())
      .then((data) => {
        setAllCount(data.length)
        setActiveCount(data.filter((t) => !t.completed).length)
        setCompletedCount(data.filter((t) => t.completed).length)
      })
      .catch(() => {})
  }, [])

  const loadTodos = useCallback(() => {
    setLoading(true)
    setError(null)
    const params = new URLSearchParams()
    if (filter !== 'all') params.set('status', filter)
    if (search.trim()) params.set('search', search.trim())
    const url = params.toString() ? `${API}?${params}` : API

    fetch(url)
      .then((r) => (r.ok ? r.json() : Promise.reject('Failed to load tasks')))
      .then((data) => {
        setTodos(data)
        setLoading(false)
      })
      .catch((e) => {
        setError(String(e))
        setLoading(false)
      })
  }, [filter, search])

  useEffect(() => {
    loadTodos()
  }, [loadTodos])
  useEffect(() => {
    refreshCounts()
  }, [refreshCounts, todos])

  // US-01: Create a new task
  function createTodo(data) {
    return fetch(API, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    })
      .then((r) => (r.ok ? r.json() : r.json().then((e) => Promise.reject(e))))
      .then((created) => {
        // Show at top only when current filter would include it
        if (filter === 'all' || filter === 'active') {
          setTodos((prev) => [created, ...prev])
        }
      })
  }

  // US-04: Update an existing task
  function updateTodo(id, data) {
    return fetch(`${API}/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    })
      .then((r) => (r.ok ? r.json() : r.json().then((e) => Promise.reject(e))))
      .then((updated) => {
        setTodos((prev) => prev.map((t) => (t.id === id ? updated : t)))
        setEditingId(null)
      })
  }

  // US-03: Toggle completed / active
  function toggleTodo(id) {
    fetch(`${API}/${id}/status`, { method: 'PATCH' })
      .then((r) => (r.ok ? r.json() : Promise.reject('Toggle failed')))
      .then((updated) => {
        if (filter === 'all') {
          setTodos((prev) => prev.map((t) => (t.id === id ? updated : t)))
        } else {
          // Task no longer matches filter — remove it from view
          setTodos((prev) => prev.filter((t) => t.id !== id))
        }
      })
      .catch((e) => setError(String(e)))
  }

  // US-05/MN-02: Delete with mandatory confirmation dialog
  function deleteTodo(id, title) {
    if (!window.confirm(`Are you sure you want to delete "${title}"? This cannot be undone.`))
      return
    fetch(`${API}/${id}`, { method: 'DELETE' })
      .then((r) =>
        r.ok ? setTodos((prev) => prev.filter((t) => t.id !== id)) : Promise.reject('Delete failed')
      )
      .catch((e) => setError(String(e)))
  }

  const counts = { all: allCount, active: activeCount, completed: completedCount }

  return (
    <div className="app-container">
      <header className="app-header">
        <h1>✓ Todo Application</h1>
        <p>Organize your tasks, boost your productivity</p>
      </header>

      <main className="app-content">
        <AddTodoForm onSubmit={createTodo} />

        <FilterBar
          filter={filter}
          onFilterChange={(f) => {
            setFilter(f)
            setEditingId(null)
          }}
          search={search}
          onSearchChange={(s) => {
            setSearch(s)
            setEditingId(null)
          }}
          counts={counts}
        />

        <section className="task-list-section">
          <h2>Tasks ({todos.length})</h2>

          {loading && <p className="loading-message">Loading tasks…</p>}
          {error && <p className="error-message">{error}</p>}

          {!loading && !error && todos.length === 0 && (
            <div className="empty-state">
              <div className="empty-state-icon">📭</div>
              {search.trim() ? (
                <>
                  <h3>No Results</h3>
                  <p>No tasks match your search.</p>
                </>
              ) : (
                <>
                  <h3>No Tasks Yet</h3>
                  <p>No tasks yet. Add one above!</p>
                </>
              )}
            </div>
          )}

          {todos.map((todo) =>
            editingId === todo.id ? (
              <EditTodoForm
                key={todo.id}
                todo={todo}
                onSave={(data) => updateTodo(todo.id, data)}
                onCancel={() => setEditingId(null)}
              />
            ) : (
              <TodoItem
                key={todo.id}
                todo={todo}
                onToggle={() => toggleTodo(todo.id)}
                onEdit={() => setEditingId(todo.id)}
                onDelete={() => deleteTodo(todo.id, todo.title)}
              />
            )
          )}
        </section>
      </main>

      <footer className="app-footer">
        <p>Todo Application v1.0 — Spring Boot + React</p>
      </footer>
    </div>
  )
}
