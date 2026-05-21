import { useState } from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import CartList from './components/CartList'
import CartDetail from './components/CartDetail'
import OwnerInput from './components/OwnerInput'

export default function App() {
  const [owner, setOwner] = useState(localStorage.getItem('cartOwner') || '')

  const handleSetOwner = (name) => {
    setOwner(name)
    localStorage.setItem('cartOwner', name)
  }

  const handleClearOwner = () => {
    setOwner('')
    localStorage.removeItem('cartOwner')
  }

  if (!owner) {
    return <OwnerInput onSetOwner={handleSetOwner} />
  }

  return (
    <div className="app">
      <header>
        <h1>Shopping Cart App</h1>
        <span className="header-user">
          {owner}
          <button onClick={handleClearOwner}>Switch User</button>
        </span>
      </header>
      <main>
        <Routes>
          <Route path="/" element={<CartList owner={owner} />} />
          <Route path="/cart/:id" element={<CartDetail />} />
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </main>
    </div>
  )
}
