async function postJson(url, body) {
  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  return res.json();
}

async function postForm(url, formData) {
  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams(formData)
  });
  return res.json();
}

// Add to cart buttons on products page
document.querySelectorAll('.add-to-cart-btn').forEach(btn => {
  btn.addEventListener('click', async (e) => {
    const id = btn.getAttribute('data-id');
    if (!id) return;
    try {
      // try to read the product name from the card to show a friendly message
      let productName = 'Item';
      try {
        const card = btn.closest('.p-4') || btn.closest('.card') || btn.parentElement;
        const titleEl = card ? card.querySelector('h2') : null;
        if (titleEl && titleEl.textContent) productName = titleEl.textContent.trim();
      } catch (e) {
        // ignore, fallback to generic 'Item'
      }
      const res = await postJson(`/cart/add/${id}`, {});
      if (res && res.success) {
        alert(productName + ' added to cart');
        btn.textContent = 'Added';
        setTimeout(() => btn.textContent = 'Add to Cart', 1000);
      } else if (res && res.message === 'not_logged_in') {
        alert('Please log in to add items to your cart');
        window.location.href = '/login';
      } else {
        alert('Failed to add item to cart: ' + (res && res.message ? res.message : 'unknown'));
      }
    } catch (err) {
      console.error('Add to cart failed', err);
      alert('Add to cart failed: ' + (err && err.message ? err.message : 'network error'));
    }
  });
});

// Cart page behaviour
function updateGrandTotal() {
  let total = 0;
  document.querySelectorAll('.cart-check').forEach(cb => {
    if (!cb.checked) return;
    const id = cb.getAttribute('data-id');
    const qtyEl = document.querySelector(`.qty-input[data-id="${id}"]`);
    const subtotalEl = document.querySelector(`.subtotal[data-id="${id}"]`);
    const qty = parseInt(qtyEl.value || '0', 10);
    const subtotal = parseFloat(subtotalEl.textContent || '0');
    total += subtotal;
  });
  document.getElementById('grand-total').textContent = total.toFixed(2);
}
// helper to update quantity on server and refresh UI
async function updateQuantityOnServer(id, qty, input) {
  // clamp to input min/max if present
  const min = parseInt(input.getAttribute('min') || '1', 10);
  const max = parseInt(input.getAttribute('max') || '1000', 10);
  qty = Math.max(min, Math.min(max, qty));
  const res = await postForm(`/cart/update/${id}`, { quantity: qty });
  if (res && res.success) {
    input.value = qty;
    const priceCell = input.closest('tr').querySelector('td:nth-child(4)');
    const price = parseFloat(priceCell.textContent.replace(/[¥, ]/g, '')) || 0;
    const subtotalEl = document.querySelector(`.subtotal[data-id="${id}"]`);
    subtotalEl.textContent = (price * qty).toFixed(2);
    updateGrandTotal();
  }
  return res;
}

document.addEventListener('click', async (e) => {
  // increase quantity
  if (e.target.matches('.qty-incr')) {
    const id = e.target.getAttribute('data-id');
    const input = document.querySelector(`.qty-input[data-id="${id}"]`);
    let qty = parseInt(input.value || '0', 10);
    const max = parseInt(input.getAttribute('max') || '1000', 10);
    if (qty >= max) {
      // already at or above max; clamp and do nothing
      input.value = max;
      return;
    }
    qty = Math.min(max, qty + 1);
    await updateQuantityOnServer(id, qty, input);
  }

  // decrease quantity
  if (e.target.matches('.qty-decr')) {
    const id = e.target.getAttribute('data-id');
    const input = document.querySelector(`.qty-input[data-id="${id}"]`);
    let qty = parseInt(input.value || '0', 10);
    const min = parseInt(input.getAttribute('min') || '1', 10);
    qty = Math.max(min, qty - 1);
    await updateQuantityOnServer(id, qty, input);
  }

  // delete
  if (e.target.matches('.delete-item')) {
    const id = e.target.getAttribute('data-id');
    const res = await postJson(`/cart/delete/${id}`, {});
    if (res && res.success) {
      // remove row
      const row = e.target.closest('tr');
      row.parentNode.removeChild(row);
      updateGrandTotal();
    }
  }
});

// clamp input-typed values and persist when user changes quantity manually
document.addEventListener('change', async (e) => {
  if (e.target.matches('.qty-input')) {
    const input = e.target;
    const id = input.getAttribute('data-id');
    let qty = parseInt(input.value || '0', 10);
    const min = parseInt(input.getAttribute('min') || '1', 10);
    const max = parseInt(input.getAttribute('max') || '1000', 10);
    if (isNaN(qty)) qty = min;
    qty = Math.max(min, Math.min(max, qty));
    await updateQuantityOnServer(id, qty, input);
  }
});

// checkbox changes
document.addEventListener('change', (e) => {
  if (e.target.matches('.cart-check')) {
    updateGrandTotal();
  }
});

// toggle check all
const checkAllBtn = document.getElementById('check-all');
if (checkAllBtn) {
  checkAllBtn.addEventListener('click', () => {
    const checks = document.querySelectorAll('.cart-check');
    const anyUnchecked = Array.from(checks).some(c => !c.checked);
    checks.forEach(c => c.checked = anyUnchecked);
    updateGrandTotal();
  });
}

// buy selected
const buyBtn = document.getElementById('buy-selected');
if (buyBtn) {
  buyBtn.addEventListener('click', async () => {
    const checked = Array.from(document.querySelectorAll('.cart-check')).filter(c => c.checked).map(c => Number(c.getAttribute('data-id')));
    if (checked.length === 0) {
      alert('No items selected');
      return;
    }
    const res = await postJson('/cart/buy', checked);
    if (res && res.success) {
      alert('Purchase successful');
      window.location.reload();
    } else {
      alert('Purchase failed: ' + (res ? res.message : 'unknown'));
    }
  });
}

// ensure initial grand total calculation
setTimeout(updateGrandTotal, 50);
