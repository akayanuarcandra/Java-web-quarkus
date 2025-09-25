package org.acme;

import java.net.URI;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/register")
public class RegisterResource {

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)    // tells quarkus to expect form data
    @Transactional  // to enable transaction management
    public Response register(@FormParam("username") String username, @FormParam("password") String password, @FormParam("email") String email) {
        User user = new User(); // create a new user instance
        user.username = username;
        user.password = password;
        user.email = email;
        user.persist(); // save the user to the database
        return Response.status(Response.Status.FOUND) 
                .location(URI.create("/login.html"))    // redirect to login page after successful registration
                .build();   // return a 302 response
    }
}
