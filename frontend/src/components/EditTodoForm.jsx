import { useState } from 'react'

/**
 * US-04: Inline edit form pre-filled with the current task values.
 * MN-01: Title must not be blank — same validation rules as creation.
 * US-06: Due date editor with past-date warning.
 * US-08: Priority selector.
 */
export default function EditTodoForm({ todo, onSave, onCancel }) {
  const [title, setTitle] = useState(todo.title)
  const [description, setDescription] = useState(todo.description || '')
  const [priority, setPriority] = useState(todo.priority || 'MEDIUM')
  const [dueDate, setDueDate] = useState(todo.dueDate || '')
  const [titleError, setTitleError] = useState('')
  const [saving, setSaving] = useState(false)
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

  async function handleSave(e) {
    e.preventDefault()
    if (!validate()) return
    setSaving(true)
    try {
      await onSave({
        title: title.trim(),
        description: description.trim() || null,
        priority,
        dueDate: dueDate || null,
      })
    } catch (err) {
      if (err?.errors?.title) setTitleError(err.errors.title)
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="edit-form">
      <h3>Edit Task</h3>
      <form onSubmit={handleSave} noValidate>
        <div className="form-group">
          <label htmlFor={`edit-title-${todo.id}`}>Task Title *</label>
          <input
            id={`edit-title-${todo.id}`}
            type="text"
            value={title}
            onChange={e => { setTitle(e.target.value); if (titleError) setTitleError('') }}
            maxLength={200}
          />
          {titleError && <span className="field-error">{titleError}</span>}
        </div>

        <div className="form-group">
          <label htmlFor={`edit-desc-${todo.id}`}>Description</label>
          <textarea
            id={`edit-desc-${todo.id}`}
            value={description}
            onChange={e => setDescription(e.target.value)}
          />
        </div>

        <div className="form-row">
          <div className="form-group">
            <label htmlFor={`edit-priority-${todo.id}`}>Priority</label>
            <select
              id={`edit-priority-${todo.id}`}
              value={priority}
              onChange={e => setPriority(e.target.value)}
            >
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
            </select>
          </div>

          <div className="form-group">
            <label htmlFor={`edit-due-${todo.id}`}>Due Date</label>
            <input
              id={`edit-due-${todo.id}`}
              type="date"
              value={dueDate}
              onChange={e => { setDueDate(e.target.value); setDueDateBlurred(false) }}
              onBlur={() => setDueDateBlurred(true)}
            />
            {dueDateInPast && dueDateBlurred && (
              <span className="due-date-warning">⚠ Due date is in the past — are you sure?</span>
            )}
          </div>
        </div>

        <div className="button-group">
          <button type="submit" className="btn-primary btn-save" disabled={saving}>
            {saving ? 'Saving…' : 'Save'}
          </button>
          <button type="button" className="btn-secondary" onClick={onCancel}>
            Cancel
          </button>
        </div>
      </form>
    </div>
  )
}
