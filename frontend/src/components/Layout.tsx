import { NavLink, Outlet } from 'react-router-dom';

const navItems = [
  { to: '/', label: 'Početna', end: true },
  { to: '/reference-data', label: 'Šifarnici' },
  { to: '/suppliers', label: 'Dobavljači' },
  { to: '/inventory', label: 'Magacin' },
  { to: '/work-orders', label: 'Radni nalozi' },
  { to: '/purchase-requests', label: 'Zahtevi za nabavku' },
  { to: '/procurement-optimization', label: 'Optimizacija nabavke' },
];

export function Layout() {
  return (
    <div>
      <nav style={{ display: 'flex', gap: '1rem', padding: '1rem', borderBottom: '1px solid #ccc' }}>
        {navItems.map((item) => (
          <NavLink key={item.to} to={item.to} end={item.end}>
            {item.label}
          </NavLink>
        ))}
      </nav>
      <main style={{ padding: '1rem' }}>
        <Outlet />
      </main>
    </div>
  );
}
