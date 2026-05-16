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

// ── validation ────────────────────────────────────────────────────────────────

test('EditTodoForm shows error and does not call onSave when title is cleared', async () => {
  const onSave = vi.fn()
  render(<EditTodoForm todo={todo} onSave={onSave} onCancel={vi.fn()} />)

  await userEvent.clear(screen.getByDisplayValue('Existing title'))
  await userEvent.click(screen.getByRole('button', { name: /save/i }))

  expect(screen.getByText('Title is required')).toBeInTheDocument()
  expect(onSave).not.toHaveBeenCalled()
})

test('EditTodoForm shows past-date warning only after date field loses focus', async () => {
  render(<EditTodoForm todo={todo} onSave={vi.fn()} onCancel={vi.fn()} />)
  const dateInput = screen.getByLabelText(/due date/i)

  await userEvent.type(dateInput, '2020-01-01')
  expect(screen.queryByText(/due date is in the past/i)).not.toBeInTheDocument()

  await userEvent.tab()
  expect(screen.getByText(/due date is in the past/i)).toBeInTheDocument()
})

// ── save / cancel ─────────────────────────────────────────────────────────────

test('EditTodoForm calls onSave with updated title when form is submitted', async () => {
  const onSave = vi.fn().mockResolvedValue(undefined)
  render(<EditTodoForm todo={todo} onSave={onSave} onCancel={vi.fn()} />)

  await userEvent.clear(screen.getByDisplayValue('Existing title'))
  await userEvent.type(screen.getByLabelText(/task title/i), 'Updated title')
  await userEvent.click(screen.getByRole('button', { name: /save/i }))

  expect(onSave).toHaveBeenCalledWith(
    expect.objectContaining({ title: 'Updated title' })
  )
})

test('EditTodoForm calls onCancel when Cancel button is clicked', async () => {
  const onCancel = vi.fn()
  render(<EditTodoForm todo={todo} onSave={vi.fn()} onCancel={onCancel} />)

  await userEvent.click(screen.getByRole('button', { name: /cancel/i }))

  expect(onCancel).toHaveBeenCalledTimes(1)
})

// ── pre-fill ──────────────────────────────────────────────────────────────────

test('EditTodoForm pre-fills inputs from todo prop', () => {
  render(<EditTodoForm todo={todo} onSave={vi.fn()} onCancel={vi.fn()} />)

  expect(screen.getByDisplayValue('Existing title')).toBeInTheDocument()
  expect(screen.getByDisplayValue('Existing desc')).toBeInTheDocument()
  expect(screen.getByDisplayValue('Low')).toBeInTheDocument()
})
