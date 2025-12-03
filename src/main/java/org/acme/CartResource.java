package org.acme;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/cart")
public class CartResource {

    @Inject
    Template cart;

    @Inject
    CartService cartService;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance viewCart(@jakarta.ws.rs.CookieParam("username") String username) {
        if (username == null || username.isBlank()) {
            // show empty cart view but prompt login
            return cart.instance().data("items", List.of()).data("total", 0.0).data("username", username);
        }
        var entries = cartService.getCart(username);
        List<CartItem> items = new ArrayList<>();
        double total = 0.0;
        for (var e : entries.entrySet()) {
            Long id = e.getKey();
            int qty = e.getValue();
            Product p = Product.findById(id);
            if (p == null) continue;
            var ci = new CartItem(p.id, p.name, p.imagePath, p.price, qty);
            items.add(ci);
            total += ci.subtotal();
        }
        return cart.instance().data("items", items).data("total", total).data("username", username);
    }

    @POST
    @Path("/add/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ApiResponse addToCart(@PathParam("id") long id, @jakarta.ws.rs.CookieParam("username") String username) {
        // Debug logging removed to keep terminal output clean
        if (username == null || username.isBlank()) {
            return new ApiResponse(false, "not_logged_in", 0);
        }
        int count = cartService.addItem(username, id);
        return new ApiResponse(true, "added", count);
    }

    @POST
    @Path("/update/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public ApiResponse updateQty(@PathParam("id") long id, @FormParam("quantity") Integer quantity,
                                 @jakarta.ws.rs.CookieParam("username") String username) {
        if (username == null || username.isBlank()) {
            return new ApiResponse(false, "not_logged_in", 0);
        }
        if (quantity == null) {
            return new ApiResponse(false, "missing_quantity", 0);
        }
        int q = cartService.updateQuantity(username, id, quantity);
        return new ApiResponse(true, "updated", q);
    }

    @POST
    @Path("/delete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ApiResponse deleteItem(@PathParam("id") long id, @jakarta.ws.rs.CookieParam("username") String username) {
        if (username == null || username.isBlank()) {
            return new ApiResponse(false, "not_logged_in", 0);
        }
        cartService.removeItem(username, id);
        return new ApiResponse(true, "removed", 0);
    }

    @POST
    @Path("/buy")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ApiResponse buy(List<Long> productIds, @jakarta.ws.rs.CookieParam("username") String username) {
        if (username == null || username.isBlank()) {
            return new ApiResponse(false, "not_logged_in", 0);
        }
        double total = cartService.purchaseItems(username, productIds);
        return new ApiResponse(true, "purchased", (int) Math.round(total));
    }
}
