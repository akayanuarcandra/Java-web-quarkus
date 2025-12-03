package org.acme;

import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/whoami")
public class DebugResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response whoami(@CookieParam("username") String username) {
        if (username == null) {
            return Response.ok("<no username cookie sent>").build();
        }
        return Response.ok(username).build();
    }
}
