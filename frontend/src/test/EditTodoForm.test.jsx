import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import EditTodoForm from '../components/EditTodoForm'

const todo = {
  id: 5,
  title: 'Existing title',
  description: 'Existing desc',
  priority: 'LOW',
  dueDate: '',
}

// ── pre-fill ──────────────────────────────────────────────────────────────────

test('EditTodoForm pre-fills inputs from todo prop', () => {
  render(<EditTodoForm todo={todo} onSave={vi.fn()} onCancel={vi.fn()} />)

  expect(screen.getByDisplayValue('Existing title')).toBeInTheDocument()
  expect(screen.getByDisplayValue('Existing desc')).toBeInTheDocument()
  expect(screen.getByDisplayValue('Low')).toBeInTheDocument()
})
