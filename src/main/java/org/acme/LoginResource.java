package org.acme;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;

@Path("/login")
public class LoginResource {

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response login(@FormParam("username") String username, @FormParam("password") String password) {
        User user = User.findByUsername(username);
        if (user != null && user.password.equals(password)) {
            return Response.status(Response.Status.FOUND)
                    .location(URI.create("/index.html"))
                    .build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
}
