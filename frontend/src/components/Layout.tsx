import { NavLink, Outlet } from 'react-router-dom';

const navItems = [
  { to: '/', label: 'Početna', end: true },
  { to: '/inventory', label: 'Magacin' },
  { to: '/suppliers', label: 'Dobavljači' },
  { to: '/work-orders', label: 'Radni nalozi' },
  { to: '/procurement-inquiries', label: 'Upiti' },
  { to: '/procurement-optimization', label: 'Optimizacija nabavke' },
  { to: '/purchase-requests', label: 'Zahtevi za nabavku' },
];

export function Layout() {
  return (
    <div className="app-shell">
      <header className="app-header">
        <span className="app-header-brand">MPO</span>
      </header>

      <div className="app-title">Manufacturing Procurement Optimization</div>

      <div className="app-frame">
        <nav className="nav-underline">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) => (isActive ? 'active' : undefined)}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <main style={{ padding: '1rem' }}>
          <Outlet />
        </main>
      </div>
    </div>
  );
}
