package com.example.burnchuck.domain.meeting.repository;

import com.example.burnchuck.common.entity.Address;
import com.example.burnchuck.common.enums.ErrorCode;
import com.example.burnchuck.common.exception.CustomException;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {

    Optional<Address> findByProvinceAndCityAndDistrict(String province, String city, String district);

    default Address findAddressByAddressInfo(String province, String city, String district) {
        return findByProvinceAndCityAndDistrict(province, city, district)
            .orElseThrow(() -> new CustomException(ErrorCode.ADDRESS_NOT_FOUND));
    }
}
