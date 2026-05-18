import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { rfqApi } from '../api'
import { useToast } from '../ToastContext'

const SUPPLIERS = [
  { id: 1, label: 'admin_buyer (Buyer)' },
  { id: 2, label: 'supplier_alpha' },
  { id: 3, label: 'supplier_beta' },
  { id: 4, label: 'supplier_gamma' },
  { id: 5, label: 'supplier_delta' },
]

function addHours(h) {
  const d = new Date(Date.now() + h * 3600000)
  return d.toISOString().slice(0, 16)
}

export default function CreateRfq() {
  const toast    = useToast()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [form, setForm] = useState({
    title: '',
    description: '',
    createdById: 1,
    scheduledCloseTime: addHours(2),
    hardCloseTime: addHours(4),
    extensionType: 'TIME_BASED',
    extensionTriggerMins: 5,
    extensionDurationMins: 5,
    itemName: '',
    quantity: '',
    unit: '',
    basePrice: '',
    currency: 'INR',
  })

  const set = (key, val) => setForm(f => ({ ...f, [key]: val }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.title) return toast('Title is required', 'error')
    if (new Date(form.hardCloseTime) <= new Date(form.scheduledCloseTime)) {
      return toast('Hard close time must be after scheduled close time', 'error')
    }
    setLoading(true)
    try {
      const payload = {
        ...form,
        extensionTriggerMins: Number(form.extensionTriggerMins),
        extensionDurationMins: Number(form.extensionDurationMins),
        quantity: form.quantity ? Number(form.quantity) : null,
        basePrice: form.basePrice ? Number(form.basePrice) : null,
        scheduledCloseTime: new Date(form.scheduledCloseTime).toISOString().replace('Z', ''),
        hardCloseTime: new Date(form.hardCloseTime).toISOString().replace('Z', ''),
      }
      const res = await rfqApi.create(payload)
      toast(`RFQ ${res.data.rfqNumber} created successfully!`, 'success')
      navigate(`/auction/${res.data.id}`)
    } catch (err) {
      toast(err.response?.data?.error || 'Failed to create RFQ', 'error')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ maxWidth: 780, margin: '0 auto' }}>
      <div className="page-header">
        <h1 className="page-title">Create New RFQ</h1>
        <p className="page-subtitle">Configure auction parameters, item details, and deadline extension rules</p>
      </div>

      <form onSubmit={handleSubmit}>
        {/* ── Basic Info ── */}
        <div className="card" style={{ marginBottom: 20 }}>
          <div className="card-header"><h2 style={{ fontSize:'1rem', fontWeight:700 }}>📋 Basic Information</h2></div>
          <div className="card-body" style={{ display:'flex', flexDirection:'column', gap:16 }}>
            <div className="form-group">
              <label className="form-label">RFQ Title *</label>
              <input className="form-control" value={form.title} onChange={e => set('title', e.target.value)} placeholder="e.g. Steel Pipes Grade-A — 500 Units" required />
            </div>
            <div className="form-group">
              <label className="form-label">Description</label>
              <textarea className="form-control" value={form.description} onChange={e => set('description', e.target.value)} placeholder="Detailed procurement requirements..." />
            </div>
            <div className="form-group">
              <label className="form-label">Created By *</label>
              <select className="form-control" value={form.createdById} onChange={e => set('createdById', Number(e.target.value))}>
                {SUPPLIERS.map(s => <option key={s.id} value={s.id}>{s.label}</option>)}
              </select>
            </div>
          </div>
        </div>

        {/* ── Item Details ── */}
        <div className="card" style={{ marginBottom: 20 }}>
          <div className="card-header"><h2 style={{ fontSize:'1rem', fontWeight:700 }}>📦 Item Details</h2></div>
          <div className="card-body" style={{ display:'flex', flexDirection:'column', gap:16 }}>
            <div className="form-group">
              <label className="form-label">Item Name</label>
              <input className="form-control" value={form.itemName} onChange={e => set('itemName', e.target.value)} placeholder="e.g. Steel Pipe 6 inch Grade-A" />
            </div>
            <div className="form-grid-3">
              <div className="form-group">
                <label className="form-label">Quantity</label>
                <input className="form-control" type="number" min="0" step="0.001" value={form.quantity} onChange={e => set('quantity', e.target.value)} placeholder="500" />
              </div>
              <div className="form-group">
                <label className="form-label">Unit</label>
                <input className="form-control" value={form.unit} onChange={e => set('unit', e.target.value)} placeholder="Units / Kg / Litres" />
              </div>
              <div className="form-group">
                <label className="form-label">Currency</label>
                <select className="form-control" value={form.currency} onChange={e => set('currency', e.target.value)}>
                  {['INR','USD','EUR','GBP'].map(c => <option key={c}>{c}</option>)}
                </select>
              </div>
            </div>
            <div className="form-group">
              <label className="form-label">Base / Reserve Price</label>
              <input className="form-control" type="number" min="0" step="0.01" value={form.basePrice} onChange={e => set('basePrice', e.target.value)} placeholder="12000" />
            </div>
          </div>
        </div>

        {/* ── Timing ── */}
        <div className="card" style={{ marginBottom: 20 }}>
          <div className="card-header"><h2 style={{ fontSize:'1rem', fontWeight:700 }}>⏱ Auction Timing</h2></div>
          <div className="card-body" style={{ display:'flex', flexDirection:'column', gap:16 }}>
            <div className="form-grid">
              <div className="form-group">
                <label className="form-label">Scheduled Close *</label>
                <input className="form-control" type="datetime-local" value={form.scheduledCloseTime} onChange={e => set('scheduledCloseTime', e.target.value)} required />
              </div>
              <div className="form-group">
                <label className="form-label">Hard Close (never extended) *</label>
                <input className="form-control" type="datetime-local" value={form.hardCloseTime} onChange={e => set('hardCloseTime', e.target.value)} required />
              </div>
            </div>
            <div style={{ fontSize:'0.8rem', color:'var(--text-muted)', background:'rgba(245,158,11,0.07)', border:'1px solid rgba(245,158,11,0.2)', borderRadius:'var(--radius-sm)', padding:'10px 14px' }}>
              ⚠️ The <strong>hard close time</strong> is an absolute deadline. The system will never extend beyond it regardless of bid activity.
            </div>
          </div>
        </div>

        {/* ── Extension Rules ── */}
        <div className="card" style={{ marginBottom: 28 }}>
          <div className="card-header"><h2 style={{ fontSize:'1rem', fontWeight:700 }}>🔁 Bid Extension Rules</h2></div>
          <div className="card-body" style={{ display:'flex', flexDirection:'column', gap:16 }}>
            <div className="form-group">
              <label className="form-label">Extension Trigger Type</label>
              <select className="form-control" value={form.extensionType} onChange={e => set('extensionType', e.target.value)}>
                <option value="TIME_BASED">Time-Based — bid within X minutes of close</option>
                <option value="RANK_BASED">Rank-Based — bid changes the L1 supplier</option>
                <option value="COMBINED">Combined — both conditions required</option>
              </select>
            </div>
            <div className="form-grid">
              <div className="form-group">
                <label className="form-label">Trigger Window (minutes)</label>
                <input className="form-control" type="number" min="1" max="60" value={form.extensionTriggerMins} onChange={e => set('extensionTriggerMins', e.target.value)} />
                <span className="form-error" style={{ color:'var(--text-muted)' }}>Bid within this many mins of close triggers extension</span>
              </div>
              <div className="form-group">
                <label className="form-label">Extension Duration (minutes)</label>
                <input className="form-control" type="number" min="1" max="60" value={form.extensionDurationMins} onChange={e => set('extensionDurationMins', e.target.value)} />
                <span className="form-error" style={{ color:'var(--text-muted)' }}>How many minutes to add when triggered</span>
              </div>
            </div>
          </div>
        </div>

        <div style={{ display:'flex', gap:12, justifyContent:'flex-end' }}>
          <button type="button" className="btn btn-ghost" onClick={() => navigate('/')}>Cancel</button>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? '⏳ Creating...' : '🚀 Launch Auction'}
          </button>
        </div>
      </form>
    </div>
  )
}
