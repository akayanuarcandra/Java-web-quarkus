package org.acme;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/_dashboard_disabled")
public class DashboardResource {

    @Inject
    Template dashboard;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getDashboard(@CookieParam("user") String userCookie) throws UnsupportedEncodingException {
        if (userCookie == null) {
            // This is a simplified way to handle unauthorized access.
            // A real application should use a proper security framework or filter
            // to redirect to a login page.
            return null;
        }

        String username = URLDecoder.decode(userCookie, StandardCharsets.UTF_8);
        List<Product> products = Product.listAll();
        return dashboard.data("username", username).data("products", products);
    }
}

