package com.nvs.store.service;

import com.nvs.store.models.product.Product;
import com.nvs.store.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;


    @Test
    public void getAllProductsTest() {
        when(productRepository.findAll()).thenReturn(Stream.of(
                new Product(1L, "strawberry", 25, BigDecimal.valueOf(45.60)),
                new Product(1L, "banana", 31, BigDecimal.valueOf(37.90))).collect(Collectors.toList()));
        assertEquals(2, productService.getAllProducts().size());
    }

    @Test
    public void getProductTest() {
        Long id = 1L;
        Optional<Product> product = Optional.of(new Product(1L, "strawberry", 25, BigDecimal.valueOf(45.60)));
        Mockito.when(productRepository.findById(id)).thenReturn(product);
        Optional<Product> productById = productService.getProduct(id);
        assertEquals(product, productById);
    }

    @Test
    public void addProductTest() {
        Product product = new Product(1000L, "pear", 33, BigDecimal.valueOf(35.89));
        productService.addProduct(product);
        verify(productRepository).save(Mockito.argThat(testProduct -> testProduct.equals(product)));
    }

    @Test
    public void updateProductTest() {
        Product oldProduct = new Product(1L, "orange", 49, BigDecimal.valueOf(23.30));
        Long id = 1L;
        Mockito.when(productRepository.getProductById(id)).thenReturn(oldProduct);
        Product newProduct = new Product(2L, "apple", 25, BigDecimal.valueOf(11.50));
        productService.updateProduct(oldProduct.getId(), newProduct);
        verify(productRepository)
                .save(
                        Mockito.argThat(
                                product -> product.getId().equals(oldProduct.getId())
                                        && product.getTitle().equals(newProduct.getTitle())
                                        && product.getAvailable().equals(newProduct.getAvailable())
                                        && product.getPrice().equals(newProduct.getPrice())
                        )
                );
    }

    @Test
    public void deleteProductTest() {
        Product product = new Product(1L, "strawberry", 25, BigDecimal.valueOf(45.60));
        productService.deleteProduct(product.getId());
        assertNull(productRepository.getProductById(product.getId()));
    }

}
