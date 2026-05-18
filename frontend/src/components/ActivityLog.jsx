import { formatDistanceToNow, format } from 'date-fns';

const EVENT_STYLES = {
  BID_SUBMITTED:    { icon: '💰', color: 'var(--accent-blue)',   bg: 'rgba(79,142,247,0.12)' },
  AUCTION_EXTENDED: { icon: '⏰', color: 'var(--accent-orange)', bg: 'rgba(255,140,66,0.12)' },
  AUCTION_CLOSED:   { icon: '🔒', color: 'var(--accent-red)',    bg: 'rgba(255,77,109,0.12)' },
  AUCTION_CREATED:  { icon: '🚀', color: 'var(--accent-green)',  bg: 'rgba(0,229,160,0.12)' },
  AUCTION_ACTIVATED:{ icon: '▶',  color: 'var(--accent-green)',  bg: 'rgba(0,229,160,0.12)' },
  RANK_CHANGED:     { icon: '📈', color: 'var(--accent-purple)', bg: 'rgba(155,89,252,0.12)' },
  EXTENSION_BLOCKED:{ icon: '🚫', color: 'var(--accent-red)',    bg: 'rgba(255,77,109,0.12)' },
};

const DEFAULT_STYLE = { icon: '📋', color: 'var(--text-secondary)', bg: 'rgba(138,146,176,0.12)' };

export default function ActivityLog({ logs = [] }) {
  if (!logs.length) {
    return (
      <div className="empty-state">
        <div className="empty-icon">📜</div>
        <div className="empty-title">No activity yet</div>
        <div className="empty-desc">Events will appear here as the auction progresses</div>
      </div>
    );
  }

  return (
    <div className="activity-list">
      {logs.map((log) => {
        const style = EVENT_STYLES[log.eventType] || DEFAULT_STYLE;
        const ts = log.createdAt ? new Date(log.createdAt) : null;
        return (
          <div className="activity-item" key={log.id}>
            <div
              className="activity-icon"
              style={{ background: style.bg, color: style.color }}
            >
              {style.icon}
            </div>
            <div className="activity-content" style={{ flex: 1 }}>
              <div className="event-type" style={{ color: style.color }}>{log.eventType}</div>
              <div className="event-desc">{log.description}</div>
              {ts && (
                <div className="event-time" title={format(ts, 'PPpp')}>
                  {formatDistanceToNow(ts, { addSuffix: true })}
                </div>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
}
