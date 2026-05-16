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

// ── filter buttons ────────────────────────────────────────────────────────────

test('FilterBar calls onFilterChange with correct key when a filter button is clicked', async () => {
  const onFilterChange = vi.fn()
  render(<FilterBar {...defaultProps} onFilterChange={onFilterChange} />)

  await userEvent.click(screen.getByRole('button', { name: /active/i }))

  expect(onFilterChange).toHaveBeenCalledWith('active')
})
