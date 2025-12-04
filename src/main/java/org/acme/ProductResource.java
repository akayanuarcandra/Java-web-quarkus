package org.acme;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import io.quarkus.panache.common.Sort;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("/dashboard")
@ApplicationScoped
public class ProductResource {

    @Inject
    Template dashboard;

    @Inject
    Template product_form;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response products(@CookieParam("username") String username, @Context UriInfo uriInfo) {
        String path = uriInfo != null ? uriInfo.getRequestUri().getPath() : "<no-uri>";
        if (username == null || !username.equals("admin")) {
            return Response.seeOther(URI.create("/login")).build();
        }
        List<Product> productList = Product.listAll(Sort.by("id"));
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
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response saveProduct(@CookieParam("username") String username, MultipartFormDataInput input) {
        if (username == null || !username.equals("admin")) {
            return Response.seeOther(URI.create("/login")).build();
        }

        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

        try {
            String id = uploadForm.get("id").get(0).getBodyAsString();
            String name = uploadForm.get("name").get(0).getBodyAsString();
            String description = uploadForm.get("description").get(0).getBodyAsString();
            double price = Double.parseDouble(uploadForm.get("price").get(0).getBodyAsString());
            int quantity = Integer.parseInt(uploadForm.get("quantity").get(0).getBodyAsString());

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

            List<InputPart> inputParts = uploadForm.get("image");
            if (inputParts != null && !inputParts.isEmpty()) {
                InputPart inputPart = inputParts.get(0);
                MultivaluedMap<String, String> header = inputPart.getHeaders();
                String fileName = getFileName(header);
                if (fileName != null && !fileName.isEmpty()) {
                    try (InputStream inputStream = inputPart.getBody(InputStream.class, null)) {
                        String newFileName = UUID.randomUUID().toString() + "-" + fileName;
                        // Save uploads into source META-INF resources so Quarkus serves them at /uploads/*
                        java.nio.file.Path uploadsDir = java.nio.file.Paths.get("src/main/resources/META-INF/resources/uploads");
                        if (!Files.exists(uploadsDir)) {
                            Files.createDirectories(uploadsDir);
                        }
                        java.nio.file.Path filePath = uploadsDir.resolve(newFileName);
                        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                        product.imagePath = newFileName;
                    }
                }
            }

            product.persist();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exceptions
        }

        return Response.seeOther(URI.create("/dashboard")).build();
    }

    private String getFileName(MultivaluedMap<String, String> header) {
        String[] contentDisposition = header.getFirst("Content-Disposition").split(";");
        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {
                String[] name = filename.split("=");
                String finalFileName = name[1].trim().replaceAll("\"", "");
                return finalFileName;
            }
        }
        return "unknown";
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
        return Response.seeOther(URI.create("/dashboard")).build();
    }
}
