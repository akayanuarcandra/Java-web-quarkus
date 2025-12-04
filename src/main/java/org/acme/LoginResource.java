package org.acme;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

@Path("/login")
public class LoginResource {

    @Inject
    Template login;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getLoginPage() {
        return login.data("title", "Login", "username", null, "error", null);
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response login(@FormParam("username") String username, @FormParam("password") String password,
            @FormParam("remember") String remember) {
        User user = User.find("username", username).firstResult();
        if (user != null && user.password.equals(password)) {
            String redirectPath = username.equals("admin") ? "/dashboard" : "/";
            boolean rememberMe = remember != null && (remember.equalsIgnoreCase("on") || remember.equalsIgnoreCase("true"));
            int maxAgeSeconds = rememberMe ? 30 * 24 * 60 * 60 : 60 * 60;
            return Response.status(Response.Status.FOUND)
                    .location(java.net.URI.create(redirectPath))
                    .cookie(new NewCookie.Builder("username")
                        .value(URLEncoder.encode(username, StandardCharsets.UTF_8))
                        .path("/")
                        .maxAge(maxAgeSeconds)
                        .httpOnly(true)
                        .build())
                    .build();
        } else {
            return Response.ok(login.data("error", "Invalid credentials", "username", null)).status(Response.Status.UNAUTHORIZED).build();
        }
    }
}
