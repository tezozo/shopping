import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { getCarts, createCart, deleteCart } from '../api/carts'

export default function CartList({ owner }) {
  const [carts, setCarts] = useState([])
  const [newCartName, setNewCartName] = useState('')
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()

  useEffect(() => {
    getCarts(owner)
      .then(({ data }) => setCarts(data))
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [owner])

  const handleCreate = async (e) => {
    e.preventDefault()
    if (!newCartName.trim()) return
    const { data } = await createCart(owner, newCartName.trim())
    setCarts([...carts, data])
    setNewCartName('')
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this cart?')) return
    await deleteCart(id)
    setCarts(carts.filter(c => c.id !== id))
  }

  if (loading) return <div className="loading">Loading...</div>

  return (
    <div className="cart-list">
      <h2>My Shopping Carts</h2>

      <form className="create-form" onSubmit={handleCreate}>
        <input
          type="text"
          placeholder="New cart name"
          value={newCartName}
          onChange={(e) => setNewCartName(e.target.value)}
        />
        <button type="submit">Create Cart</button>
      </form>

      {carts.length === 0 ? (
        <p className="empty">No carts yet. Create one above.</p>
      ) : (
        <div className="cart-cards">
          {carts.map(cart => (
            <div key={cart.id} className="cart-card">
              <div className="cart-card-info">
                <strong>{cart.name}</strong>
                <span>{cart.items?.length ?? 0} item(s)</span>
              </div>
              <div className="cart-card-actions">
                <button onClick={() => navigate(`/cart/${cart.id}`)}>Open</button>
                <button className="danger" onClick={() => handleDelete(cart.id)}>Delete</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
