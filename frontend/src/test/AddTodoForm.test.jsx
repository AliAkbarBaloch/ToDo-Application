import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import AddTodoForm from '../components/AddTodoForm'

// ── due date warning ──────────────────────────────────────────────────────────

test('AddTodoForm shows past-date warning only after date field loses focus', async () => {
  render(<AddTodoForm onSubmit={vi.fn()} />)
  const dateInput = screen.getByLabelText(/due date/i)

  await userEvent.type(dateInput, '2020-01-01')
  expect(screen.queryByText(/due date is in the past/i)).not.toBeInTheDocument()

  await userEvent.tab()
  expect(screen.getByText(/due date is in the past/i)).toBeInTheDocument()
})

// ── successful submit ─────────────────────────────────────────────────────────

test('AddTodoForm calls onSubmit with trimmed title and selected priority', async () => {
  const onSubmit = vi.fn().mockResolvedValue(undefined)
  render(<AddTodoForm onSubmit={onSubmit} />)

  await userEvent.type(screen.getByLabelText(/task title/i), '  Buy milk  ')
  await userEvent.selectOptions(screen.getByLabelText(/priority/i), 'HIGH')
  await userEvent.click(screen.getByRole('button', { name: /add task/i }))

  expect(onSubmit).toHaveBeenCalledWith(
    expect.objectContaining({ title: 'Buy milk', priority: 'HIGH' })
  )
})

// ── validation ────────────────────────────────────────────────────────────────

test('AddTodoForm shows error and does not submit when title is blank', async () => {
  const onSubmit = vi.fn()
  render(<AddTodoForm onSubmit={onSubmit} />)

  await userEvent.click(screen.getByRole('button', { name: /add task/i }))

  expect(screen.getByText('Title is required')).toBeInTheDocument()
  expect(onSubmit).not.toHaveBeenCalled()
})
