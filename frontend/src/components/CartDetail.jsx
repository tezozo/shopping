import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getCart, updateCart } from '../api/carts'
import { getItems } from '../api/items'
import { getCategoryDiscounts } from '../api/categoryDiscounts'

export default function CartDetail() {
  const { id } = useParams()
  const navigate = useNavigate()

  const [cart, setCart] = useState(null)
  const [cartItems, setCartItems] = useState([])
  const [availableItems, setAvailableItems] = useState([])
  const [categoryDiscounts, setCategoryDiscounts] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState(null)

  useEffect(() => {
    Promise.all([getCart(id), getItems(), getCategoryDiscounts()])
      .then(([cartRes, itemsRes, cdRes]) => {
        setCart(cartRes.data)
        setCartItems(cartRes.data.items ?? [])
        setAvailableItems(itemsRes.data)
        setCategoryDiscounts(cdRes.data)
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
    setSaveError(null)
    try {
      await updateCart(id, {
        name: cart.name,
        owner: cart.owner,
        items: cartItems.map(ci => ({ itemId: ci.item.id, units: ci.units }))
      })
    } catch (err) {
      setSaveError(err.response?.data?.error ?? 'Failed to save cart')
    } finally {
      setSaving(false)
    }
  }

  // Best applicable discount for an item given current date/time.
  // Cumulative discounts stack; non-cumulative apply alone.
  // Returns the better of (sum of cumulative) vs (best non-cumulative).
  const resolveDiscount = (item) => {
    const now = new Date()
    const itemCatIds = new Set((item.categories ?? []).map(c => c.id))

    const active = categoryDiscounts
      .filter(cd => itemCatIds.has(cd.category.id))
      .map(cd => cd.discount)
      .filter(d => new Date(d.start) <= now && now <= new Date(d.end))

    if (active.length === 0) return { pct: 0, labels: [] }

    const cumulative    = active.filter(d => d.cumulative)
    const nonCumulative = active.filter(d => !d.cumulative)

    const cumulativePct = cumulative.reduce((s, d) => s + parseFloat(d.percent), 0)
    const bestNC = nonCumulative.reduce(
      (best, d) => parseFloat(d.percent) > best.pct ? { pct: parseFloat(d.percent), labels: [d.name] } : best,
      { pct: 0, labels: [] }
    )

    return cumulativePct >= bestNC.pct
      ? { pct: cumulativePct, labels: cumulative.map(d => d.name) }
      : bestNC
  }

  // Per-line numbers: discount first, then tax on the net (discounted) amount.
  const breakdown = (ci) => {
    const base = parseFloat(ci.item.unitPrice) * ci.units
    const { pct: discountPct, labels: discountLabels } = resolveDiscount(ci.item)
    const discountAmount  = base * discountPct / 100
    const net             = base - discountAmount          // Net = discounted base, before tax
    const taxes           = (ci.item.taxes ?? []).filter(t => parseFloat(t.percent) > 0)
    const taxAmount       = taxes.reduce((s, t) => s + net * parseFloat(t.percent) / 100, 0)
    return { base, discountPct, discountAmount, discountLabels, net, taxAmount, total: net + taxAmount }
  }

  const breakdowns = cartItems.map(ci => ({ ci, ...breakdown(ci) }))

  // Aggregate each tax type across all lines
  const taxTotals = {}
  breakdowns.forEach(({ ci, net }) => {
    ;(ci.item.taxes ?? []).filter(t => parseFloat(t.percent) > 0).forEach(t => {
      if (!taxTotals[t.id]) taxTotals[t.id] = { tax: t, amount: 0 }
      taxTotals[t.id].amount += net * parseFloat(t.percent) / 100
    })
  })

  const subtotal     = breakdowns.reduce((s, b) => s + b.net, 0)
  const totalTax     = Object.values(taxTotals).reduce((s, { amount }) => s + amount, 0)
  const grandTotal   = subtotal + totalTax
  const totalSavings = breakdowns.reduce((s, b) => s + b.discountAmount, 0)

  if (loading) return <div className="loading">Loading...</div>
  if (!cart)   return <div className="loading">Cart not found.</div>

  return (
    <div className="cart-detail">
      <div className="cart-header">
        <div className="cart-header-left">
          <button className="secondary" onClick={() => navigate('/')}>Cancel</button>
        </div>
        <button onClick={handleSave} disabled={saving}>
          {saving ? 'Saving…' : 'Save Cart'}
        </button>
      </div>
      <h2>{cart.name}</h2>
      {saveError && <div className="save-error">{saveError}</div>}

      <section>
        <h3>Items in Cart</h3>
        {cartItems.length === 0 ? (
          <p className="empty">No items yet — add from the list below.</p>
        ) : (
          <>
            <table>
              <thead>
                <tr>
                  <th>Item</th>
                  <th>Unit Price</th>
                  <th>Discount</th>
                  <th>Applicable Taxes</th>
                  <th>Units</th>
                  <th>Net</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {breakdowns.map(({ ci, discountPct, discountAmount, discountLabels, net, total }) => (
                  <tr key={ci.item.id}>
                    <td>{ci.item.name}</td>
                    <td>${parseFloat(ci.item.unitPrice).toFixed(2)}</td>
                    <td className={discountPct > 0 ? 'discount-cell' : ''}>
                      {discountPct > 0 ? (
                        <>
                          <div className="discount-names">{discountLabels.join(', ')}</div>
                          <div>{`−${discountPct.toFixed(2)}% (−$${discountAmount.toFixed(2)})`}</div>
                        </>
                      ) : '—'}
                    </td>
                    <td className="taxes-cell">
                      {(ci.item.taxes ?? []).map(t => `${t.name} ${parseFloat(t.percent)}%`).join(', ') || '—'}
                    </td>
                    <td>
                      <input
                        type="number"
                        min="0"
                        value={ci.units}
                        onChange={(e) => setUnits(ci.item.id, parseInt(e.target.value) || 0)}
                      />
                    </td>
                    <td>${net.toFixed(2)}</td>
                    <td>
                      <button className="danger" onClick={() => setUnits(ci.item.id, 0)}>Remove</button>
                    </td>
                  </tr>
                ))}
              </tbody>
              <tfoot>
                <tr>
                  <td colSpan="5">Subtotal (after discounts, before tax)</td>
                  <td>${subtotal.toFixed(2)}</td>
                  <td></td>
                </tr>
                {Object.values(taxTotals).map(({ tax, amount }) => (
                  <tr key={tax.id}>
                    <td colSpan="5">{tax.name} ({parseFloat(tax.percent)}%)</td>
                    <td>${amount.toFixed(2)}</td>
                    <td></td>
                  </tr>
                ))}
                <tr className="grand-total-row">
                  <td colSpan="5"><strong>Grand Total</strong></td>
                  <td><strong>${grandTotal.toFixed(2)}</strong></td>
                  <td></td>
                </tr>
              </tfoot>
            </table>

            {totalSavings > 0 && (
              <div className="savings-banner">
                You save: ${totalSavings.toFixed(2)}
              </div>
            )}
          </>
        )}
      </section>

      <section>
        <h3>Available Items</h3>
        {availableItems.length === 0 ? (
          <p className="empty">No items in catalogue.</p>
        ) : (
          <div className="item-grid">
            {availableItems.map(item => {
              const { pct: dPct, labels: dLabels } = resolveDiscount(item)
              const unitPrice     = parseFloat(item.unitPrice)
              const discountedPrice = unitPrice * (1 - dPct / 100)
              return (
                <div key={item.id} className="item-card">
                  <span className="item-name">{item.name}</span>
                  <span className="item-price">
                    {dPct > 0
                      ? <><s className="item-price-original">${unitPrice.toFixed(2)}</s> ${discountedPrice.toFixed(2)}</>
                      : `$${unitPrice.toFixed(2)}`}
                  </span>
                  {dLabels.length > 0 && (
                    <span className="item-discount-label">{dLabels.join(', ')}</span>
                  )}
                  <span className="item-meta">{(item.categories ?? []).map(c => c.name).join(', ')}</span>
                  <button onClick={() => addItem(item)}>+ Add</button>
                </div>
              )
            })}
          </div>
        )}
      </section>

    </div>
  )
}
