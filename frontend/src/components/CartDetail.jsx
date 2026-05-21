import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getCart, updateCart } from '../api/carts'
import { getItems } from '../api/items'

export default function CartDetail() {
  const { id } = useParams()
  const navigate = useNavigate()

  const [cart, setCart] = useState(null)
  const [cartItems, setCartItems] = useState([])
  const [availableItems, setAvailableItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    Promise.all([getCart(id), getItems()])
      .then(([cartRes, itemsRes]) => {
        setCart(cartRes.data)
        setCartItems(cartRes.data.items ?? [])
        setAvailableItems(itemsRes.data)
      })
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [id])

  const addItem = (item) => {
    setCartItems(prev => {
      const existing = prev.find(ci => ci.item.id === item.id)
      if (existing) return prev.map(ci => ci.item.id === item.id ? { ...ci, units: ci.units + 1 } : ci)
      return [...prev, { item, units: 1 }]
    })
  }

  const setUnits = (itemId, units) => {
    if (units <= 0) setCartItems(prev => prev.filter(ci => ci.item.id !== itemId))
    else setCartItems(prev => prev.map(ci => ci.item.id === itemId ? { ...ci, units } : ci))
  }

  const handleSave = async () => {
    setSaving(true)
    try {
      await updateCart(id, {
        name: cart.name,
        owner: cart.owner,
        items: cartItems.map(ci => ({ itemId: ci.item.id, units: ci.units }))
      })
    } catch (err) {
      console.error('Save failed', err)
      alert('Failed to save cart')
    } finally {
      setSaving(false)
    }
  }

  const lineTotal = (ci) => {
    const base = parseFloat(ci.item.unitPrice) * ci.units
    const tax = (ci.item.taxes ?? []).reduce((s, t) => s + base * parseFloat(t.percent) / 100, 0)
    return base + tax
  }

  const grandTotal = cartItems.reduce((s, ci) => s + lineTotal(ci), 0).toFixed(2)

  if (loading) return <div className="loading">Loading...</div>
  if (!cart) return <div className="loading">Cart not found.</div>

  return (
    <div className="cart-detail">
      <button className="secondary" onClick={() => navigate('/')}>← Back</button>
      <h2>{cart.name}</h2>

      <section>
        <h3>Items in Cart</h3>
        {cartItems.length === 0 ? (
          <p className="empty">No items yet — add from the list below.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Item</th>
                <th>Unit Price</th>
                <th>Taxes</th>
                <th>Units</th>
                <th>Line Total</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {cartItems.map(ci => (
                <tr key={ci.item.id}>
                  <td>{ci.item.name}</td>
                  <td>${parseFloat(ci.item.unitPrice).toFixed(2)}</td>
                  <td>
                    {(ci.item.taxes ?? []).map(t => `${t.name} ${t.percent}%`).join(', ') || '—'}
                  </td>
                  <td>
                    <input
                      type="number"
                      min="0"
                      value={ci.units}
                      onChange={(e) => setUnits(ci.item.id, parseInt(e.target.value) || 0)}
                    />
                  </td>
                  <td>${lineTotal(ci).toFixed(2)}</td>
                  <td>
                    <button className="danger" onClick={() => setUnits(ci.item.id, 0)}>Remove</button>
                  </td>
                </tr>
              ))}
            </tbody>
            <tfoot>
              <tr>
                <td colSpan="4">Total (incl. taxes)</td>
                <td>${grandTotal}</td>
                <td></td>
              </tr>
            </tfoot>
          </table>
        )}
      </section>

      <section>
        <h3>Available Items</h3>
        {availableItems.length === 0 ? (
          <p className="empty">No items in catalogue.</p>
        ) : (
          <div className="item-grid">
            {availableItems.map(item => (
              <div key={item.id} className="item-card">
                <span className="item-name">{item.name}</span>
                <span className="item-price">${parseFloat(item.unitPrice).toFixed(2)}</span>
                <span className="item-meta">
                  {(item.categories ?? []).map(c => c.name).join(', ')}
                </span>
                <button onClick={() => addItem(item)}>+ Add</button>
              </div>
            ))}
          </div>
        )}
      </section>

      <div className="detail-actions">
        <button onClick={handleSave} disabled={saving}>
          {saving ? 'Saving…' : 'Save Cart'}
        </button>
        <button className="secondary" onClick={() => navigate('/')}>Cancel</button>
      </div>
    </div>
  )
}
