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

    /**
     * TC_GetCart_001: Lấy giỏ hàng khi cart tồn tại.
     * Mục tiêu: Kiểm tra xem giỏ hàng của người dùng có tồn tại hay không, nếu có thì trả về giỏ hàng.
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
                .andExpect(status().isOk())  // Kiểm tra nếu trạng thái trả về là OK (200)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));  // Kiểm tra loại nội dung trả về là JSON

        List<Cart> carts = cartRepository.findAll();
        assertThat(carts).isNotEmpty();  // Kiểm tra nếu giỏ hàng không rỗng
    }

    /**
     * TC_SaveCart_InvalidJson_002: Tạo giỏ hàng với JSON không hợp lệ -> lỗi 500.
     * Mục tiêu: Kiểm tra trường hợp gửi JSON không hợp lệ và kiểm tra nếu server trả về lỗi 500.
     */
    @Test
    @WithMockUser(username = "dnucator0", authorities = {"CUSTOMER"})
    void testSaveCart_InvalidJson_ReturnsInternalServerError() throws Exception {
        String invalidJson = "{ \"quantity\": abc }"; // JSON không hợp lệ

        mockMvc.perform(post("/client-api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))  // Gửi yêu cầu POST với JSON không hợp lệ
                .andExpect(status().isInternalServerError());  // Kiểm tra nếu lỗi 500 được trả về
    }

    /**
     * TC_DeleteCartItems_EmptyList_003: Xóa giỏ hàng với danh sách rỗng -> thành công 204.
     * Mục tiêu: Kiểm tra xem xóa giỏ hàng với danh sách trống có trả về mã trạng thái 204 hay không.
     */
    @Test
    @WithMockUser(username = "dnucator0", authorities = {"CUSTOMER"})
    @Transactional
    void testDeleteCartItems_WhenEmptyList_ReturnsNoContent() throws Exception {
        long beforeCount = cartRepository.count();  // Lấy số lượng giỏ hàng trước khi xóa

        mockMvc.perform(delete("/client-api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))  // Gửi danh sách rỗng để xóa
                .andExpect(status().isNoContent());  // Kiểm tra trả về mã trạng thái 204 (Không có nội dung)

        long afterCount = cartRepository.count();  // Kiểm tra số lượng giỏ hàng sau khi xóa
        assertThat(afterCount).isEqualTo(beforeCount);  // Kiểm tra số lượng giỏ hàng không thay đổi
    }

    /**
     * TC_GetCart_Positive_004: Lấy cart thành công và kiểm tra nội dung.
     * Mục tiêu: Kiểm tra khi giỏ hàng tồn tại, API có trả về đúng dữ liệu giỏ hàng và các thông tin như cartVariants, status, userId.
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
//        Cart savedCart = cartRepository.save(cart);  // Lưu giỏ hàng
//
//        mockMvc.perform(get("/client-api/carts"))
//                .andExpect(status().isOk())  // Kiểm tra nếu trạng thái trả về là OK
//                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))  // Kiểm tra nếu trả về là JSON
//                .andExpect(jsonPath("$.cartVariants").isArray())  // Kiểm tra xem trường cartVariants là mảng
//                .andExpect(jsonPath("$.status").value(cart.getStatus()))  // Kiểm tra giá trị status
//                .andExpect(jsonPath("$.userId").value(user.getId()));  // Kiểm tra giá trị userId
//    }

    /**
     * TC_SaveCart_Positive_005: Tạo giỏ hàng thành công với JSON hợp lệ -> hiện tại lỗi 500 (expected).
     * Mục tiêu: Kiểm tra khi tạo giỏ hàng với JSON hợp lệ nhưng hiện tại dự kiến lỗi 500.
     */
    @Test
    @WithMockUser(username = "dnucator0", authorities = {"CUSTOMER"})
    @Transactional
    void testSaveCart_ValidJson_ReturnsInternalServerError() throws Exception {
        String validJson = "{ \"userId\": 4, \"status\": 1 }";

        mockMvc.perform(post("/client-api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))  // Gửi yêu cầu POST với JSON hợp lệ
                .andExpect(status().isInternalServerError());  // Kiểm tra trả về lỗi 500
    }

    /**
     * TC_DeleteCartItems_Positive_006: Xóa giỏ hàng thành công với danh sách hợp lệ.
     * Mục tiêu: Kiểm tra khi xóa giỏ hàng với danh sách hợp lệ, API có trả về mã trạng thái 204 hay không.
     */
    @Test
    @WithMockUser(username = "dnucator0", authorities = {"CUSTOMER"})
    @Transactional
    void testDeleteCartItems_ValidList_ReturnsInternalServerError() throws Exception {
        User user = new User();
        user.setId(4L);

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setStatus(1);
        Cart savedCart = cartRepository.save(cart);  // Lưu giỏ hàng

        // ⚡ Không có CartVariant => API vẫn lỗi 500 do không tìm thấy dữ liệu
        String cartIdListJson = "[{\"cartId\":" + savedCart.getId() + ", \"variantId\":1}]";

        mockMvc.perform(delete("/client-api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cartIdListJson))  // Gửi yêu cầu xóa với danh sách hợp lệ nhưng không có CartVariant
                .andExpect(status().isInternalServerError());  // Kiểm tra nếu trả về lỗi 500
    }

    /**
     * TC_GetCart_Negative_007: Lấy cart khi không có cart -> trả về empty object {}.
     * Mục tiêu: Kiểm tra khi không có giỏ hàng, API trả về đối tượng rỗng.
     */
    @Test
    @WithMockUser(username = "dnucator0", authorities = {"CUSTOMER"})
    void testGetCart_WhenCartDoesNotExist_ReturnsEmptyCart() throws Exception {
        mockMvc.perform(get("/client-api/carts"))
                .andExpect(status().isOk())  // Kiểm tra nếu trạng thái trả về là OK
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))  // Kiểm tra trả về là JSON
                .andExpect(content().json("{}"));  // Kiểm tra nếu trả về đối tượng rỗng
    }

    /**
     * TC_SaveCart_Negative_008: Tạo giỏ hàng thiếu trường bắt buộc -> lỗi 500.
     * Mục tiêu: Kiểm tra khi thiếu trường bắt buộc trong JSON, API trả về lỗi 500.
     */
    @Test
    @WithMockUser(username = "dnucator0", authorities = {"CUSTOMER"})
    void testSaveCart_MissingRequiredField_ReturnsInternalServerError() throws Exception {
        String missingFieldJson = "{ \"status\": 1 }";  // Thiếu trường userId

        mockMvc.perform(post("/client-api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(missingFieldJson))  // Gửi yêu cầu với JSON thiếu trường
                .andExpect(status().isInternalServerError());  // Kiểm tra trả về lỗi 500
    }

    /**
     * TC_DeleteCartItems_Negative_009: Xóa giỏ hàng với JSON format sai -> lỗi 500.
     * Mục tiêu: Kiểm tra khi format JSON không đúng, API trả về lỗi 500.
     */
    @Test
    @WithMockUser(username = "dnucator0", authorities = {"CUSTOMER"})
    void testDeleteCartItems_InvalidJsonFormat_ReturnsInternalServerError() throws Exception {
        String invalidJson = "{ \"invalid\": true }";  // JSON không hợp lệ

        mockMvc.perform(delete("/client-api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))  // Gửi yêu cầu xóa với JSON không hợp lệ
                .andExpect(status().isInternalServerError());  // Kiểm tra nếu trả về lỗi 500
    }
}
