package com.electro.Tuan;

import com.electro.controller.client.ClientUserController;
import com.electro.dto.authentication.UserResponse;
import com.electro.dto.client.ClientEmailSettingUserRequest;
import com.electro.dto.client.ClientPasswordSettingUserRequest;
import com.electro.dto.client.ClientPersonalSettingUserRequest;
import com.electro.dto.client.ClientPhoneSettingUserRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static com.electro.utils.TestDataFactory.createMockAuthentication;
import static com.electro.utils.TestDataFactory.objectMapperLogger;
import static org.junit.jupiter.api.Assertions.*;

/**
 * File test: ClientUserControllerTests.java
 * Class được test: ClientUserController
 * <p>
 * Mục tiêu: Test trực tiếp các hàm của ClientUserController với dữ liệu thực từ database.
 * Các endpoint được test:
 * 1. GET /info - Lấy thông tin user
 * 2. POST /personal - Cập nhật thông tin cá nhân
 * 3. POST /phone - Cập nhật số điện thoại
 * 4. POST /email - Cập nhật email
 * 5. POST /password - Cập nhật mật khẩu
 * <p>
 * Các hàm được test:
 * - getUserInfo: lấy thông tin user
 * - updatePersonalSetting: cập nhật thông tin cá nhân
 * - updatePhoneSetting: cập nhật số điện thoại
 * - updateEmailSetting: cập nhật email
 * - updatePasswordSetting: cập nhật mật khẩu
 * Mỗi test case bao gồm:
 * - Mã Test Case với các ký tự đầu viết tắt CUT (Client User Test)
 * - Mục tiêu của test case
 * - Input: các tham số truyền vào
 * - Expected Output: kết quả mong đợi
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ClientUserControllerTests {

    @Autowired
    private ClientUserController clientUserController;

    // GET /info Test Cases

    /**
     * Test Case ID: CUT001
     * Mục tiêu: Kiểm tra lấy thông tin user thành công với username tồn tại.
     * Input: username = "customer"
     * Expected Output: ResponseEntity với status 200 và thông tin user đầy đủ.
     */
    @Test
    public void testGetUserInfo_ExistingUser_CUT001() throws JsonProcessingException {
        String username = "dnucator0";
        Authentication auth = createMockAuthentication(username);

        ResponseEntity<UserResponse> response = clientUserController.getUserInfo(auth);
        System.out.println("Response: " + objectMapperLogger().writeValueAsString(response));

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(username, response.getBody().getUsername());
    }

    /**
     * Test Case ID: CUT002
     * Mục tiêu: Kiểm tra lấy thông tin user thất bại với username không tồn tại.
     * Input: username = "nonexistent"
     * Expected Output: UsernameNotFoundException
     */
    @Test
    public void testGetUserInfo_NonexistentUser_CUT002() {
        Authentication auth = createMockAuthentication("nonexistent");
        assertThrows(UsernameNotFoundException.class, () -> clientUserController.getUserInfo(auth));
    }

    // POST /personal Test Cases

    /**
     * Test Case ID: CUT003
     * Mục tiêu: Kiểm tra cập nhật thông tin cá nhân thành công.
     * Input: username tồn tại, fullname và gender hợp lệ
     * Expected Output: ResponseEntity với status 200 và thông tin đã cập nhật.
     */
    @Test
    public void testUpdatePersonalSetting_ValidData_CUT003() throws JsonProcessingException {
        Authentication auth = createMockAuthentication("dnucator0");
        ClientPersonalSettingUserRequest request = new ClientPersonalSettingUserRequest();
        request.setFullname("New Customer Name");
        request.setGender("MALE");

        ResponseEntity<UserResponse> response = clientUserController.updatePersonalSetting(auth, request);
        System.out.println("Response: " + objectMapperLogger().writeValueAsString(response));

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("New Customer Name", response.getBody().getFullname());
    }

    /**
     * Test Case ID: CUT004
     * Mục tiêu: Kiểm tra cập nhật thông tin cá nhân với username không tồn tại.
     * Input: username không tồn tại
     * Expected Output: UsernameNotFoundException
     */
    @Test
    public void testUpdatePersonalSetting_NonexistentUser_CUT004() {
        Authentication auth = createMockAuthentication("nonexistent");
        ClientPersonalSettingUserRequest request = new ClientPersonalSettingUserRequest();
        request.setFullname("New Name");
        request.setGender("MALE");

        assertThrows(UsernameNotFoundException.class, () ->
                clientUserController.updatePersonalSetting(auth, request));
    }

    // POST /phone Test Cases

    /**
     * Test Case ID: CUT005
     * Mục tiêu: Kiểm tra cập nhật số điện thoại thành công.
     * Input: username tồn tại, số điện thoại hợp lệ
     * Expected Output: ResponseEntity với status 200 và số điện thoại đã cập nhật.
     */
    @Test
    public void testUpdatePhoneSetting_ValidPhone_CUT005() throws JsonProcessingException {
        Authentication auth = createMockAuthentication("dnucator0");
        ClientPhoneSettingUserRequest request = new ClientPhoneSettingUserRequest();
        request.setPhone("0987654321");

        ResponseEntity<UserResponse> response = clientUserController.updatePhoneSetting(auth, request);
        System.out.println("Response: " + objectMapperLogger().writeValueAsString(response));

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("0987654321", response.getBody().getPhone());
    }

    /**
     * Test Case ID: CUT006
     * Mục tiêu: Kiểm tra cập nhật số điện thoại với username không tồn tại.
     * Input: username không tồn tại
     * Expected Output: UsernameNotFoundException
     */
    @Test
    public void testUpdatePhoneSetting_NonexistentUser_CUT006() {
        Authentication auth = createMockAuthentication("nonexistent");
        ClientPhoneSettingUserRequest request = new ClientPhoneSettingUserRequest();
        request.setPhone("0987654321");

        assertThrows(UsernameNotFoundException.class, () ->
                clientUserController.updatePhoneSetting(auth, request));
    }

    // POST /email Test Cases

    /**
     * Test Case ID: CUT007
     * Mục tiêu: Kiểm tra cập nhật email thành công.
     * Input: username tồn tại, email hợp lệ
     * Expected Output: ResponseEntity với status 200 và email đã cập nhật.
     */
    @Test
    public void testUpdateEmailSetting_ValidEmail_CUT007() throws JsonProcessingException {
        Authentication auth = createMockAuthentication("dnucator0");
        ClientEmailSettingUserRequest request = new ClientEmailSettingUserRequest();
        request.setEmail("new.email@example.com");

        ResponseEntity<UserResponse> response = clientUserController.updateEmailSetting(auth, request);
        System.out.println("Response: " + objectMapperLogger().writeValueAsString(response));

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("new.email@example.com", response.getBody().getEmail());
    }

    /**
     * Test Case ID: CUT008
     * Mục tiêu: Kiểm tra cập nhật email với username không tồn tại.
     * Input: username không tồn tại
     * Expected Output: UsernameNotFoundException
     */
    @Test
    public void testUpdateEmailSetting_NonexistentUser_CUT008() {
        Authentication auth = createMockAuthentication("nonexistent");
        ClientEmailSettingUserRequest request = new ClientEmailSettingUserRequest();
        request.setEmail("new.email@example.com");

        assertThrows(UsernameNotFoundException.class, () ->
                clientUserController.updateEmailSetting(auth, request));
    }

    // POST /password Test Cases

    /**
     * Test Case ID: CUT009
     * Mục tiêu: Kiểm tra đổi mật khẩu thành công.
     * Input: username tồn tại, mật khẩu cũ đúng
     * Expected Output: ResponseEntity với status 200
     */
    @Test
    public void testUpdatePasswordSetting_ValidPassword_CUT009() throws Exception {
        Authentication auth = createMockAuthentication("dnucator0");
        ClientPasswordSettingUserRequest request = new ClientPasswordSettingUserRequest();
        request.setOldPassword("123456789");
        request.setNewPassword("987654321");

        ResponseEntity<ObjectNode> response = clientUserController.updatePasswordSetting(auth, request);
        System.out.println("Response: " + objectMapperLogger().writeValueAsString(response));

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    /**
     * Test Case ID: CUT010
     * Mục tiêu: Kiểm tra đổi mật khẩu thất bại do mật khẩu cũ không đúng.
     * Input: username tồn tại, mật khẩu cũ sai
     * Expected Output: Exception với message "Wrong old password"
     */
    @Test
    public void testUpdatePasswordSetting_WrongOldPassword_CUT010() {
        Authentication auth = createMockAuthentication("dnucator0");
        ClientPasswordSettingUserRequest request = new ClientPasswordSettingUserRequest();
        request.setOldPassword("wrongpassword");
        request.setNewPassword("654321");

        Exception exception = assertThrows(Exception.class, () ->
                clientUserController.updatePasswordSetting(auth, request));
        assertEquals("Wrong old password", exception.getMessage());
    }

    /**
     * Test Case ID: CUT011
     * Mục tiêu: Kiểm tra đổi mật khẩu với username không tồn tại.
     * Input: username không tồn tại
     * Expected Output: UsernameNotFoundException
     */
    @Test
    public void testUpdatePasswordSetting_NonexistentUser_CUT011() {
        Authentication auth = createMockAuthentication("nonexistent");
        ClientPasswordSettingUserRequest request = new ClientPasswordSettingUserRequest();
        request.setOldPassword("123456");
        request.setNewPassword("654321");

        assertThrows(UsernameNotFoundException.class, () ->
                clientUserController.updatePasswordSetting(auth, request));
    }
}