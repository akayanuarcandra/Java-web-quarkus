package org.acme;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/register")
public class RegisterResource {

    @Inject
    Template register;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getRegisterPage() {
        return register.data("title", "Register", "username", null);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response registerUser(@FormParam("username") String username, 
                                 @FormParam("email") String email,
                                 @FormParam("password") String password) {
        if (User.find("username", username).firstResult() != null) {
            return Response.status(Response.Status.CONFLICT).entity("Username already exists").build();
        }
        User user = new User();
        user.username = username;
        user.email = email;
        user.password = password;
        user.persist();
        return Response.status(Response.Status.FOUND).location(java.net.URI.create("/login")).build();
    }
}
