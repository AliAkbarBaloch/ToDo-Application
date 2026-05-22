/**
 * US-02: Displays a single todo task with all its fields.
 * US-03: Checkbox to toggle completed status.
 * US-04: Edit button to open inline edit form.
 * US-05: Delete button (confirmation handled by parent via onDelete).
 * US-06: Due date with overdue/today colour indicator.
 * US-08: Priority badge with colour coding.
 */
export default function TodoItem({ todo, onToggle, onEdit, onDelete }) {
  const dueDateLabel = getDueDateLabel(todo.dueDate, todo.completed)

  return (
    <div className={`task-item${todo.completed ? ' completed' : ''}`}>
      <input
        type="checkbox"
        className="task-checkbox"
        checked={todo.completed}
        onChange={onToggle}
        aria-label={todo.completed ? 'Mark as active' : 'Mark as completed'}
      />

      <div className="task-content">
        <div className="task-header">
          <span className="task-title">{todo.title}</span>
          <span className={`priority-badge priority-${todo.priority}`}>
            {todo.priority.charAt(0) + todo.priority.slice(1).toLowerCase()}
          </span>
        </div>

        {todo.description && <div className="task-description">{todo.description}</div>}

        <div className="task-meta">
          {dueDateLabel && (
            <span className={`due-date ${dueDateLabel.cls}`}>📅 {dueDateLabel.text}</span>
          )}
          <span>📝 Created {formatDate(todo.createdAt)}</span>
        </div>
      </div>

      <div className="task-actions">
        <button className="btn-small btn-edit" onClick={onEdit}>
          Edit
        </button>
        <button className="btn-small btn-delete" onClick={onDelete}>
          Delete
        </button>
      </div>
    </div>
  )
}

function getDueDateLabel(dueDate, completed) {
  if (!dueDate) return null
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const due = new Date(dueDate)
  due.setHours(0, 0, 0, 0)
  const diff = due - today

  if (!completed && diff < 0) {
    const days = Math.round(-diff / 86400000)
    return { cls: 'overdue', text: `Overdue by ${days} day${days !== 1 ? 's' : ''}` }
  }
  if (!completed && diff === 0) {
    return { cls: 'due-today', text: 'Due Today' }
  }
  return { cls: '', text: `Due ${dueDate}` }
}

function formatDate(isoString) {
  if (!isoString) return ''
  return new Date(isoString).toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  })
}
