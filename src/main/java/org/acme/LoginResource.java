package org.acme;

import java.net.URI;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/login")
public class LoginResource {

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)    // tells quarkus to expect form data
    @Transactional // to enable transaction management
    public Response login(@FormParam("username") String username, @FormParam("password") String password) {
        User user = User.findByUsername(username);  // fetch user from database
        if (user != null && user.password.equals(password)) {
            return Response.status(Response.Status.FOUND)   // redirect to index page after successful login
                    .location(URI.create("/index.html"))    // redirect to index page
                    .build();   // return a 302 response
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();   // return a 401 response
        }
    }
}
