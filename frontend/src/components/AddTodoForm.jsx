import { useState } from 'react'

/**
 * US-01: Form to create a new todo task.
 * MN-01: Client-side validation — title must not be blank.
 * US-06: Optional due date input with past-date warning.
 * US-08: Priority selector (Low / Medium / High), defaults to Medium.
 */
export default function AddTodoForm({ onSubmit }) {
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [priority, setPriority] = useState('MEDIUM')
  const [dueDate, setDueDate] = useState('')
  const [titleError, setTitleError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  // MN-03: show warning only after the user has left the date field
  const [dueDateBlurred, setDueDateBlurred] = useState(false)

  const today = new Date().toISOString().split('T')[0]
  const dueDateInPast = dueDate && dueDate < today

  function validate() {
    if (!title.trim()) {
      setTitleError('Title is required')
      return false
    }
    if (title.trim().length > 200) {
      setTitleError('Title must not exceed 200 characters')
      return false
    }
    setTitleError('')
    return true
  }

  async function handleSubmit(e) {
    e.preventDefault()
    if (!validate()) return
    setSubmitting(true)
    try {
      await onSubmit({
        title: title.trim(),
        description: description.trim() || null,
        priority,
        dueDate: dueDate || null,
      })
      setTitle('')
      setDescription('')
      setPriority('MEDIUM')
      setDueDate('')
      setTitleError('')
      setDueDateBlurred(false)
    } catch (err) {
      if (err?.errors?.title) setTitleError(err.errors.title)
    } finally {
      setSubmitting(false)
    }
  }

  function handleClear() {
    setTitle('')
    setDescription('')
    setPriority('MEDIUM')
    setDueDate('')
    setTitleError('')
  }

  return (
    <section className="add-task-section">
      <h2>Add New Task</h2>
      <form onSubmit={handleSubmit} noValidate>
        <div className="form-group">
          <label htmlFor="new-title">Task Title *</label>
          <input
            id="new-title"
            type="text"
            value={title}
            onChange={(e) => {
              setTitle(e.target.value)
              if (titleError) setTitleError('')
            }}
            placeholder="What do you want to accomplish?"
            maxLength={200}
          />
          {titleError && <span className="field-error">{titleError}</span>}
        </div>

        <div className="form-group">
          <label htmlFor="new-desc">Description</label>
          <textarea
            id="new-desc"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Add more details about this task..."
          />
        </div>

        <div className="form-row">
          <div className="form-group">
            <label htmlFor="new-priority">Priority</label>
            <select
              id="new-priority"
              value={priority}
              onChange={(e) => setPriority(e.target.value)}
            >
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="new-due">Due Date</label>
            <input
              id="new-due"
              type="date"
              value={dueDate}
              onChange={(e) => {
                setDueDate(e.target.value)
                setDueDateBlurred(false)
              }}
              onBlur={() => setDueDateBlurred(true)}
            />
            {dueDateInPast && dueDateBlurred && (
              <span className="due-date-warning">⚠ Due date is in the past — are you sure?</span>
            )}
          </div>
        </div>

        <div className="button-group">
          <button type="submit" className="btn-primary" disabled={submitting}>
            {submitting ? 'Adding…' : 'Add Task'}
          </button>
          <button type="button" className="btn-secondary" onClick={handleClear}>
            Clear
          </button>
        </div>
      </form>
    </section>
  )
}
