package com.example.burnchuck.domain.meeting.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeetingMapViewPortRequest {

    private Double centerLat;
    private Double centerLng;
    private Double minLat;
    private Double maxLat;
    private Double minLng;
    private Double maxLng;

    public boolean notNull() {
        return minLat != null && maxLat != null && minLng != null && maxLng != null;
    }
}
