import { NavLink } from 'react-router-dom';

const ITEMS = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/compose', label: 'Compose' },
  { to: '/history', label: 'History' },
  { to: '/templates', label: 'Templates' },
  { to: '/recipients', label: 'Recipients' },
];

export default function WorkspaceNav() {
  return (
    <nav
      style={{
        display: 'flex',
        gap: 'var(--ea-space-2)',
        marginBottom: 'var(--ea-space-8)',
        borderBottom: '1px solid var(--ea-line)',
        // Scroll horizontally on narrow screens rather than wrapping into an
        // uneven two-row block.
        overflowX: 'auto',
        WebkitOverflowScrolling: 'touch',
      }}
    >
      {ITEMS.map(({ to, label }) => (
        <NavLink
          key={to}
          to={to}
          style={({ isActive }) => ({
            padding: 'var(--ea-space-3) var(--ea-space-4)',
            fontSize: 'var(--ea-text-sm)',
            fontWeight: 500,
            whiteSpace: 'nowrap',
            color: isActive ? 'var(--ea-signal-600)' : 'var(--ea-ink-500)',
            borderBottom: `2px solid ${isActive ? 'var(--ea-signal-500)' : 'transparent'}`,
            marginBottom: -1,
            textDecoration: 'none',
          })}
        >
          {label}
        </NavLink>
      ))}
    </nav>
  );
}