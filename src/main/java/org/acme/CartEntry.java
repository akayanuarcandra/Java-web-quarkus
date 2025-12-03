package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class CartEntry extends PanacheEntity {
    public String username;
    public Long productId;
    public int quantity;

    public static CartEntry findByUserAndProduct(String username, Long productId) {
        return find("username = ?1 and productId = ?2", username, productId).firstResult();
    }
}
