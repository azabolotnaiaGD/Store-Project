package com.nvs.store.models.cart;

import com.nvs.store.models.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "carts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue
    private Long id;
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "cart")
    private List<CartItem> cartItems;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    private Boolean actual;

}
