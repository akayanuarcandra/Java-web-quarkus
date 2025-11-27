package org.acme;

import java.net.URI;

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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/simple/products")
public class SimpleProductResource {

    @Inject
    Template simple_product_form;

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance newProductForm(@Context HttpHeaders headers) {
        Cookie usernameCookie = headers.getCookies().get("username");
        String username = usernameCookie != null ? usernameCookie.getValue() : null;
        return simple_product_form.data("username", username);
    }

    @POST
    @Path("/save")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response saveProduct(@FormParam("name") String name, @FormParam("description") String description) {
        Product product = new Product();
        product.name = name;
        product.description = description;
        product.persist();
        return Response.status(302).location(URI.create("/dashboard")).build();
    }
}
