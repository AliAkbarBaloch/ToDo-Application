import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import FilterBar from '../components/FilterBar'

const defaultProps = {
  filter: 'all',
  onFilterChange: vi.fn(),
  search: '',
  onSearchChange: vi.fn(),
  counts: { all: 5, active: 3, completed: 2 },
}

// ── search input ──────────────────────────────────────────────────────────────

test('FilterBar calls onSearchChange with typed text when search input changes', async () => {
  const onSearchChange = vi.fn()
  render(<FilterBar {...defaultProps} onSearchChange={onSearchChange} />)

  await userEvent.type(screen.getByRole('textbox', { name: /search/i }), 'a')

  expect(onSearchChange).toHaveBeenCalledWith('a')
})

// ── filter buttons ────────────────────────────────────────────────────────────

test('FilterBar calls onFilterChange with correct key when a filter button is clicked', async () => {
  const onFilterChange = vi.fn()
  render(<FilterBar {...defaultProps} onFilterChange={onFilterChange} />)

  await userEvent.click(screen.getByRole('button', { name: /active/i }))

  expect(onFilterChange).toHaveBeenCalledWith('active')
})
