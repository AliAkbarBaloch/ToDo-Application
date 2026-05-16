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

test('TodoItem renders priority badge with correct label', () => {
  const todo = { ...baseTodo, priority: 'HIGH' }
  render(<TodoItem todo={todo} onToggle={() => {}} onEdit={() => {}} onDelete={() => {}} />)

  expect(screen.getByText('High')).toBeInTheDocument()
})
