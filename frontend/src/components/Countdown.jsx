import React, { useEffect, useState } from 'react'

export default function Countdown({ scheduledCloseTime, status }) {
  const [seconds, setSeconds] = useState(0)

  useEffect(() => {
    const calc = () => {
      const diff = Math.max(0, Math.floor((new Date(scheduledCloseTime) - Date.now()) / 1000))
      setSeconds(diff)
    }
    calc()
    if (status !== 'ACTIVE') return
    const id = setInterval(calc, 1000)
    return () => clearInterval(id)
  }, [scheduledCloseTime, status])

  if (status !== 'ACTIVE') return <span className="badge badge-closed">Closed</span>
  if (seconds === 0)       return <span className="countdown urgent">Expired</span>

  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  const pad = n => String(n).padStart(2, '0')
  const urgent = seconds < 300  // last 5 min

  return (
    <span className={`countdown${urgent ? ' urgent' : ''}`}>
      {h > 0 && `${pad(h)}:`}{pad(m)}:{pad(s)}
    </span>
  )
}
