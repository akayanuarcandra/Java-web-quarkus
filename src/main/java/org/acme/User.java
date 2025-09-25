package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends PanacheEntity {

    public String username;
    public String password;

    public static User findByUsername(String username) {
        return find("username", username).firstResult();
    }
}