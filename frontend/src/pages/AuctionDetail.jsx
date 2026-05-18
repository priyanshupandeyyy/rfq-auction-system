import React, { useEffect, useState, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { rfqApi, bidApi, activityApi } from '../api'
import Countdown from '../components/Countdown'
import { useToast } from '../ToastContext'
import { format } from 'date-fns'

const SUPPLIERS = [
  { id: 2, label: 'supplier_alpha — Alpha Supplies Ltd' },
  { id: 3, label: 'supplier_beta — Beta Trading Co' },
  { id: 4, label: 'supplier_gamma — Gamma Logistics' },
  { id: 5, label: 'supplier_delta — Delta Enterprises' },
]

function fmtCurrency(val, currency) {
  if (val == null) return '—'
  return new Intl.NumberFormat('en-IN', { style: 'currency', currency: currency || 'INR', maximumFractionDigits: 2 }).format(val)
}

function fmtDate(dt) {
  if (!dt) return '—'
  return format(new Date(dt), 'dd MMM yyyy, HH:mm:ss')
}

function getActivityIcon(eventType) {
  if (eventType?.includes('CREATED'))  return { cls: 'created',  icon: '🔔' }
  if (eventType?.includes('BID'))      return { cls: 'bid',      icon: '💰' }
  if (eventType?.includes('EXTENDED')) return { cls: 'extended', icon: '⏰' }
  if (eventType?.includes('CLOSED'))   return { cls: 'closed',   icon: '🔒' }
  return { cls: 'default', icon: '📝' }
}

function RankBadge({ rank }) {
  if (rank === 1) return <span className="badge badge-l1">L1</span>
  if (rank === 2) return <span className="badge badge-l2">L2</span>
  if (rank === 3) return <span className="badge badge-l3">L3</span>
  return <span className="badge badge-rank">L{rank}</span>
}

export default function AuctionDetail() {
  const { id }   = useParams()
  const navigate  = useNavigate()
  const toast     = useToast()

  const [auction,   setAuction]   = useState(null)
  const [rankings,  setRankings]  = useState([])
  const [allBids,   setAllBids]   = useState([])
  const [activity,  setActivity]  = useState([])
  const [tab,       setTab]       = useState('rankings')
  const [loading,   setLoading]   = useState(true)
  const [closing,   setClosing]   = useState(false)

  const [bidForm, setBidForm] = useState({ supplierId: 2, bidAmount: '', remarks: '' })
  const [bidding, setBidding] = useState(false)

  const fetchAll = useCallback(async () => {
    try {
      const [aRes, rRes, bRes, aLog] = await Promise.all([
        rfqApi.getById(id),
        bidApi.getRankings(id),
        bidApi.getByRfq(id),
        activityApi.getByRfq(id),
      ])
      setAuction(aRes.data)
      setRankings(rRes.data)
      setAllBids(bRes.data)
      setActivity(aLog.data)
    } catch (e) {
      console.error(e)
    } finally {
      setLoading(false)
    }
  }, [id])

  useEffect(() => {
    fetchAll()
    const intervalId = setInterval(fetchAll, 5000)
    return () => clearInterval(intervalId)
  }, [fetchAll])

  const handleBid = async (e) => {
    e.preventDefault()
    if (!bidForm.bidAmount || Number(bidForm.bidAmount) <= 0) return toast('Enter a valid bid amount', 'error')
    setBidding(true)
    try {
      await bidApi.submit({
        auctionId: Number(id),
        supplierId: Number(bidForm.supplierId),
        bidAmount: Number(bidForm.bidAmount),
        remarks: bidForm.remarks,
      })
      toast('Bid submitted successfully!', 'success')
      setBidForm(f => ({ ...f, bidAmount: '', remarks: '' }))
      fetchAll()
    } catch (err) {
      toast(err.response?.data?.error || 'Failed to submit bid', 'error')
    } finally {
      setBidding(false)
    }
  }

  const handleClose = async () => {
    if (!window.confirm('Force-close this auction?')) return
    setClosing(true)
    try {
      await rfqApi.close(id)
      toast('Auction closed successfully', 'info')
      fetchAll()
    } catch (err) {
      toast(err.response?.data?.error || 'Failed to close auction', 'error')
    } finally {
      setClosing(false)
    }
  }

  if (loading) return <div className="loader-wrap"><div className="spinner" /></div>
  if (!auction) return <div className="empty-state"><h3>Auction not found</h3></div>

  const isActive = auction.status === 'ACTIVE'

  return (
    <div>
      {/* ── Header ── */}
      <div style={{ display:'flex', alignItems:'flex-start', justifyContent:'space-between', flexWrap:'wrap', gap:12, marginBottom:24 }}>
        <div>
          <div style={{ display:'flex', alignItems:'center', gap:10, marginBottom:6 }}>
            <button className="btn btn-ghost btn-sm" onClick={() => navigate('/')}>← Back</button>
            <span className="mono" style={{ color:'var(--accent-light)' }}>{auction.rfqNumber}</span>
            <span className={`badge badge-${auction.status.toLowerCase()}`}>
              <span className={`dot${isActive ? ' pulse' : ''}`} />{auction.status}
            </span>
          </div>
          <h1 className="page-title" style={{ fontSize:'1.35rem' }}>{auction.title}</h1>
          {auction.description && <p style={{ color:'var(--text-secondary)', marginTop:4, fontSize:'0.875rem' }}>{auction.description}</p>}
        </div>
        {isActive && (
          <button className="btn btn-danger btn-sm" onClick={handleClose} disabled={closing}>
            {closing ? '...' : '🔒 Force Close'}
          </button>
        )}
      </div>

      {/* ── Stat Ribbon ── */}
      <div className="stats-grid" style={{ marginBottom:24 }}>
        <div className="stat-card">
          <span className="stat-label">Time Remaining</span>
          <Countdown scheduledCloseTime={auction.scheduledCloseTime} status={auction.status} />
        </div>
        <div className="stat-card">
          <span className="stat-label">L1 Price</span>
          <span className="stat-value green" style={{ fontSize:'1.3rem' }}>{fmtCurrency(auction.currentL1Price, auction.currency)}</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">Total Bids</span>
          <span className="stat-value accent">{auction.totalBids}</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">Suppliers</span>
          <span className="stat-value">{auction.totalSuppliers}</span>
        </div>
      </div>

      <div style={{ display:'grid', gridTemplateColumns:'1fr 320px', gap:20, alignItems:'start' }}>

        {/* ── Left: Rankings + Tabs ── */}
        <div>
          {/* Top-3 Ranking Cards */}
          {rankings.length > 0 && (
            <div className="rankings-grid" style={{ marginBottom:24 }}>
              {rankings.slice(0, 3).map(r => {
                const cls = r.rank === 1 ? 'l1' : r.rank === 2 ? 'l2' : 'l3'
                return (
                  <div key={r.supplierId} className={`ranking-card ${cls}`}>
                    <div className="ranking-label">{'L' + r.rank + (r.rank === 1 ? ' — Winner' : '')}</div>
                    <div className="ranking-amount">{fmtCurrency(r.bidAmount, auction.currency)}</div>
                    <div className="ranking-company">{r.supplierCompany || r.supplierUsername}</div>
                    <div className="ranking-time">{fmtDate(r.bidTime)}</div>
                  </div>
                )
              })}
            </div>
          )}

          {/* Tabs: Rankings | All Bids | Activity */}
          <div className="tabs">
            {[['rankings','🏆 Rankings'],['bids','📊 All Bids'],['activity','📜 Activity']].map(([k,label]) => (
              <button key={k} className={`tab-btn${tab === k ? ' active' : ''}`} onClick={() => setTab(k)}>{label}</button>
            ))}
          </div>

          {tab === 'rankings' && (
            <div className="card">
              <div className="card-header"><span style={{ fontWeight:700 }}>Live Bid Rankings</span></div>
              {rankings.length === 0 ? (
                <div className="empty-state" style={{ padding:'40px 24px' }}><h3>No bids yet</h3></div>
              ) : (
                <div className="table-wrapper">
                  <table>
                    <thead><tr>
                      <th>Rank</th><th>Supplier</th><th>Company</th><th>Bid Amount</th><th>Submitted At</th><th>Remarks</th>
                    </tr></thead>
                    <tbody>
                      {rankings.map(r => (
                        <tr key={r.supplierId}>
                          <td><RankBadge rank={r.rank} /></td>
                          <td className="mono">{r.supplierUsername}</td>
                          <td style={{ color:'var(--text-secondary)' }}>{r.supplierCompany}</td>
                          <td style={{ color:'var(--green)', fontWeight:700, fontFamily:'var(--font-mono)' }}>{fmtCurrency(r.bidAmount, auction.currency)}</td>
                          <td style={{ color:'var(--text-muted)', fontSize:'0.8rem' }}>{fmtDate(r.bidTime)}</td>
                          <td style={{ color:'var(--text-secondary)', fontSize:'0.8rem' }}>{r.remarks || '—'}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}

          {tab === 'bids' && (
            <div className="card">
              <div className="card-header"><span style={{ fontWeight:700 }}>All Bid History</span><span style={{ fontSize:'0.8rem', color:'var(--text-muted)' }}>{allBids.length} bids total</span></div>
              <div className="table-wrapper">
                <table>
                  <thead><tr>
                    <th>Supplier</th><th>Amount</th><th>Time</th><th>Status</th><th>Rank at Submit</th><th>Remarks</th>
                  </tr></thead>
                  <tbody>
                    {allBids.map(b => (
                      <tr key={b.id}>
                        <td className="mono">{b.supplierUsername}</td>
                        <td style={{ fontFamily:'var(--font-mono)', color: b.isLatest ? 'var(--green)' : 'var(--text-muted)' }}>{fmtCurrency(b.bidAmount, auction.currency)}</td>
                        <td style={{ fontSize:'0.78rem', color:'var(--text-muted)' }}>{fmtDate(b.bidTime)}</td>
                        <td>{b.isLatest ? <span className="badge badge-active">Active</span> : <span className="badge badge-closed">Superseded</span>}</td>
                        <td>{b.rankAtSubmit ? <RankBadge rank={b.rankAtSubmit} /> : '—'}</td>
                        <td style={{ fontSize:'0.8rem', color:'var(--text-secondary)' }}>{b.remarks || '—'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {tab === 'activity' && (
            <div className="card">
              <div className="card-header"><span style={{ fontWeight:700 }}>Activity Log</span></div>
              <div className="card-body">
                <div className="activity-list">
                  {activity.map(log => {
                    const { cls, icon } = getActivityIcon(log.eventType)
                    return (
                      <div key={log.id} className="activity-item">
                        <div className={`activity-icon ${cls}`}>{icon}</div>
                        <div className="activity-body">
                          <div className="activity-desc">{log.description}</div>
                          <div className="activity-time">{fmtDate(log.createdAt)} · {log.actorUsername}</div>
                        </div>
                      </div>
                    )
                  })}
                  {activity.length === 0 && <div style={{ textAlign:'center', color:'var(--text-muted)', padding:'32px 0' }}>No activity yet</div>}
                </div>
              </div>
            </div>
          )}
        </div>

        {/* ── Right: Bid Form + Auction Info ── */}
        <div style={{ display:'flex', flexDirection:'column', gap:16 }}>

          {/* Bid Form */}
          {isActive && (
            <div className="card">
              <div className="card-header"><span style={{ fontWeight:700 }}>💰 Submit Bid</span></div>
              <form className="card-body" onSubmit={handleBid} style={{ display:'flex', flexDirection:'column', gap:14 }}>
                <div className="form-group">
                  <label className="form-label">Supplier</label>
                  <select className="form-control" value={bidForm.supplierId} onChange={e => setBidForm(f => ({ ...f, supplierId: e.target.value }))}>
                    {SUPPLIERS.map(s => <option key={s.id} value={s.id}>{s.label}</option>)}
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Bid Amount ({auction.currency})</label>
                  <input className="form-control" type="number" min="0.01" step="0.01" placeholder="Enter your bid" value={bidForm.bidAmount} onChange={e => setBidForm(f => ({ ...f, bidAmount: e.target.value }))} required />
                  {auction.currentL1Price && (
                    <span style={{ fontSize:'0.75rem', color:'var(--text-muted)', marginTop:4 }}>
                      Current L1: {fmtCurrency(auction.currentL1Price, auction.currency)} — bid lower to lead
                    </span>
                  )}
                </div>
                <div className="form-group">
                  <label className="form-label">Remarks (optional)</label>
                  <textarea className="form-control" style={{ minHeight:60 }} placeholder="e.g. ISO certified, on-time delivery..." value={bidForm.remarks} onChange={e => setBidForm(f => ({ ...f, remarks: e.target.value }))} />
                </div>
                <button type="submit" className="btn btn-primary" disabled={bidding} style={{ width:'100%' }}>
                  {bidding ? '⏳ Submitting...' : '🚀 Place Bid'}
                </button>
              </form>
            </div>
          )}

          {/* Auction Info */}
          <div className="card">
            <div className="card-header"><span style={{ fontWeight:700 }}>ℹ️ Auction Details</span></div>
            <div className="card-body" style={{ display:'flex', flexDirection:'column', gap:12 }}>
              {[
                ['Item', auction.itemName],
                ['Quantity', auction.quantity ? `${auction.quantity} ${auction.unit || ''}` : null],
                ['Base Price', fmtCurrency(auction.basePrice, auction.currency)],
                ['Extension', auction.extensionType?.replace('_',' ')],
                ['Trigger', `${auction.extensionTriggerMins} min window`],
                ['Duration', `+${auction.extensionDurationMins} mins`],
                ['Hard Close', fmtDate(auction.hardCloseTime)],
                ['Created By', auction.createdByCompany || auction.createdByUsername],
              ].filter(([,v]) => v).map(([k,v]) => (
                <div key={k} style={{ display:'flex', justifyContent:'space-between', gap:8 }}>
                  <span style={{ fontSize:'0.78rem', color:'var(--text-muted)', fontWeight:600 }}>{k}</span>
                  <span style={{ fontSize:'0.82rem', color:'var(--text-primary)', textAlign:'right' }}>{v}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
