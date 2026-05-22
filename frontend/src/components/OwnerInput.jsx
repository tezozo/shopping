import { useState, useEffect, useRef } from 'react'
import { getOwners } from '../api/carts'

export default function OwnerInput({ onSetOwner }) {
  const [name, setName] = useState('')
  const [owners, setOwners] = useState([])
  const [open, setOpen] = useState(false)
  const containerRef = useRef(null)

  useEffect(() => {
    getOwners().then(res => setOwners(res.data)).catch(() => {})
  }, [])

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (containerRef.current && !containerRef.current.contains(e.target)) {
        setOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const filtered = owners.filter(o => o.toLowerCase().includes(name.toLowerCase()))

  const select = (owner) => {
    setName(owner)
    setOpen(false)
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    if (name.trim()) onSetOwner(name.trim())
  }

  return (
    <div className="owner-input">
      <h2>Welcome to Shopping Cart</h2>
      <form onSubmit={handleSubmit}>
        <div className="combobox" ref={containerRef}>
          <input
            type="text"
            placeholder="Enter your username"
            value={name}
            onChange={(e) => { setName(e.target.value); setOpen(true) }}
            onFocus={() => setOpen(true)}
            autoFocus
          />
          {owners.length > 0 && (
            <button
              type="button"
              className="combobox-arrow"
              onClick={() => setOpen(o => !o)}
              tabIndex={-1}
            >▾</button>
          )}
          {open && filtered.length > 0 && (
            <ul className="combobox-list">
              {filtered.map(o => (
                <li key={o} onMouseDown={() => select(o)}>{o}</li>
              ))}
            </ul>
          )}
        </div>
        <button type="submit">Continue</button>
      </form>
    </div>
  )
}
