import React from 'react'
import { NavLink } from 'react-router-dom'

export default function Navbar() {
  return (
    <nav className="navbar">
      <NavLink to="/" className="navbar-brand">
        ⚡ RFQ Auction <span className="badge">LIVE</span>
      </NavLink>
      <div className="navbar-links">
        <NavLink to="/"         end   className={({ isActive }) => isActive ? 'active' : ''}>Dashboard</NavLink>
        <NavLink to="/create"         className={({ isActive }) => isActive ? 'active' : ''}>+ Create RFQ</NavLink>
      </div>
    </nav>
  )
}
