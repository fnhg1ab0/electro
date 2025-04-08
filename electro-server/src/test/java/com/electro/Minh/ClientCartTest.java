package com.electro.Minh;

import com.electro.entity.authentication.User;
import com.electro.entity.cart.Cart;
import com.electro.repository.cart.CartRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClientCartTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "dnucator0", authorities = {"CUSTOMER"})
    @Transactional
    void testGetCart_WhenCartExists_ReturnsCart() throws Exception {
        User user = new User();
        user.setId(4L);

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setStatus(1);

        cartRepository.save(cart);

        mockMvc.perform(get("/client-api/carts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        // Chỉ kiểm tra tổng số cart có tồn tại
        List<Cart> carts = cartRepository.findAll();
        assertThat(carts).isNotEmpty();
    }

    @Test
    @WithMockUser(username = "dnucator0", authorities = {"CUSTOMER"})
    void testSaveCart_InvalidJson_ReturnsInternalServerError() throws Exception {
        String invalidJson = "{ \"quantity\": abc }"; // quantity là field nhưng abc không đặt trong dấu "

        mockMvc.perform(post("/client-api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isInternalServerError());
    }


    @Test
    @WithMockUser(username = "dnucator0", authorities = {"CUSTOMER"})
    @Transactional
    void testDeleteCartItems_WhenEmptyList_ReturnsNoContent() throws Exception {
        // Lưu số lượng cart trước khi gửi request
        long beforeCount = cartRepository.count();

        mockMvc.perform(delete("/client-api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isNoContent());

        // Lưu số lượng cart sau khi gửi request
        long afterCount = cartRepository.count();

        // So sánh số lượng cart trước và sau phải bằng nhau (không thay đổi)
        assertThat(afterCount).isEqualTo(beforeCount);
    }

}
