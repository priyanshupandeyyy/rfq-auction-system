import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { rfqApi } from '../api'
import Countdown from '../components/Countdown'
import { format } from 'date-fns'

function fmtCurrency(val, currency) {
  if (val == null) return '—'
  return new Intl.NumberFormat('en-IN', { style: 'currency', currency: currency || 'INR', maximumFractionDigits: 2 }).format(val)
}

export default function Dashboard() {
  const [auctions, setAuctions] = useState([])
  const [loading, setLoading]   = useState(true)
  const [filter, setFilter]     = useState('ALL')

  const fetchAuctions = async () => {
    try {
      const res = await rfqApi.getAll()
      setAuctions(res.data)
    } catch (e) {
      console.error(e)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchAuctions()
    const id = setInterval(fetchAuctions, 6000)
    return () => clearInterval(id)
  }, [])

  const filtered = filter === 'ALL' ? auctions : auctions.filter(a => a.status === filter)
  const activeCount = auctions.filter(a => a.status === 'ACTIVE').length
  const closedCount = auctions.filter(a => a.status === 'CLOSED').length
  const totalBids   = auctions.reduce((s, a) => s + (a.totalBids || 0), 0)

  if (loading) return <div className="loader-wrap"><div className="spinner" /></div>

  return (
    <div>
      <div className="page-header" style={{ display:'flex', alignItems:'center', justifyContent:'space-between', flexWrap:'wrap', gap:12 }}>
        <div>
          <h1 className="page-title">Auction Dashboard</h1>
          <p className="page-subtitle">Live RFQ auctions — auto-refreshes every 6 seconds</p>
        </div>
        <Link to="/create" className="btn btn-primary">+ New RFQ</Link>
      </div>

      {/* Stats Row */}
      <div className="stats-grid" style={{ marginBottom: 28 }}>
        <div className="stat-card">
          <span className="stat-label">Total Auctions</span>
          <span className="stat-value accent">{auctions.length}</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">Active</span>
          <span className="stat-value green">{activeCount}</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">Closed</span>
          <span className="stat-value">{closedCount}</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">Total Bids</span>
          <span className="stat-value amber">{totalBids}</span>
        </div>
      </div>

      {/* Filter Tabs */}
      <div className="tabs">
        {['ALL','ACTIVE','CLOSED','CANCELLED'].map(f => (
          <button key={f} className={`tab-btn${filter === f ? ' active' : ''}`} onClick={() => setFilter(f)}>{f}</button>
        ))}
      </div>

      {filtered.length === 0 ? (
        <div className="empty-state">
          <div className="icon">📋</div>
          <h3>No auctions found</h3>
          <p style={{marginTop:8}}><Link to="/create" className="btn btn-primary btn-sm" style={{marginTop:12}}>Create First RFQ</Link></p>
        </div>
      ) : (
        <div className="auction-grid">
          {filtered.map(a => (
            <Link key={a.id} to={`/auction/${a.id}`} className="auction-card">
              <div className="auction-card-header">
                <div>
                  <div className="rfq-number">{a.rfqNumber}</div>
                  <div className="auction-title">{a.title}</div>
                </div>
                <span className={`badge badge-${a.status.toLowerCase()}`}>
                  <span className={`dot${a.status === 'ACTIVE' ? ' pulse' : ''}`} />
                  {a.status}
                </span>
              </div>

              {a.itemName && <p style={{ fontSize:'0.82rem', color:'var(--text-secondary)', marginBottom:4 }}>{a.itemName}</p>}

              <div style={{ marginTop: 10 }}>
                <Countdown scheduledCloseTime={a.scheduledCloseTime} status={a.status} />
              </div>

              <div className="auction-meta">
                <div className="auction-meta-item">
                  <span className="auction-meta-key">L1 Price</span>
                  <span className="auction-meta-val price">{fmtCurrency(a.currentL1Price, a.currency)}</span>
                </div>
                <div className="auction-meta-item">
                  <span className="auction-meta-key">Bids</span>
                  <span className="auction-meta-val">{a.totalBids}</span>
                </div>
                <div className="auction-meta-item">
                  <span className="auction-meta-key">Suppliers</span>
                  <span className="auction-meta-val">{a.totalSuppliers}</span>
                </div>
                <div className="auction-meta-item">
                  <span className="auction-meta-key">Extension</span>
                  <span className="auction-meta-val" style={{fontSize:'0.75rem'}}>{a.extensionType?.replace('_',' ')}</span>
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
