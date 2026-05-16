import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import AddTodoForm from '../components/AddTodoForm'

// ── validation ────────────────────────────────────────────────────────────────

test('AddTodoForm shows error and does not submit when title is blank', async () => {
  const onSubmit = vi.fn()
  render(<AddTodoForm onSubmit={onSubmit} />)

  await userEvent.click(screen.getByRole('button', { name: /add task/i }))

  expect(screen.getByText('Title is required')).toBeInTheDocument()
  expect(onSubmit).not.toHaveBeenCalled()
})
