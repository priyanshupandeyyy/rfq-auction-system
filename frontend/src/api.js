import axios from 'axios'

const BASE = import.meta.env.VITE_API_URL || '/api'

const api = axios.create({ baseURL: BASE, headers: { 'Content-Type': 'application/json' } })

// ── Auctions ─────────────────────────────────────────
export const rfqApi = {
  create:      (data)  => api.post('/rfq/create', data),
  getAll:      ()      => api.get('/rfq/all'),
  getActive:   ()      => api.get('/rfq/active'),
  getById:     (id)    => api.get(`/rfq/${id}`),
  close:       (id)    => api.put(`/rfq/close/${id}`),
}

// ── Bids ──────────────────────────────────────────────
export const bidApi = {
  submit:      (data)    => api.post('/bid/submit', data),
  getByRfq:   (rfqId)   => api.get(`/bid/${rfqId}`),
  getRankings: (rfqId)   => api.get(`/ranking/${rfqId}`),
}

// ── Activity ──────────────────────────────────────────
export const activityApi = {
  getByRfq: (rfqId) => api.get(`/activity/${rfqId}`),
}

export default api
