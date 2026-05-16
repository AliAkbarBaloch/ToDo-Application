import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import TodoItem from '../components/TodoItem'

const baseTodo = {
  id: 1,
  title: 'Test task',
  description: 'Some description',
  completed: false,
  priority: 'MEDIUM',
  dueDate: null,
  createdAt: '2026-01-01T10:00:00',
}

// ── renders fields ────────────────────────────────────────────────────────────

test('TodoItem renders title and description', () => {
  render(<TodoItem todo={baseTodo} onToggle={() => {}} onEdit={() => {}} onDelete={() => {}} />)

  expect(screen.getByText('Test task')).toBeInTheDocument()
  expect(screen.getByText('Some description')).toBeInTheDocument()
})

test('TodoItem calls onToggle when checkbox is clicked', async () => {
  const onToggle = vi.fn()
  render(<TodoItem todo={baseTodo} onToggle={onToggle} onEdit={() => {}} onDelete={() => {}} />)

  await userEvent.click(screen.getByRole('checkbox'))

  expect(onToggle).toHaveBeenCalledTimes(1)
})

test('TodoItem calls onEdit when Edit button is clicked', async () => {
  const onEdit = vi.fn()
  render(<TodoItem todo={baseTodo} onToggle={() => {}} onEdit={onEdit} onDelete={() => {}} />)

  await userEvent.click(screen.getByRole('button', { name: /edit/i }))

  expect(onEdit).toHaveBeenCalledTimes(1)
})

test('TodoItem calls onDelete when Delete button is clicked', async () => {
  const onDelete = vi.fn()
  render(<TodoItem todo={baseTodo} onToggle={() => {}} onEdit={() => {}} onDelete={onDelete} />)

  await userEvent.click(screen.getByRole('button', { name: /delete/i }))

  expect(onDelete).toHaveBeenCalledTimes(1)
})

test('TodoItem renders priority badge with correct label', () => {
  const todo = { ...baseTodo, priority: 'HIGH' }
  render(<TodoItem todo={todo} onToggle={() => {}} onEdit={() => {}} onDelete={() => {}} />)

  expect(screen.getByText('High')).toBeInTheDocument()
})

test('TodoItem shows overdue label when due date is in the past', () => {
  const todo = { ...baseTodo, dueDate: '2020-01-01' }
  render(<TodoItem todo={todo} onToggle={() => {}} onEdit={() => {}} onDelete={() => {}} />)

  expect(screen.getByText(/overdue by/i)).toBeInTheDocument()
})

test('TodoItem shows Due Today label when due date is today', () => {
  const today = new Date().toISOString().split('T')[0]
  const todo = { ...baseTodo, dueDate: today }
  render(<TodoItem todo={todo} onToggle={() => {}} onEdit={() => {}} onDelete={() => {}} />)

  expect(screen.getByText(/Due Today/)).toBeInTheDocument()
})

test('TodoItem shows future due date label when due date is tomorrow', () => {
  const tomorrow = new Date(Date.now() + 86400000).toISOString().split('T')[0]
  const todo = { ...baseTodo, dueDate: tomorrow }
  render(<TodoItem todo={todo} onToggle={() => {}} onEdit={() => {}} onDelete={() => {}} />)

  expect(screen.getByText(new RegExp(`Due ${tomorrow}`))).toBeInTheDocument()
})
