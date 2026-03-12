package com.example.burnchuck.domain.meeting.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocationFilterRequest {

    private String province;
    private String city;
    private String district;

    public boolean notNull() {
        return province != null && city != null;
    }
}
