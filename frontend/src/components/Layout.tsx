import { NavLink, Outlet } from 'react-router-dom';

const navItems = [
  { to: '/', label: 'Početna', end: true },
  { to: '/suppliers', label: 'Dobavljači' },
  { to: '/inventory', label: 'Magacin' },
  { to: '/work-orders', label: 'Radni nalozi' },
  { to: '/purchase-requests', label: 'Zahtevi za nabavku' },
  { to: '/procurement-optimization', label: 'Optimizacija nabavke' },
];

export function Layout() {
  return (
    <div>
      {/* privremena header slika - placeholder dok ne stigne prava, videti komentar u razgovoru */}
      <div style={{ height: '140px', overflow: 'hidden' }}>
        <img
          src="/header.jpg"
          alt=""
          style={{ width: '100%', height: '100%', objectFit: 'cover', display: 'block' }}
        />
      </div>
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
  );
}
