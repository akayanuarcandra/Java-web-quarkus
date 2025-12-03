package org.acme;

public class CartItem {
    public Long id;
    public String name;
    public String imagePath;
    public double price;
    public int quantity;

    public CartItem() {}

    public CartItem(Long id, String name, String imagePath, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.imagePath = imagePath;
        this.price = price;
        this.quantity = quantity;
    }

    public double subtotal() {
        return price * quantity;
    }
}
