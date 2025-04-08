package com.electro;

import com.electro.entity.address.District;
import com.electro.entity.address.Province;
import com.electro.repository.address.DistrictRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional  // ✅ Tự động rollback sau mỗi test
public class DistrictRepositoryTests {

    @Autowired
    private DataSource dataSource;
    @Autowired
    private DistrictRepository districtRepository;

    private Province testProvince;

    @BeforeEach
    void setup() {
        // ✅ Tạo một tỉnh trước mỗi test
        testProvince = new Province();
        testProvince.setId(10L); // Giả sử tỉnh có ID = 10
    }

    @Test
    void injectedComponentsAreNotNull() {
        assertThat(dataSource).isNotNull();
        assertThat(districtRepository).isNotNull();
    }

    @Test
    void findDistrictById() {
        // ✅ Tạo dữ liệu test trước khi tìm kiếm
        District district = new District()
                .setName("Quận 1")
                .setCode("Q1")
                .setProvince(testProvince);
        district = districtRepository.save(district);

        // ✅ Tìm kiếm theo ID
        Optional<District> foundDistrict = districtRepository.findById(district.getId());

        assertThat(foundDistrict).isPresent();
        assertThat(foundDistrict.get().getName()).isEqualTo("Quận 1");
    }

    @Test
    void saveDistrictWithProvinceHavingOnlyId() {
        // ✅ Kiểm tra lưu District khi Province chỉ có ID
        District district = new District()
                .setName("Orange County")
                .setCode("12345")
                .setProvince(testProvince);

        District savedDistrict = districtRepository.save(district);

        Optional<District> foundDistrict = districtRepository.findById(savedDistrict.getId());

        assertThat(foundDistrict).isPresent();
        assertThat(foundDistrict.get().getProvince().getId()).isEqualTo(10L);
    }

    @Test
    void updateDistrict() {
        // ✅ Lưu district trước
        District district = new District()
                .setName("Old Name")
                .setCode("OLD")
                .setProvince(testProvince);
        district = districtRepository.save(district);

        // ✅ Cập nhật district
        district.setName("New Name");
        district.setCode("NEW");
        districtRepository.save(district);

        // ✅ Kiểm tra cập nhật thành công
        Optional<District> updatedDistrict = districtRepository.findById(district.getId());

        assertThat(updatedDistrict).isPresent();
        assertThat(updatedDistrict.get().getName()).isEqualTo("New Name");
        assertThat(updatedDistrict.get().getCode()).isEqualTo("NEW");
    }

    @Test
    void deleteDistrict() {
        // ✅ Tạo district trước
        District district = new District()
                .setName("To Be Deleted")
                .setCode("DEL")
                .setProvince(testProvince);
        district = districtRepository.save(district);
        Long districtId = district.getId();

        // ✅ Xóa district
        districtRepository.deleteById(districtId);

        // ✅ Kiểm tra đã xóa thành công
        Optional<District> deletedDistrict = districtRepository.findById(districtId);
        assertThat(deletedDistrict).isEmpty();
    }
}
