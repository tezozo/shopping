import { useState, useEffect } from 'react'
import { getOwners } from '../api/carts'

export default function OwnerInput({ onSetOwner }) {
  const [name, setName] = useState('')
  const [owners, setOwners] = useState([])

  useEffect(() => {
    getOwners().then(res => setOwners(res.data)).catch(() => {})
  }, [])

  const handleSubmit = (e) => {
    e.preventDefault()
    if (name.trim()) onSetOwner(name.trim())
  }

  return (
    <div className="owner-input">
      <h2>Welcome to Shopping Cart</h2>
      <form onSubmit={handleSubmit}>
        <input
          type="text"
          list="owners-list"
          placeholder="Enter your username"
          value={name}
          onChange={(e) => setName(e.target.value)}
          autoFocus
        />
        <datalist id="owners-list">
          {owners.map(o => <option key={o} value={o} />)}
        </datalist>
        <button type="submit">Continue</button>
      </form>
    </div>
  )
}
