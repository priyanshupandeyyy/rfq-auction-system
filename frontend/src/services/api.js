import axios from 'axios';

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    const msg = err.response?.data?.message || err.message || 'Network error';
    return Promise.reject(new Error(msg));
  }
);

export const createRfq = (data) => api.post('/api/rfq/create', data).then(r => r.data);
export const getAllRfqs = () => api.get('/api/rfq/all').then(r => r.data);
export const getActiveRfqs = () => api.get('/api/rfq/active').then(r => r.data);
export const getRfqById = (id) => api.get(`/api/rfq/${id}`).then(r => r.data);
export const closeRfq = (id) => api.put(`/api/rfq/close/${id}`).then(r => r.data);
export const submitBid = (data) => api.post('/api/bid/submit', data).then(r => r.data);
export const getBids = (rfqId) => api.get(`/api/bid/${rfqId}`).then(r => r.data);
export const getRankings = (rfqId) => api.get(`/api/ranking/${rfqId}`).then(r => r.data);
export const getActivity = (rfqId) => api.get(`/api/activity/${rfqId}`).then(r => r.data);

export default api;
