import { useState } from 'react';
import { submitBid } from '../services/api';

const DEMO_SUPPLIERS = [
  { id: 2, name: 'Supplier Alpha', company: 'Alpha Supplies Pvt Ltd' },
  { id: 3, name: 'Supplier Beta', company: 'Beta Trading Co' },
  { id: 4, name: 'Supplier Gamma', company: 'Gamma Exports Ltd' },
];

export default function BidForm({ rfqId, currency = 'INR', onBidSubmitted }) {
  const [supplierId, setSupplierId] = useState('');
  const [amount, setAmount] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(''); setSuccess('');
    if (!supplierId || !amount) { setError('Please fill all fields.'); return; }

    setLoading(true);
    try {
      const res = await submitBid({
        rfqId: Number(rfqId),
        supplierId: Number(supplierId),
        amount: parseFloat(amount),
      });
      setSuccess(`Bid submitted! You are ranked L${res.data?.rankPosition || '?'}`);
      setAmount('');
      onBidSubmitted && onBidSubmitted();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <div className="form-group">
        <label className="form-label">Select Supplier</label>
        <select
          className="form-select"
          value={supplierId}
          onChange={e => setSupplierId(e.target.value)}
          required
        >
          <option value="">— Choose supplier —</option>
          {DEMO_SUPPLIERS.map(s => (
            <option key={s.id} value={s.id}>{s.name} ({s.company})</option>
          ))}
        </select>
      </div>

      <div className="form-group">
        <label className="form-label">Bid Amount ({currency})</label>
        <input
          type="number"
          className="form-input"
          placeholder="e.g. 150000"
          value={amount}
          onChange={e => setAmount(e.target.value)}
          min="0.01"
          step="0.01"
          required
        />
      </div>

      {error && (
        <div style={{
          padding: '10px 14px', borderRadius: 'var(--radius-sm)',
          background: 'rgba(255,77,109,0.1)', border: '1px solid rgba(255,77,109,0.3)',
          color: 'var(--accent-red)', fontSize: 13
        }}>
          ⚠ {error}
        </div>
      )}

      {success && (
        <div style={{
          padding: '10px 14px', borderRadius: 'var(--radius-sm)',
          background: 'rgba(0,229,160,0.1)', border: '1px solid rgba(0,229,160,0.3)',
          color: 'var(--accent-green)', fontSize: 13
        }}>
          ✓ {success}
        </div>
      )}

      <button type="submit" className="btn btn-primary" disabled={loading}>
        {loading ? '⏳ Submitting…' : '🏷 Submit Bid'}
      </button>
    </form>
  );
}
