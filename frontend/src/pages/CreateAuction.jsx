import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createRfq } from '../services/api';

const DEFAULT_FORM = {
  title: '',
  description: '',
  buyerId: 1,
  basePrice: '',
  currency: 'INR',
  startTime: '',
  endTime: '',
  hardCloseTime: '',
  extensionTrigger: 'TIME',
  extensionMinutes: 5,
  extensionWindowMinutes: 3,
};

const toISO = (localDatetime) => localDatetime ? new Date(localDatetime).toISOString().slice(0, 19) : '';

export default function CreateAuction() {
  const navigate = useNavigate();
  const [form, setForm] = useState(DEFAULT_FORM);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const set = (key) => (e) => setForm(f => ({ ...f, [key]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!form.title || !form.startTime || !form.endTime || !form.hardCloseTime) {
      setError('Please fill all required fields.'); return;
    }
    if (new Date(form.hardCloseTime) <= new Date(form.endTime)) {
      setError('Hard close time must be after end time.'); return;
    }

    setLoading(true);
    try {
      const payload = {
        ...form,
        buyerId: Number(form.buyerId),
        basePrice: form.basePrice ? parseFloat(form.basePrice) : null,
        extensionMinutes: Number(form.extensionMinutes),
        extensionWindowMinutes: Number(form.extensionWindowMinutes),
        startTime: toISO(form.startTime),
        endTime: toISO(form.endTime),
        hardCloseTime: toISO(form.hardCloseTime),
      };
      const res = await createRfq(payload);
      navigate(`/auction/${res.data.id}`);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: 720, margin: '0 auto' }}>
      <div className="page-header">
        <h1 className="page-title">Create RFQ Auction</h1>
        <p className="page-subtitle">Set up a new reverse auction for supplier bidding</p>
      </div>

      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
        {/* Basic Info */}
        <div className="card">
          <h2 style={{ fontSize: 16, fontWeight: 700, marginBottom: 20, color: 'var(--text-primary)' }}>📋 Basic Information</h2>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <div className="form-group">
              <label className="form-label">Title *</label>
              <input className="form-input" placeholder="e.g. Supply of Raw Chemicals Q3 2025" value={form.title} onChange={set('title')} required />
            </div>
            <div className="form-group">
              <label className="form-label">Description</label>
              <textarea className="form-textarea" placeholder="Detailed specifications, quantity, delivery terms..." value={form.description} onChange={set('description')} />
            </div>
            <div className="form-grid">
              <div className="form-group">
                <label className="form-label">Base Price (optional)</label>
                <input type="number" className="form-input" placeholder="e.g. 500000" value={form.basePrice} onChange={set('basePrice')} min="0" step="0.01" />
              </div>
              <div className="form-group">
                <label className="form-label">Currency</label>
                <select className="form-select" value={form.currency} onChange={set('currency')}>
                  <option>INR</option><option>USD</option><option>EUR</option><option>GBP</option>
                </select>
              </div>
            </div>
          </div>
        </div>

        {/* Timing */}
        <div className="card">
          <h2 style={{ fontSize: 16, fontWeight: 700, marginBottom: 20, color: 'var(--text-primary)' }}>⏰ Auction Timing</h2>
          <div className="form-grid-3">
            <div className="form-group">
              <label className="form-label">Start Time *</label>
              <input type="datetime-local" className="form-input" value={form.startTime} onChange={set('startTime')} required />
            </div>
            <div className="form-group">
              <label className="form-label">End Time *</label>
              <input type="datetime-local" className="form-input" value={form.endTime} onChange={set('endTime')} required />
            </div>
            <div className="form-group">
              <label className="form-label" style={{ color: 'var(--accent-red)' }}>Hard Close Time * 🔒</label>
              <input type="datetime-local" className="form-input" style={{ borderColor: 'rgba(255,77,109,0.4)' }} value={form.hardCloseTime} onChange={set('hardCloseTime')} required />
            </div>
          </div>
          <div style={{ marginTop: 12, padding: '10px 14px', borderRadius: 'var(--radius-sm)', background: 'rgba(255,77,109,0.06)', border: '1px solid rgba(255,77,109,0.2)', fontSize: 12, color: 'var(--text-secondary)' }}>
            🔒 <strong style={{ color: 'var(--accent-red)' }}>Hard Close</strong> is the absolute deadline — the auction will NEVER be extended beyond this time, regardless of trigger settings.
          </div>
        </div>

        {/* Extension Logic */}
        <div className="card">
          <h2 style={{ fontSize: 16, fontWeight: 700, marginBottom: 8, color: 'var(--text-primary)' }}>⚡ Bid Time Extension Logic</h2>
          <p style={{ fontSize: 13, color: 'var(--text-secondary)', marginBottom: 20 }}>
            Configure when the auction deadline should be automatically extended
          </p>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <div className="form-group">
              <label className="form-label">Extension Trigger</label>
              <select className="form-select" value={form.extensionTrigger} onChange={set('extensionTrigger')}>
                <option value="TIME">TIME — Bid placed within window of close</option>
                <option value="RANK">RANK — L1 (best) supplier changes within window</option>
                <option value="COMBINED">COMBINED — Both TIME and RANK conditions met</option>
              </select>
            </div>
            <div className="form-grid">
              <div className="form-group">
                <label className="form-label">Extension Window (minutes)</label>
                <input type="number" className="form-input" value={form.extensionWindowMinutes} onChange={set('extensionWindowMinutes')} min="1" max="60" />
                <span style={{ fontSize: 11, color: 'var(--text-muted)', marginTop: 4 }}>Bid within this many mins of close triggers extension</span>
              </div>
              <div className="form-group">
                <label className="form-label">Extension Duration (minutes)</label>
                <input type="number" className="form-input" value={form.extensionMinutes} onChange={set('extensionMinutes')} min="1" max="60" />
                <span style={{ fontSize: 11, color: 'var(--text-muted)', marginTop: 4 }}>How many minutes to add to end time</span>
              </div>
            </div>
          </div>
        </div>

        {error && (
          <div style={{ padding: '12px 16px', borderRadius: 'var(--radius-sm)', background: 'rgba(255,77,109,0.1)', border: '1px solid rgba(255,77,109,0.3)', color: 'var(--accent-red)' }}>
            ⚠ {error}
          </div>
        )}

        <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end' }}>
          <button type="button" className="btn btn-secondary" onClick={() => navigate('/')}>Cancel</button>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? '⏳ Creating…' : '🚀 Launch Auction'}
          </button>
        </div>
      </form>
    </div>
  );
}
