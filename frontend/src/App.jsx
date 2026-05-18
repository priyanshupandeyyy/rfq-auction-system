import React from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Navbar from './components/Navbar'
import Dashboard from './pages/Dashboard'
import CreateRfq from './pages/CreateRfq'
import AuctionDetail from './pages/AuctionDetail'
import { ToastProvider } from './ToastContext'

export default function App() {
  return (
    <BrowserRouter>
      <ToastProvider>
        <div className="app-shell">
          <Navbar />
          <main className="page-content">
            <Routes>
              <Route path="/"            element={<Dashboard />} />
              <Route path="/create"      element={<CreateRfq />} />
              <Route path="/auction/:id" element={<AuctionDetail />} />
            </Routes>
          </main>
        </div>
      </ToastProvider>
    </BrowserRouter>
  )
}
