import { useNavigate } from 'react-router-dom';
import { format, formatDistanceToNow } from 'date-fns';

const statusBadge = (status) => {
  const map = {
    ACTIVE: 'badge-active',
    CLOSED: 'badge-closed',
    DRAFT: 'badge-draft',
    CANCELLED: 'badge-cancelled',
  };
  return map[status] || 'badge-draft';
};

export default function AuctionCard({ auction }) {
  const navigate = useNavigate();
  const endTime = auction.endTime ? new Date(auction.endTime) : null;
  const isUrgent = endTime && (endTime - Date.now()) < 10 * 60 * 1000;

  return (
    <div className="card" style={{ cursor: 'pointer' }} onClick={() => navigate(`/auction/${auction.id}`)}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 12 }}>
        <span className={`badge ${statusBadge(auction.status)}`}>
          <span className="badge-dot" />
          {auction.status}
        </span>
        {auction.status === 'ACTIVE' && endTime && (
          <span className={`timer${isUrgent ? ' urgent' : ''}`}>
            ⏱ {formatDistanceToNow(endTime, { addSuffix: true })}
          </span>
        )}
      </div>

      <h3 style={{ fontSize: 16, fontWeight: 700, marginBottom: 8, color: 'var(--text-primary)' }}>
        {auction.title}
      </h3>

      {auction.description && (
        <p style={{ fontSize: 13, color: 'var(--text-secondary)', marginBottom: 12, lineHeight: 1.5 }}>
          {auction.description.length > 100 ? auction.description.slice(0, 100) + '…' : auction.description}
        </p>
      )}

      <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap', marginBottom: 12 }}>
        {auction.basePrice && (
          <div>
            <div style={{ fontSize: 11, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.08em' }}>Base Price</div>
            <div style={{ fontSize: 15, fontWeight: 600, color: 'var(--text-primary)' }}>
              {auction.currency} {Number(auction.basePrice).toLocaleString()}
            </div>
          </div>
        )}
        {auction.bestBidAmount && (
          <div>
            <div style={{ fontSize: 11, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.08em' }}>Best Bid (L1)</div>
            <div style={{ fontSize: 15, fontWeight: 700, color: 'var(--accent-green)' }}>
              {auction.currency} {Number(auction.bestBidAmount).toLocaleString()}
            </div>
          </div>
        )}
        {auction.totalBids != null && (
          <div>
            <div style={{ fontSize: 11, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.08em' }}>Bids</div>
            <div style={{ fontSize: 15, fontWeight: 600, color: 'var(--accent-blue)' }}>{auction.totalBids}</div>
          </div>
        )}
      </div>

      <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
        <div style={{
          width: 24, height: 24, borderRadius: '50%',
          background: 'linear-gradient(135deg, var(--accent-blue), var(--accent-cyan))',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontSize: 10, fontWeight: 700, color: '#fff', flexShrink: 0
        }}>
          {auction.buyerName?.[0] || 'B'}
        </div>
        <span style={{ fontSize: 12, color: 'var(--text-secondary)' }}>{auction.buyerCompany || auction.buyerName}</span>
        {endTime && (
          <>
            <span style={{ color: 'var(--text-muted)' }}>·</span>
            <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>
              Closes {format(endTime, 'MMM d, HH:mm')}
            </span>
          </>
        )}
      </div>
    </div>
  );
}
