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

    // ============================
    // 🧪 Các Test Case gốc (bạn đã viết)
    // ============================

    /**
     * TC_GetCart_001
     * Mục tiêu: Lấy giỏ hàng khi cart tồn tại.
     */
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

        List<Cart> carts = cartRepository.findAll();
        assertThat(carts).isNotEmpty();
    }

    /**
     * TC_SaveCart_InvalidJson_002
     * Mục tiêu: Tạo giỏ hàng với JSON không hợp lệ -> lỗi.
     */
    @Test
    @WithMockUser(username = "dnucator0", authorities = {"CUSTOMER"})
    void testSaveCart_InvalidJson_ReturnsInternalServerError() throws Exception {
        String invalidJson = "{ \"quantity\": abc }"; // quantity sai định dạng

        mockMvc.perform(post("/client-api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isInternalServerError());
    }

    /**
     * TC_DeleteCartItems_EmptyList_003
     * Mục tiêu: Xóa giỏ hàng với danh sách rỗng.
     */
    @Test
    @WithMockUser(username = "dnucator0", authorities = {"CUSTOMER"})
    @Transactional
    void testDeleteCartItems_WhenEmptyList_ReturnsNoContent() throws Exception {
        long beforeCount = cartRepository.count();

        mockMvc.perform(delete("/client-api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isNoContent());

        long afterCount = cartRepository.count();
        assertThat(afterCount).isEqualTo(beforeCount);
    }

    // ============================
    // 🟢 Positive Test Cases mới
    // ============================

    /**
     * TC_GetCart_Positive_004
     * Mục tiêu: Lấy cart thành công và kiểm tra nội dung.
     */
//    @Test
//    @WithMockUser(username = "dnucator0", authorities = {"CUSTOMER"})
//    @Transactional
//    void testGetCart_WhenCartExists_ReturnsCart_Success() throws Exception {
//        User user = new User();
//        user.setId(4L);
//
//        Cart cart = new Cart();
//        cart.setUser(user);
//        cart.setStatus(1);
//
//        Cart savedCart = cartRepository.save(cart);
//
//        mockMvc.perform(get("/client-api/carts"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                .andExpect(content().json(objectMapper.writeValueAsString(savedCart)));
//    }

    /**
     * TC_SaveCart_Positive_005
     * Mục tiêu: Tạo giỏ hàng thành công với JSON hợp lệ.
     */
    @Test
    @WithMockUser(username = "dnucator0", authorities = {"CUSTOMER"})
    @Transactional
    void testSaveCart_ValidJson_ReturnsCreated() throws Exception {
        String validJson = "{ \"userId\": 4, \"status\": 1 }";

        mockMvc.perform(post("/client-api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isCreated());
    }

    /**
     * TC_DeleteCartItems_Positive_006
     * Mục tiêu: Xóa giỏ hàng thành công với danh sách hợp lệ.
     */
    @Test
    @WithMockUser(username = "dnucator0", authorities = {"CUSTOMER"})
    @Transactional
    void testDeleteCartItems_ValidList_ReturnsNoContent() throws Exception {
        User user = new User();
        user.setId(4L);

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setStatus(1);
        Cart savedCart = cartRepository.save(cart);

        String cartIdListJson = "[" + savedCart.getId() + "]";

        mockMvc.perform(delete("/client-api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cartIdListJson))
                .andExpect(status().isNoContent());

        assertThat(cartRepository.findById(savedCart.getId())).isEmpty();
    }

    // ============================
    // 🔴 Negative Test Cases mới
    // ============================

    /**
     * TC_GetCart_Negative_007
     * Mục tiêu: Lấy cart khi không có cart -> trả empty.
     */
    @Test
    @WithMockUser(username = "dnucator0", authorities = {"CUSTOMER"})
    void testGetCart_WhenCartDoesNotExist_ReturnsEmptyCart() throws Exception {
        mockMvc.perform(get("/client-api/carts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{}")); // hoặc "[]"
    }

    /**
     * TC_SaveCart_Negative_008
     * Mục tiêu: Tạo giỏ hàng thiếu trường bắt buộc -> lỗi 400.
     */
    @Test
    @WithMockUser(username = "dnucator0", authorities = {"CUSTOMER"})
    void testSaveCart_MissingRequiredField_ReturnsBadRequest() throws Exception {
        String missingFieldJson = "{ \"status\": 1 }"; // thiếu userId

        mockMvc.perform(post("/client-api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(missingFieldJson))
                .andExpect(status().isBadRequest());
    }

    /**
     * TC_DeleteCartItems_Negative_009
     * Mục tiêu: Xóa giỏ hàng với JSON format sai -> lỗi 400.
     */
    @Test
    @WithMockUser(username = "dnucator0", authorities = {"CUSTOMER"})
    void testDeleteCartItems_InvalidJsonFormat_ReturnsBadRequest() throws Exception {
        String invalidJson = "{ \"invalid\": true }";

        mockMvc.perform(delete("/client-api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
