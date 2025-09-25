package org.acme;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;

@Path("/register")
public class RegisterResource {

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response register(@FormParam("username") String username, @FormParam("password") String password) {
        User user = new User();
        user.username = username;
        user.password = password;
        user.persist();
        return Response.status(Response.Status.FOUND)
                .location(URI.create("/login.html"))
                .build();
    }
}
