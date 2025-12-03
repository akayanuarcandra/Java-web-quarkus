package org.acme;

import java.net.URI;
import java.util.List;

import io.quarkus.panache.common.Sort;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.transaction.Transactional;

@Path("/products")
public class BuyResource {

    @Inject
    Template products;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getBuyPage(@CookieParam("username") String username) {
        if (username == null || username.isEmpty()) {
            return Response.seeOther(URI.create("/login")).build();
        }
        List<Product> productList = Product.listAll(Sort.by("id"));
        TemplateInstance template = products.data("products", productList).data("username", username);
        return Response.ok(template).build();
    }

    @POST
    @Path("/buy/{id}")
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    public Response buyProduct(@PathParam("id") Long id) {
        Product product = Product.findById(id);
        if (product == null) {
            String json = "{\"error\":\"not found\"}";
            return Response.status(Response.Status.NOT_FOUND).entity(json).type(MediaType.APPLICATION_JSON).build();
        }
        if (product.quantity <= 0) {
            String json = "{\"error\":\"out of stock\"}";
            return Response.status(Response.Status.BAD_REQUEST).entity(json).type(MediaType.APPLICATION_JSON).build();
        }
        product.quantity = product.quantity - 1;
        product.persist();
        String json = "{\"success\":true,\"quantity\":" + product.quantity + "}";
        return Response.ok(json).type(MediaType.APPLICATION_JSON).build();
    }
}
