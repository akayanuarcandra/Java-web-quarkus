package org.acme;

import java.net.URI;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

@Path("/logout")
public class LogoutResource {

    @GET
    public Response logout() {
        return Response.seeOther(URI.create("/")).cookie(
                new NewCookie.Builder("username").path("/").maxAge(0).httpOnly(true).build())
                .build();
    }
}
