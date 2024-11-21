package com.nvs.store.service;

import com.nvs.store.dto.*;
import com.nvs.store.models.cart.Cart;
import com.nvs.store.models.cart.CartItem;
import com.nvs.store.models.product.Product;
import com.nvs.store.models.user.User;
import com.nvs.store.repository.*;
import com.nvs.store.util.CartUtils;
import com.nvs.store.util.UserUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartDTO findActualCartByEmail() {
        String userEmail = UserUtils.getUserEmailFromContext();
        User user = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException(
                        String.format("User with email [%s] not found", userEmail))
                );
        Cart cart = cartRepository
                .findByUserEmailAndActualTrue(userEmail)
                .orElse(null);
        CartDTO cartDTO;
        if (cart == null) {
            Cart newCart = Cart.builder()
                    .user(user)
                    .actual(true)
                    .cartItems(List.of())
                    .build();
            cartRepository.save(newCart);
            cartDTO = CartUtils.convertToCartDto.apply(newCart);
            return cartDTO;
        }
        cartDTO = CartUtils.convertToCartDto.apply(cart);
        return cartDTO;
    }

    public List<CartDTO> getAllCarts() {
        String userEmail = UserUtils.getUserEmailFromContext();
        return cartRepository
                .findByUserEmail(userEmail)
                .stream()
                .map(CartUtils.convertToCartDto)
                .toList();
    }

    public CartItem addProductToCart(CartItemRequest cartItemRequest) {
        String userEmail = UserUtils.getUserEmailFromContext();
        Product product = productRepository
                .findById(cartItemRequest
                        .getProductId())
                .orElseThrow(() ->
                        new RuntimeException(
                                String.format("Product with id [%d] does not exist", cartItemRequest.getProductId()))
                );
        Cart actualCart = cartRepository
                .findByUserEmailAndActualTrue(userEmail).orElseThrow();

        List<CartItem> cartItems = actualCart.getCartItems();
        for (CartItem cartItem : cartItems) {
            if (cartItem.getProduct().getId().equals(product.getId())) {
                int newQuantity = cartItem.getQuantity() + cartItemRequest.getQuantity();
                validateAvailableProductQuantities(product, newQuantity);
                cartItem.setQuantity(newQuantity);
                return cartItemRepository.save(cartItem);
            }
        }

        validateAvailableProductQuantities(product, cartItemRequest.getQuantity());
        CartItem newCartItem = CartItem
                .builder()
                .quantity(cartItemRequest.getQuantity())
                .product(product)
                .cart(actualCart)
                .build();
        cartItems.add(newCartItem);
        actualCart.setCartItems(cartItems);
        cartRepository.save(actualCart);
        return newCartItem;
    }

    public CartItem updateProduct(Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("No such cart item in cart"));

        validateAvailableProductQuantities(cartItem.getProduct(), quantity);
        cartItem.setQuantity(quantity);
        return cartItemRepository.save(cartItem);
    }

    public CartDTO orderCart() {
        String userEmail = UserUtils.getUserEmailFromContext();
        User user = userRepository.findByEmail(userEmail).orElseThrow(
                () -> new RuntimeException(
                        String.format("User with username %s not found", userEmail))
        );

        Cart cart = cartRepository
                .findByUserEmailAndActualTrue(userEmail).orElseThrow();

        List<CartItem> orderedCartItems = cart.getCartItems();

        for (CartItem cartItem : orderedCartItems) {
            if (cartItem.getProduct().getAvailable() < 1) {
                throw new RuntimeException(
                        String.format("Sorry, [%s] is not available yet", cartItem.getProduct().getTitle())
                );
            } else if (cartItem.getQuantity() > cartItem.getProduct().getAvailable()) {
                throw new RuntimeException(
                        String.format(
                                "Only [%d] of [%s] available. Please order available quantity.",
                                cartItem.getProduct().getAvailable(),
                                cartItem.getProduct().getTitle())
                );
            }

            Product product = cartItem.getProduct();
            product.setAvailable(product.getAvailable() - cartItem.getQuantity());
            productRepository.save(product);
        }

        cart.setActual(false);
        cartRepository.save(cart);

        Cart newCart = Cart.builder()
                .user(user)
                .actual(true)
                .cartItems(List.of())
                .build();
        cartRepository.save(newCart);
        return CartUtils.convertToCartDto
                .apply(cart);
    }

    @Transactional
    public void deleteCartItem(Long id) {
        cartItemRepository.findById(id).ifPresent(cartItem -> cartItemRepository.deleteById(id));
    }

    private void validateAvailableProductQuantities(Product product, Integer quantity) {
        if (product.getAvailable() < 1) {
            throw new RuntimeException(
                    String.format("Sorry, [%s] is not available yet", product.getTitle())
            );
        } else if (quantity > product.getAvailable()) {
            throw new RuntimeException(
                    String.format(
                            "Only [%d] of [%s] available. Please order available quantity.",
                            product.getAvailable(),
                            product.getTitle())
            );
        }
    }
}
