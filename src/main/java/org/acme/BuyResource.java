package org.acme;

import java.net.URI;
import java.util.List;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/buy")
public class BuyResource {

    @Inject
    Template buy;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getBuyPage(@CookieParam("username") String username) {
        if (username == null || username.isEmpty()) {
            return Response.seeOther(URI.create("/login")).build();
        }
        List<Product> products = Product.listAll();
        TemplateInstance template = buy.data("products", products).data("username", username);
        return Response.ok(template).build();
    }
}
