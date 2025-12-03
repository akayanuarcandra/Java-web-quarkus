package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@ApplicationScoped
public class CartService {

    @Transactional
    public int addItem(String username, long productId) {
        CartEntry entry = CartEntry.findByUserAndProduct(username, productId);
        if (entry == null) {
            entry = new CartEntry();
            entry.username = username;
            entry.productId = productId;
            entry.quantity = 1;
            entry.persist();
            return 1;
        }
        entry.quantity = entry.quantity + 1;
        entry.persist();
        return entry.quantity;
    }

    @Transactional
    public int updateQuantity(String username, long productId, int quantity) {
        CartEntry entry = CartEntry.findByUserAndProduct(username, productId);
        if (entry == null) {
            if (quantity <= 0) return 0;
            entry = new CartEntry();
            entry.username = username;
            entry.productId = productId;
            entry.quantity = quantity;
            entry.persist();
            return quantity;
        }
        if (quantity <= 0) {
            entry.delete();
            return 0;
        }
        entry.quantity = quantity;
        entry.persist();
        return entry.quantity;
    }

    @Transactional
    public void removeItem(String username, long productId) {
        CartEntry.delete("username = ?1 and productId = ?2", username, productId);
    }

    public Map<Long, Integer> getCart(String username) {
        List<CartEntry> list = CartEntry.list("username", username);
        Map<Long, Integer> map = new HashMap<>();
        for (CartEntry e : list) {
            map.put(e.productId, e.quantity);
        }
        return map;
    }

    @Transactional
    public void clearItems(String username, Iterable<Long> productIds) {
        for (Long id : productIds) {
            CartEntry.delete("username = ?1 and productId = ?2", username, id);
        }
    }

    @Transactional
    public double purchaseItems(String username, Iterable<Long> productIds) {
        double total = 0.0;
        // track purchased ids to remove from cart after successful decrement
        List<Long> purchased = new ArrayList<>();
        for (Long id : productIds) {
            CartEntry entry = CartEntry.findByUserAndProduct(username, id);
            if (entry == null || entry.quantity <= 0) continue;
            Product p = Product.findById(id);
            if (p == null) continue;
            int qty = entry.quantity;
            if (p.quantity >= qty) {
                p.quantity = p.quantity - qty;
                p.persist();
                total += p.price * qty;
                purchased.add(id);
            }
        }
        // remove purchased items from cart
        for (Long id : purchased) {
            CartEntry.delete("username = ?1 and productId = ?2", username, id);
        }
        return total;
    }
}
