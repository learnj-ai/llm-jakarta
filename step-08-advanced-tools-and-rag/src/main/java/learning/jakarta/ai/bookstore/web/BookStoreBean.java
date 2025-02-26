package learning.jakarta.ai.bookstore.web;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import learning.jakarta.ai.bookstore.domain.Book;
import learning.jakarta.ai.bookstore.domain.Cart;
import learning.jakarta.ai.bookstore.domain.CartItem;
import learning.jakarta.ai.bookstore.service.BookStoreService;
import learning.jakarta.ai.bookstore.service.CartSession;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Named
@ViewScoped
public class BookStoreBean implements Serializable {

    @Inject
    private BookStoreService bookStoreService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Getter
    private List<Book> books;

    @Getter
    @Setter
    private String searchQuery;

    @Getter
    private String userId;

    @Getter
    private CartSession currentCart;

    private String getUserIdFromCookie() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("userId".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void setUserIdCookie(String userId) {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
        Cookie cookie = new Cookie("userId", userId);
        cookie.setMaxAge(60 * 60 * 24 * 365); // 1 year
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private void saveCartToCookie() {
        try {
            if (currentCart != null) {
                String cartJson = objectMapper.writeValueAsString(currentCart.getCart());
                String encodedCart = URLEncoder.encode(cartJson, StandardCharsets.UTF_8);
                FacesContext context = FacesContext.getCurrentInstance();
                HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
                Cookie cookie = new Cookie("cart", encodedCart);
                cookie.setMaxAge(60 * 60 * 24 * 365); // 1 year
                cookie.setPath("/");
                response.addCookie(cookie);
            }
        } catch (Exception e) {
            // Log error but continue
            e.printStackTrace();
        }
    }

    private void loadCartFromCookie() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
            Cookie[] cookies = request.getCookies();

            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("cart".equals(cookie.getName())) {
                        String encodedCart = cookie.getValue();
                        String cartJson = URLDecoder.decode(encodedCart, StandardCharsets.UTF_8);
                        log.info("books on cart: {}", cartJson);
                        Cart cart = objectMapper.readValue(cartJson, Cart.class);
                        currentCart.setCart(cart);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            // Log error but continue with empty cart
            e.printStackTrace();
        }
    }

    @PostConstruct
    public void init() {
        // Try to get userId from cookie
        userId = getUserIdFromCookie();

        if (userId == null) {
            // Generate new userId if not found in cookie
            long timestamp = System.currentTimeMillis();
            String randomStr = Long.toString(Math.abs(java.util.UUID.randomUUID().getLeastSignificantBits()), 36).substring(0, 13);
            userId = String.format("user-%d-%s", timestamp, randomStr);
            setUserIdCookie(userId);
        }


        // If no cart in cookie, create new one
        if (currentCart == null) {
            currentCart = bookStoreService.getOrCreateCart(userId);
        }

        loadCartFromCookie();

        // Load initial book list
        showAllBooks();
    }

    public void showAllBooks() {
        books = bookStoreService.getAllBooks();
    }

    public void searchBooks() {
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            books = bookStoreService.searchBooks(searchQuery);
        } else {
            showAllBooks();
        }
    }

    public void filterByCategory(String category) {
        books = bookStoreService.searchByCategory(category);
    }

    public void addToCart(String isbn) {
        if (currentCart == null) {
            currentCart = bookStoreService.getOrCreateCart(userId);
        }

        try {
            bookStoreService.addToCart(userId, isbn, 1);
            books = bookStoreService.getAllBooks();
            currentCart = bookStoreService.getOrCreateCart(userId); // Refresh cart
            saveCartToCookie(); // Save updated cart to cookie
        } catch (IllegalArgumentException e) {
            // Handle error (e.g., show message to user)
        }
    }

    public long getCartItemCount() {
        return currentCart != null ? currentCart.getCart().getItems()
            .stream()
            .mapToInt(CartItem::getQuantity)
            .sum() : 0;
    }

    public double getCartTotal() {
        return currentCart != null ? currentCart.getCart().getTotal() : 0.0;
    }

    public void removeFromCart(String isbn) {
        if (currentCart != null) {
            bookStoreService.removeFromCart(userId, isbn);
            currentCart = bookStoreService.getOrCreateCart(userId); // Refresh cart
            saveCartToCookie(); // Save updated cart to cookie
        }
    }

    public void refreshBooks() {
        books = bookStoreService.getAllBooks();
        if (userId != null) {
            currentCart = bookStoreService.getOrCreateCart(userId);
        }
    }
}
