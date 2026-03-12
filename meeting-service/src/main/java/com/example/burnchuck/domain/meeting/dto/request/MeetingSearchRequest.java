package com.example.burnchuck.domain.meeting.dto.request;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeetingSearchRequest {

    private String keyword;
    private String category;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer startTime;
    private Integer endTime;
}
