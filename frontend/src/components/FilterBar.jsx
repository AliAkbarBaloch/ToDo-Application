/**
 * US-07: Filter buttons (All / Active / Completed) with per-status counts.
 * US-09: Keyword search input.
 */
export default function FilterBar({ filter, onFilterChange, search, onSearchChange, counts }) {
  const filters = [
    { key: 'all', label: 'All' },
    { key: 'active', label: 'Active' },
    { key: 'completed', label: 'Completed' },
  ]

  return (
    <div className="filters-section">
      <div className="search-box">
        <span className="search-icon">🔍</span>
        <input
          type="text"
          placeholder="Search tasks…"
          value={search}
          onChange={e => onSearchChange(e.target.value)}
          aria-label="Search tasks"
        />
      </div>

      <div className="filter-buttons">
        {filters.map(f => (
          <button
            key={f.key}
            className={`filter-btn${filter === f.key ? ' active' : ''}`}
            onClick={() => onFilterChange(f.key)}
          >
            {f.label}
            {counts[f.key] != null ? ` (${counts[f.key]})` : ''}
          </button>
        ))}
      </div>
    </div>
  )
}
