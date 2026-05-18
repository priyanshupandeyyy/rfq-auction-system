import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getAllRfqs, getActiveRfqs } from '../services/api';
import AuctionCard from '../components/AuctionCard';

export default function Home() {
  const [auctions, setAuctions] = useState([]);
  const [filter, setFilter] = useState('ALL');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchAuctions = async () => {
    try {
      const res = filter === 'ACTIVE' ? await getActiveRfqs() : await getAllRfqs();
      setAuctions(res.data || []);
      setError('');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    setLoading(true);
    fetchAuctions();
    const interval = setInterval(fetchAuctions, 5000);
    return () => clearInterval(interval);
  }, [filter]);

  const active = auctions.filter(a => a.status === 'ACTIVE').length;
  const closed = auctions.filter(a => a.status === 'CLOSED').length;
  const totalBids = auctions.reduce((s, a) => s + (a.totalBids || 0), 0);

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Auction Dashboard</h1>
        <p className="page-subtitle">Live RFQ auctions — auto-refreshes every 5 seconds</p>
      </div>

      <div className="stat-row" style={{ marginBottom: 28 }}>
        <div className="stat-box">
          <div className="stat-label">Total Auctions</div>
          <div className="stat-value">{auctions.length}</div>
        </div>
        <div className="stat-box">
          <div className="stat-label" style={{ color: 'var(--accent-green)' }}>Active</div>
          <div className="stat-value" style={{ color: 'var(--accent-green)' }}>{active}</div>
        </div>
        <div className="stat-box">
          <div className="stat-label">Closed</div>
          <div className="stat-value">{closed}</div>
        </div>
        <div className="stat-box">
          <div className="stat-label">Total Bids</div>
          <div className="stat-value" style={{ color: 'var(--accent-blue)' }}>{totalBids}</div>
        </div>
      </div>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <div style={{ display: 'flex', gap: 8 }}>
          {['ALL', 'ACTIVE'].map(f => (
            <button
              key={f}
              className={`btn btn-sm ${filter === f ? 'btn-primary' : 'btn-secondary'}`}
              onClick={() => setFilter(f)}
            >
              {f === 'ALL' ? '🗂 All Auctions' : '🟢 Active Only'}
            </button>
          ))}
        </div>
        <Link to="/create" className="btn btn-primary btn-sm">+ New RFQ</Link>
      </div>

      {loading && (
        <div className="loading-spinner"><div className="spinner" /></div>
      )}

      {error && (
        <div style={{
          padding: '16px', borderRadius: 'var(--radius-sm)',
          background: 'rgba(255,77,109,0.1)', border: '1px solid rgba(255,77,109,0.3)',
          color: 'var(--accent-red)', marginBottom: 20
        }}>
          ⚠ Backend unreachable: {error}
        </div>
      )}

      {!loading && !error && auctions.length === 0 && (
        <div className="empty-state">
          <div className="empty-icon">🏷</div>
          <div className="empty-title">No auctions found</div>
          <div className="empty-desc">
            <Link to="/create" style={{ color: 'var(--accent-blue)' }}>Create your first RFQ</Link> to get started
          </div>
        </div>
      )}

      <div className="grid-auto">
        {auctions.map(a => <AuctionCard key={a.id} auction={a} />)}
      </div>
    </div>
  );
}
