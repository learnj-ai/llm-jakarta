package learning.jakarta.ai.bookstore.web;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import learning.jakarta.ai.bookstore.service.CartSession;
import learning.jakarta.ai.bookstore.service.CartSessionManager;

@ApplicationScoped
@Path("/session")
public class SessionResource {
    @Inject
    private CartSessionManager sessionManager;

    @GET
    @Path("/count/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public int getCartItemCount(@PathParam("userId") String userId) {
        CartSession session = sessionManager.getOrCreateSession(userId);
        return session.getCart().getItems().size();
    }
}