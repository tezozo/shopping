import { useState } from 'react'

export default function OwnerInput({ onSetOwner }) {
  const [name, setName] = useState('')

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
          placeholder="Enter your username"
          value={name}
          onChange={(e) => setName(e.target.value)}
          autoFocus
        />
        <button type="submit">Continue</button>
      </form>
    </div>
  )
}
