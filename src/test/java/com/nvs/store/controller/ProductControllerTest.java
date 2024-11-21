package com.nvs.store.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.nvs.store.models.product.Product;
import com.nvs.store.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private ProductService productService;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    public void addProductTest() throws Exception {
        Product product = new Product(1000L, "pear", 33, BigDecimal.valueOf(35.89));
        when(productService.addProduct(product)).thenReturn(product);
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", is(1000)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title", is(product.getTitle())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.available", is(product.getAvailable())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price").value((product.getPrice())));
    }

    @Test
    public void getAllProductsTest() throws Exception {
        Product product1 = new Product(1L, "pear", 33, BigDecimal.valueOf(35.89));
        Product product2 = new Product(2L, "apple", 23, BigDecimal.valueOf(13.90));
        List<Product> productList = Arrays.asList(product1, product2);
        when(productService.getAllProducts()).thenReturn(productList);
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", is(productList.size())));
    }

    @Test
    public void getProductTest() throws Exception {
        Optional<Product> product = Optional.of(new Product(1L, "apple", 23, BigDecimal.valueOf(13.90)));
        when(productService.getProduct(anyLong())).thenReturn(product);
        mockMvc.perform(get("/api/v1/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title", is(product.get().getTitle())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.available", is(product.get().getAvailable())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price").value((product.get().getPrice())));
    }

    @Test
    public void deleteProductTest() throws Exception {
        Optional<Product> product = Optional.of(new Product(1L, "apple", 23, BigDecimal.valueOf(13.90)));
        when(productService.getProduct(anyLong())).thenReturn(product);
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void updateProductTest() throws Exception {
        Optional<Product> product = Optional.of(new Product(1L, "pear", 33, BigDecimal.valueOf(35.89)));
        Product updProduct = new Product(1L, "apple", 15, BigDecimal.valueOf(11.24));
        when(productService.getProduct(product.get().getId())).thenReturn(product);
        when(productService.updateProduct(product.get().getId(), updProduct)).thenReturn(updProduct);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updProduct)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(updProduct.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.available").value(updProduct.getAvailable()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price").value((updProduct.getPrice())));
    }
}


