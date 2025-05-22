package com.electro.Tuan;

import com.electro.controller.client.ClientUserController;
import com.electro.dto.address.AddressRequest;
import com.electro.dto.authentication.UserResponse;
import com.electro.dto.client.ClientEmailSettingUserRequest;
import com.electro.dto.client.ClientPasswordSettingUserRequest;
import com.electro.dto.client.ClientPersonalSettingUserRequest;
import com.electro.dto.client.ClientPhoneSettingUserRequest;
import com.electro.entity.authentication.User;
import com.electro.exception.InvalidValueException;
import com.electro.repository.authentication.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static com.electro.utils.TestDataFactory.createMockAuthentication;
import static com.electro.utils.TestDataFactory.objectMapperLogger;
import static org.junit.jupiter.api.Assertions.*;

/**
 * File test: ClientUserControllerTests.java
 * Class được test: ClientUserController
 * <p>
 * Kiểm thử hộp trắng:
 * - Cấp độ 1: Bao phủ câu lệnh - Mỗi câu lệnh được thực thi ít nhất một lần
 * - Cấp độ 2: Bao phủ nhánh - Mỗi nhánh quyết định được thực thi ít nhất một lần cho cả true và false
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ClientUserControllerTests {

    @Autowired
    private ClientUserController clientUserController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ------------------------------------------------------------------------------
    // getUserInfo()
    // ------------------------------------------------------------------------------

    /**
     * Test Case ID: CUT001
     * Tên test: testGetUserInfo_ThanhCong
     * Mục tiêu: Kiểm tra phương thức getUserInfo trả về thông tin người dùng thành công
     * Đầu vào: Authentication với username tồn tại
     * Đầu ra mong đợi: HTTP 200 OK với thông tin người dùng đầy đủ
     * Ghi chú: Kiểm tra chức năng cơ bản của endpoint thông tin người dùng
     */
    @Test
    public void testGetUserInfo_ThanhCong_CUT001() throws JsonProcessingException {
        // Chuẩn bị
        String username = "dnucator0";
        Authentication auth = createMockAuthentication(username);

        // Thực hiện
        ResponseEntity<UserResponse> response = clientUserController.getUserInfo(auth);
        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

        // Kiểm tra
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(username, response.getBody().getUsername());

        // Xác minh thông tin người dùng
        User user = userRepository.findByUsername(username).orElseThrow();
        assertEquals(username, user.getUsername());
    }

    /**
     * Test Case ID: CUT002
     * Tên test: testGetUserInfo_UserKhongTonTai
     * Mục tiêu: Kiểm tra phương thức getUserInfo xử lý lỗi khi người dùng không tồn tại
     * Đầu vào: Authentication với username không tồn tại
     * Đầu ra mong đợi: UsernameNotFoundException
     * Ghi chú: Kiểm tra xử lý lỗi cho trường hợp người dùng không tồn tại
     */
    @Test
    public void testGetUserInfo_UserKhongTonTai_CUT002() {
        // Chuẩn bị
        String nonExistingUsername = "nonexistinguser";
        Authentication auth = createMockAuthentication(nonExistingUsername);

        // Thực hiện & Kiểm tra
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> clientUserController.getUserInfo(auth)
        );
        assertEquals(nonExistingUsername, exception.getMessage());

        // Kiểm tra không có thay đổi nào trong cơ sở dữ liệu
        assertFalse(userRepository.findByUsername(nonExistingUsername).isPresent());
    }

    // -------------------------------------------------------------------------------
    // updatePersonalSetting()
    // -------------------------------------------------------------------------------

    /**
     * Test Case ID: CUT003
     * Tên test: testUpdatePersonalSetting_NguoiDungTonTai
     * Mục tiêu: Kiểm tra phương thức updatePersonalSetting cập nhật thông tin người dùng thành công
     * Đầu vào: Authentication với username tồn tại và yêu cầu cập nhật thông tin cá nhân với giá trị hợp lệ
     * Đầu ra mong đợi: HTTP 200 OK với thông tin người dùng đã cập nhật trong phần thân phản hồi
     * Ghi chú: Kiểm tra chức năng cơ bản của endpoint cập nhật thông tin cá nhân
     */
    @Test
    public void testUpdatePersonalSetting_NguoiDungTonTai_CUT003() throws JsonProcessingException {
        // Chuẩn bị
        String username = "dnucator0";
        Authentication auth = createMockAuthentication(username);

        ClientPersonalSettingUserRequest request = new ClientPersonalSettingUserRequest();
        request.setUsername("testuser");
        request.setFullname("Updated Fullname");
        request.setGender("F");
        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setLine("123 Main St");
        addressRequest.setProvinceId(1L);
        addressRequest.setDistrictId(1L);
        addressRequest.setWardId(1L);
        request.setAddress(addressRequest);

        // Thực hiện
        ResponseEntity<UserResponse> response = clientUserController.updatePersonalSetting(auth, request);
        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

        // Kiểm tra
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getUsername());

        // Xác minh thay đổi đã được lưu
        User updatedUser = userRepository.findByUsername("testuser").orElseThrow();
        assertEquals("testuser", updatedUser.getUsername());
        assertEquals("Updated Fullname", updatedUser.getFullname());
        assertEquals("F", updatedUser.getGender());
        assertEquals("123 Main St", updatedUser.getAddress().getLine());
        assertEquals(1L, updatedUser.getAddress().getProvince().getId());
        assertEquals(1L, updatedUser.getAddress().getDistrict().getId());
        assertEquals(1L, updatedUser.getAddress().getWard().getId());
    }

    /**
     * Test Case ID: CUT004
     * Tên test: testUpdatePersonalSetting_NguoiDungKhongTonTai
     * Mục tiêu: Kiểm tra phương thức updatePersonalSetting xử lý lỗi khi người dùng không tồn tại
     * Đầu vào: Authentication với username không tồn tại và yêu cầu cập nhật thông tin cá nhân
     * Đầu ra mong đợi: UsernameNotFoundException
     * Ghi chú: Kiểm tra xử lý lỗi cho trường hợp người dùng không tồn tại
     */
    @Test
    public void testUpdatePersonalSetting_NguoiDungKhongTonTai_CUT004() {
        // Chuẩn bị
        String nonExistingUsername = "nonexistinguser";
        Authentication auth = createMockAuthentication(nonExistingUsername);

        ClientPersonalSettingUserRequest request = new ClientPersonalSettingUserRequest();
        request.setFullname("New Fullname");
        request.setGender("New Gender");

        // Thực hiện & Kiểm tra
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> clientUserController.updatePersonalSetting(auth, request)
        );
        assertEquals(nonExistingUsername, exception.getMessage());

        // Kiểm tra không có thay đổi nào trong cơ sở dữ liệu
        assertFalse(userRepository.findByUsername(nonExistingUsername).isPresent());
    }

    // -------------------------------------------------------------------------------
    // updatePhoneSetting()
    // -------------------------------------------------------------------------------

    /**
     * Test Case ID: CUT005
     * Tên test: testUpdatePhoneSetting_NguoiDungTonTai
     * Mục tiêu: Kiểm tra phương thức updatePhoneSetting cập nhật số điện thoại người dùng thành công
     * Đầu vào: Authentication với username tồn tại và yêu cầu cập nhật số điện thoại
     * Đầu ra mong đợi: HTTP 200 OK với thông tin người dùng đã cập nhật trong phần thân phản hồi
     * Ghi chú: Kiểm tra chức năng cơ bản của endpoint cập nhật số điện thoại
     */
    @Test
    public void testUpdatePhoneSetting_NguoiDungTonTai_CUT005() throws JsonProcessingException {
        // Chuẩn bị
        String username = "dnucator0";
        Authentication auth = createMockAuthentication(username);

        ClientPhoneSettingUserRequest request = new ClientPhoneSettingUserRequest();
        request.setPhone("0987654321");

        // Thực hiện
        ResponseEntity<UserResponse> response = clientUserController.updatePhoneSetting(auth, request);
        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

        // Kiểm tra
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("0987654321", response.getBody().getPhone());

        // Xác minh thay đổi đã được lưu
        User updatedUser = userRepository.findByUsername(username).orElseThrow();
        assertEquals("0987654321", updatedUser.getPhone());
    }

    /**
     * Test Case ID: CUT006
     * Tên test: testUpdatePhoneSetting_NguoiDungKhongTonTai
     * Mục tiêu: Kiểm tra phương thức updatePhoneSetting xử lý lỗi khi người dùng không tồn tại
     * Đầu vào: Authentication với username không tồn tại và yêu cầu cập nhật số điện thoại
     * Đầu ra mong đợi: UsernameNotFoundException
     * Ghi chú: Kiểm tra xử lý lỗi đối với tên người dùng không hợp lệ
     */
    @Test
    public void testUpdatePhoneSetting_NguoiDungKhongTonTai_CUT006() {
        // Chuẩn bị
        String nonExistingUsername = "nonexistinguser";
        Authentication auth = createMockAuthentication(nonExistingUsername);

        ClientPhoneSettingUserRequest request = new ClientPhoneSettingUserRequest();
        request.setPhone("0987654321");

        // Thực hiện & Kiểm tra
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> clientUserController.updatePhoneSetting(auth, request)
        );
        assertEquals(nonExistingUsername, exception.getMessage());

        // Kiểm tra không có thay đổi nào trong cơ sở dữ liệu
        assertFalse(userRepository.findByUsername(nonExistingUsername).isPresent());
    }

    // -------------------------------------------------------------------------------
    // updateEmailSetting()
    // -------------------------------------------------------------------------------

    /**
     * Test Case ID: CUT007
     * Tên test: testUpdateEmailSetting_NguoiDungTonTai
     * Mục tiêu: Kiểm tra phương thức updateEmailSetting cập nhật địa chỉ email người dùng thành công
     * Đầu vào: Authentication với username tồn tại và yêu cầu cập nhật địa chỉ email
     * Đầu ra mong đợi: HTTP 200 OK với thông tin người dùng đã cập nhật trong phần thân phản hồi
     * Ghi chú: Kiểm tra chức năng cơ bản của endpoint cập nhật email
     */
    @Test
    public void testUpdateEmailSetting_NguoiDungTonTai_CUT007() throws JsonProcessingException {
        // Chuẩn bị
        String username = "dnucator0";
        Authentication auth = createMockAuthentication(username);

        ClientEmailSettingUserRequest request = new ClientEmailSettingUserRequest();
        request.setEmail("updated.email@example.com");

        // Thực hiện
        ResponseEntity<UserResponse> response = clientUserController.updateEmailSetting(auth, request);
        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

        // Kiểm tra
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("updated.email@example.com", response.getBody().getEmail());

        // Xác minh thay đổi đã được lưu
        User updatedUser = userRepository.findByUsername(username).orElseThrow();
        assertEquals("updated.email@example.com", updatedUser.getEmail());
    }

    /**
     * Test Case ID: CUT008
     * Tên test: testUpdateEmailSetting_NguoiDungKhongTonTai
     * Mục tiêu: Kiểm tra phương thức updateEmailSetting xử lý lỗi khi người dùng không tồn tại
     * Đầu vào: Authentication với username không tồn tại và yêu cầu cập nhật địa chỉ email
     * Đầu ra mong đợi: UsernameNotFoundException
     * Ghi chú: Kiểm tra xử lý lỗi đối với tên người dùng không hợp lệ
     */
    @Test
    public void testUpdateEmailSetting_NguoiDungKhongTonTai_CUT008() {
        // Chuẩn bị
        String nonExistingUsername = "nonexistinguser";
        Authentication auth = createMockAuthentication(nonExistingUsername);

        ClientEmailSettingUserRequest request = new ClientEmailSettingUserRequest();
        request.setEmail("new.email@example.com");

        // Thực hiện & Kiểm tra
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> clientUserController.updateEmailSetting(auth, request)
        );
        assertEquals(nonExistingUsername, exception.getMessage());

        // Kiểm tra không có thay đổi nào trong cơ sở dữ liệu
        assertFalse(userRepository.findByUsername(nonExistingUsername).isPresent());
    }

    // -------------------------------------------------------------------------------
    // updatePasswordSetting()
    // -------------------------------------------------------------------------------

    /**
     * Test Case ID: CUT009
     * Tên test: testUpdatePasswordSetting_MatKhauCuDung
     * Mục tiêu: Kiểm tra phương thức updatePasswordSetting cập nhật mật khẩu thành công khi mật khẩu cũ đúng
     * Đầu vào: Authentication với username tồn tại và yêu cầu với mật khẩu cũ đúng và mật khẩu mới
     * Đầu ra mong đợi: HTTP 200 OK với đối tượng JSON rỗng trong phần thân phản hồi
     * Ghi chú: Kiểm tra kịch bản cập nhật mật khẩu thành công
     */
    @Test
    public void testUpdatePasswordSetting_MatKhauCuDung_CUT009() throws Exception {
        // Chuẩn bị
        String username = "dnucator0";
        Authentication auth = createMockAuthentication(username);

        // Trước tiên, đặt một mật khẩu đã biết cho người dùng thử nghiệm
        User user = userRepository.findByUsername(username).orElseThrow();
        String oldPassword = "123456789";
        user.setPassword(passwordEncoder.encode(oldPassword));
        userRepository.save(user);

        ClientPasswordSettingUserRequest request = new ClientPasswordSettingUserRequest();
        request.setOldPassword(oldPassword);
        request.setNewPassword("newpassword123");

        // Thực hiện
        ResponseEntity<ObjectNode> response = clientUserController.updatePasswordSetting(auth, request);

        // Kiểm tra
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());

        // Xác minh mật khẩu đã được cập nhật
        User updatedUser = userRepository.findByUsername(username).orElseThrow();
        assertTrue(passwordEncoder.matches("newpassword123", updatedUser.getPassword()));
        assertFalse(passwordEncoder.matches(oldPassword, updatedUser.getPassword()));
    }

    /**
     * Test Case ID: CUT010
     * Tên test: testUpdatePasswordSetting_MatKhauCuSai
     * Mục tiêu: Kiểm tra phương thức updatePasswordSetting ném ngoại lệ khi mật khẩu cũ không đúng
     * Đầu vào: Authentication với username tồn tại và yêu cầu với mật khẩu cũ không đúng
     * Đầu ra mong đợi: Exception với thông báo "Wrong old password"
     * Ghi chú: Kiểm tra tính năng bảo mật xác thực mật khẩu
     */
    @Test
    public void testUpdatePasswordSetting_MatKhauCuSai_CUT010() {
        // Chuẩn bị
        String username = "dnucator0";
        Authentication auth = createMockAuthentication(username);

        // Trước tiên, đặt một mật khẩu đã biết cho người dùng thử nghiệm
        User user = userRepository.findByUsername(username).orElseThrow();
        String correctOldPassword = "123456789";
        user.setPassword(passwordEncoder.encode(correctOldPassword));
        userRepository.save(user);

        ClientPasswordSettingUserRequest request = new ClientPasswordSettingUserRequest();
        request.setOldPassword("wrong_password");
        request.setNewPassword("newpassword123");

        // Thực hiện & Kiểm tra
        Exception exception = assertThrows(
                Exception.class,
                () -> clientUserController.updatePasswordSetting(auth, request)
        );
        assertEquals("Wrong old password", exception.getMessage());

        // Xác minh mật khẩu không được cập nhật
        User notUpdatedUser = userRepository.findByUsername(username).orElseThrow();
        assertTrue(passwordEncoder.matches(correctOldPassword, notUpdatedUser.getPassword()));
    }

    /**
     * Test Case ID: CUT011
     * Tên test: testUpdatePasswordSetting_NguoiDungKhongTonTai
     * Mục tiêu: Kiểm tra phương thức updatePasswordSetting xử lý lỗi khi người dùng không tồn tại
     * Đầu vào: Authentication với username không tồn tại và yêu cầu cập nhật mật khẩu
     * Đầu ra mong đợi: UsernameNotFoundException
     * Ghi chú: Kiểm tra xử lý lỗi đối với tên người dùng không hợp lệ
     */
    @Test
    public void testUpdatePasswordSetting_NguoiDungKhongTonTai_CUT011() {
        // Chuẩn bị
        String nonExistingUsername = "nonexistinguser";
        Authentication auth = createMockAuthentication(nonExistingUsername);

        ClientPasswordSettingUserRequest request = new ClientPasswordSettingUserRequest();
        request.setOldPassword("oldpassword");
        request.setNewPassword("newpassword");

        // Thực hiện & Kiểm tra
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> clientUserController.updatePasswordSetting(auth, request)
        );
        assertEquals(nonExistingUsername, exception.getMessage());

        // Kiểm tra không có thay đổi nào trong cơ sở dữ liệu
        assertFalse(userRepository.findByUsername(nonExistingUsername).isPresent());
    }

    // -------------------------------------------------------------------------------
    // Additional test cases
    // -------------------------------------------------------------------------------

    /**
     * Test Case ID: CUT012
     * Tên test: testUpdatePersonalSetting_ThongTinKhongHopLe
     * Mục tiêu: Kiểm tra phương thức updatePersonalSetting xử lý lỗi khi thông tin không hợp lệ
     * Đầu vào: Authentication với username tồn tại và yêu cầu cập nhật thông tin cá nhân với giá trị không hợp lệ
     * Đầu ra mong đợi: InvalidValueException
     * Ghi chú: Kiểm tra xử lý lỗi cho trường hợp thông tin không hợp lệ
     */
    @Test
    public void testUpdatePersonalSetting_ThongTinKhongHopLe_CUT012() {
        // Chuẩn bị
        String username = "dnucator0";
        User user = userRepository.findByUsername(username).orElseThrow();
        String existingFullname = user.getFullname();
        String existingGender = user.getGender();
        Authentication auth = createMockAuthentication(username);

        ClientPersonalSettingUserRequest request = new ClientPersonalSettingUserRequest();
        request.setUsername("testuser");
        request.setFullname("Updated Fullname");
        request.setGender("testGender");

        // Thực hiện & Kiểm tra
        assertThrows(
                InvalidValueException.class,
                () -> clientUserController.updatePersonalSetting(auth, request)
        );

        // kiểm tra trực tiếp từ cơ sở dữ liệu
        user = userRepository.findByUsername(username).orElseThrow();
        assertEquals(existingFullname, user.getFullname());
        assertEquals(existingGender, user.getGender());
        assertEquals(username, user.getUsername());
    }

    /**
     * Test Case ID: CUT013
     * Tên test: testUpdatePhoneSetting_SoDienThoaiKhongHopLe
     * Mục tiêu: Kiểm tra phương thức updatePhoneSetting xử lý lỗi khi số điện thoại không hợp lệ
     * Đầu vào: Authentication với username tồn tại và yêu cầu cập nhật số điện thoại không hợp lệ
     * Đầu ra mong đợi: InvalidValueException
     * Ghi chú: Kiểm tra xử lý lỗi cho trường hợp số điện thoại không hợp lệ
     */
    @Test
    public void testUpdatePhoneSetting_SoDienThoaiKhongHopLe_CUT013() {
        // Chuẩn bị
        String username = "dnucator0";
        User user = userRepository.findByUsername(username).orElseThrow();
        String existingPhone = user.getPhone();
        Authentication auth = createMockAuthentication(username);
        ClientPhoneSettingUserRequest request = new ClientPhoneSettingUserRequest();
        request.setPhone("invalid_phone_number");
        // Thực hiện & Kiểm tra
        assertThrows(
                InvalidValueException.class,
                () -> clientUserController.updatePhoneSetting(auth, request)
        );

        // kiểm tra trực tiếp từ cơ sở dữ liệu
        user = userRepository.findByUsername(username).orElseThrow();
        assertEquals(existingPhone, user.getPhone());
    }

    /**
     * Test Case ID: CUT014
     * Tên test: testUpdatePhoneSetting_SoDienThoaiDaTonTai
     * Mục tiêu: Kiểm tra phương thức updatePhoneSetting xử lý lỗi khi số điện thoại đã tồn tại
     * Đầu vào: Authentication với username tồn tại và yêu cầu cập nhật số điện thoại đã tồn tại
     * Đầu ra mong đợi: DuplicateKeyException
     * Ghi chú: Kiểm tra xử lý lỗi cho trường hợp số điện thoại đã tồn tại
     */
    @Test
    public void testUpdatePhoneSetting_SoDienThoaiDaTonTai_CUT014() {
        // Chuẩn bị
        String username = "dnucator0";
        User user = userRepository.findByUsername(username).orElseThrow();
        String existingPhone = user.getPhone();
        Authentication auth = createMockAuthentication(username);
        ClientPhoneSettingUserRequest request = new ClientPhoneSettingUserRequest();
        request.setPhone("0919944709");
        // Thực hiện & Kiểm tra
        assertThrows(
                DuplicateKeyException.class,
                () -> clientUserController.updatePhoneSetting(auth, request)
        );

        // kiểm tra trực tiếp từ cơ sở dữ liệu
        user = userRepository.findByUsername(username).orElseThrow();
        assertEquals(existingPhone, user.getPhone());
    }

    /**
     * Test Case ID: CUT015
     * Tên test: testUpdateEmailSetting_DiaChiEmailKhongHopLe
     * Mục tiêu: Kiểm tra phương thức updateEmailSetting xử lý lỗi khi địa chỉ email không hợp lệ
     * Đầu vào: Authentication với username tồn tại và yêu cầu cập nhật địa chỉ email không hợp lệ
     * Đầu ra mong đợi: InvalidValueException
     * Ghi chú: Kiểm tra xử lý lỗi cho trường hợp địa chỉ email không hợp lệ
     */
    @Test
    public void testUpdateEmailSetting_DiaChiEmailKhongHopLe_CUT015() {
        // Chuẩn bị
        String username = "dnucator0";
        User user = userRepository.findByUsername(username).orElseThrow();
        String existingEmail = user.getEmail();
        Authentication auth = createMockAuthentication(username);
        ClientEmailSettingUserRequest request = new ClientEmailSettingUserRequest();
        request.setEmail("invalid_email");
        // Thực hiện & Kiểm tra
        assertThrows(
                InvalidValueException.class,
                () -> clientUserController.updateEmailSetting(auth, request)
        );

        // kiểm tra trực tiếp từ cơ sở dữ liệu
        user = userRepository.findByUsername(username).orElseThrow();
        assertEquals(existingEmail, user.getEmail());
    }

    /**
     * Test Case ID: CUT016
     * Tên test: testUpdateEmailSetting_DiaChiEmailDaTonTai
     * Mục tiêu: Kiểm tra phương thức updateEmailSetting xử lý lỗi khi địa chỉ email đã tồn tại
     * Đầu vào: Authentication với username tồn tại và yêu cầu cập nhật địa chỉ email đã tồn tại
     * Đầu ra mong đợi: DuplicateKeyException
     * Ghi chú: Kiểm tra xử lý lỗi cho trường hợp địa chỉ email đã tồn tại
     */
    @Test
    public void testUpdateEmailSetting_DiaChiEmailDaTonTai_CUT016() {
        // Chuẩn bị
        String username = "dnucator0";
        User user = userRepository.findByUsername(username).orElseThrow();
        String existingEmail = user.getEmail();
        Authentication auth = createMockAuthentication(username);
        ClientEmailSettingUserRequest request = new ClientEmailSettingUserRequest();
        request.setEmail("jgratten1@google.co.jp");
        // Thực hiện & Kiểm tra
        assertThrows(
                DuplicateKeyException.class,
                () -> clientUserController.updateEmailSetting(auth, request)
        );

        // kiểm tra trực tiếp từ cơ sở dữ liệu
        user = userRepository.findByUsername(username).orElseThrow();
        assertEquals(existingEmail, user.getEmail());
    }
}