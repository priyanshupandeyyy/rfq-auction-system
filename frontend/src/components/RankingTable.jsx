import { format } from 'date-fns';

const rankBadgeClass = (rank) => {
  if (rank === 1) return 'rank-badge rank-badge-1';
  if (rank === 2) return 'rank-badge rank-badge-2';
  if (rank === 3) return 'rank-badge rank-badge-3';
  return 'rank-badge rank-badge-n';
};

const rankRowStyle = (rank) => {
  if (rank === 1) return { background: 'rgba(0,229,160,0.04)', borderLeft: '3px solid rgba(0,229,160,0.4)' };
  if (rank === 2) return { background: 'rgba(79,142,247,0.04)', borderLeft: '3px solid rgba(79,142,247,0.4)' };
  if (rank === 3) return { background: 'rgba(255,140,66,0.04)', borderLeft: '3px solid rgba(255,140,66,0.4)' };
  return {};
};

export default function RankingTable({ rankings = [], currency = 'INR' }) {
  if (!rankings.length) {
    return (
      <div className="empty-state">
        <div className="empty-icon">📊</div>
        <div className="empty-title">No bids yet</div>
        <div className="empty-desc">Rankings will appear once suppliers submit bids</div>
      </div>
    );
  }

  return (
    <div className="table-wrapper">
      <table>
        <thead>
          <tr>
            <th>Rank</th>
            <th>Supplier</th>
            <th>Company</th>
            <th>Bid Amount</th>
            <th>Submitted At</th>
          </tr>
        </thead>
        <tbody>
          {rankings.map((r) => (
            <tr key={r.supplierId} style={rankRowStyle(r.rank)}>
              <td>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <span className={rankBadgeClass(r.rank)}>{r.label}</span>
                </div>
              </td>
              <td style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{r.supplierName}</td>
              <td style={{ color: 'var(--text-secondary)' }}>{r.supplierCompany || '—'}</td>
              <td>
                <span style={{
                  fontSize: 16, fontWeight: 700,
                  color: r.rank === 1 ? 'var(--accent-green)' :
                         r.rank === 2 ? 'var(--accent-blue)' :
                         r.rank === 3 ? 'var(--accent-orange)' : 'var(--text-primary)'
                }}>
                  {r.currency || currency} {Number(r.amount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                </span>
              </td>
              <td style={{ color: 'var(--text-muted)', fontSize: 13 }}>
                {r.submittedAt ? format(new Date(r.submittedAt), 'MMM d, HH:mm:ss') : '—'}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
