package com.example.burnchuck.domain.meeting.dto.request;

import com.example.burnchuck.common.entity.Address;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserLocationRequest {

    private Double distance;
    private Double latitude;
    private Double longitude;

    public boolean noCurrentLocation() {
        return latitude == null || longitude == null;
    }

    public void setLocation(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setLocation(Address address) {
        this.latitude = address.getLatitude();
        this.longitude = address.getLongitude();
    }
}
