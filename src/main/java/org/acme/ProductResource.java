package org.acme;

import java.net.URI;
import java.util.List;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/products")
@ApplicationScoped
public class ProductResource {

    @Inject
    Template dashboard;

    @Inject
    Template product_form;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response products(@CookieParam("username") String username) {
        if (username == null || !username.equals("admin")) {
            return Response.seeOther(URI.create("/login")).build();
        }
        List<Product> productList = Product.listAll();
        TemplateInstance template = dashboard.data("products", productList).data("username", username);
        return Response.ok(template).build();
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    public Response newProductForm(@CookieParam("username") String username) {
        if (username == null || !username.equals("admin")) {
            return Response.seeOther(URI.create("/login")).build();
        }
        Product product = new Product();
        TemplateInstance template = product_form.data("product", product).data("username", username).data("isNew", true);
        return Response.ok(template).build();
    }

    @POST
    @Path("/save")
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response saveProduct(@CookieParam("username") String username,
                               @FormParam("id") String id,
                               @FormParam("name") String name,
                               @FormParam("description") String description,
                               @FormParam("price") double price,
                               @FormParam("quantity") int quantity) {
        if (username == null || !username.equals("admin")) {
            return Response.seeOther(URI.create("/login")).build();
        }
        Product product;
        if (id != null && !id.trim().isEmpty()) {
            product = Product.findById(Long.valueOf(id));
            if (product == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } else {
            product = new Product();
        }
        product.name = name;
        product.description = description;
        product.price = price;
        product.quantity = quantity;
        product.persist();
        return Response.seeOther(URI.create("/products")).build();
    }

    @GET
    @Path("/edit/{id}")
    @Produces(MediaType.TEXT_HTML)
    public Response editProductForm(@PathParam("id") Long id, @CookieParam("username") String username) {
        if (username == null || !username.equals("admin")) {
            return Response.seeOther(URI.create("/login")).build();
        }
        Product product = Product.findById(id);
        TemplateInstance template = product_form.data("product", product).data("username", username).data("isNew", false);
        return Response.ok(template).build();
    }

    @POST
    @Path("/delete/{id}")
    @Transactional
    public Response deleteProduct(@PathParam("id") Long id, @CookieParam("username") String username) {
        if (username == null || !username.equals("admin")) {
            return Response.seeOther(URI.create("/login")).build();
        }
        Product.deleteById(id);
        return Response.seeOther(URI.create("/products")).build();
    }
}
