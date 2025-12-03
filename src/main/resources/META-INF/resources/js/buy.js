document.addEventListener('DOMContentLoaded', function() {
  document.querySelectorAll('.buy-now-btn').forEach(function(btn){
    btn.addEventListener('click', function(evt){
      const button = evt.currentTarget;
      const id = button.dataset.id;
      const name = button.dataset.name;
      if (!window.confirm('Do you want to buy ' + name + '?')) return;
      fetch('/products/buy/' + encodeURIComponent(id), {method: 'POST', headers: {'Accept':'application/json'}})
        .then(function(r){
          const contentType = r.headers.get('content-type') || '';
          if (contentType.includes('application/json')) {
            return r.json().then(function(body){ return {status: r.status, body: body}; });
          }
          return r.text().then(function(text){ return {status: r.status, body: {error: text || 'Unexpected response'}}; });
        })
        .then(function(res){
           const status = res.status;
           const body = res.body;
           if (status === 200 && body && body.success) {
               const qtyEl = document.getElementById('qty-' + id);
               if (qtyEl) qtyEl.textContent = body.quantity + ' in stock';
                 if (body.quantity <= 0) {
                   // Replace the buttons container with a single disabled 'Out of Stock' button
                   const container = button.closest('.p-4').querySelector('.buttons-container');
                   const outBtn = document.createElement('button');
                   outBtn.className = 'bg-gray-400 text-white font-bold py-2 px-4 rounded cursor-not-allowed';
                   outBtn.disabled = true;
                   outBtn.textContent = 'Out of Stock';
                   if (container && container.parentNode) {
                     container.parentNode.replaceChild(outBtn, container);
                   } else {
                     // fallback: just disable clicked button and remove sibling
                     button.disabled = true;
                     button.textContent = 'Out of Stock';
                     const sibling = button.closest('.p-4').querySelector('.add-to-cart-btn');
                     if (sibling) sibling.remove();
                   }
                 }
               window.alert('Purchase successful');
           } else if (body && body.error) {
               window.alert(body.error);
           } else {
               window.alert('Purchase failed');
           }
        }).catch(function(e){ console.error(e); window.alert('Error performing purchase'); });
    });
  });
});
