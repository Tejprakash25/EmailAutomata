import { NavLink } from 'react-router-dom';

/**
 * Primary navigation for the authenticated workspace. Items are added as their
 * features land; the active link takes the signal accent.
 */
const ITEMS = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/templates', label: 'Templates' },
];

export default function WorkspaceNav() {
  return (
    <nav
      style={{
        display: 'flex',
        gap: 'var(--ea-space-2)',
        marginBottom: 'var(--ea-space-8)',
        borderBottom: '1px solid var(--ea-line)',
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