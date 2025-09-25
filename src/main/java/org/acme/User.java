package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends PanacheEntity {

    public String username;
    public String password;
    public String email;

    public static User findByUsername(String username) {
        return find("username", username).firstResult();    // find user by username
    }

    public static User findByUsernameAndPassword(String username, String password) {
        return find("username = ?1 and password = ?2", username, password).firstResult();   // find user by username and password, ?1 and ?2 are placeholders for the parameters
    }
}