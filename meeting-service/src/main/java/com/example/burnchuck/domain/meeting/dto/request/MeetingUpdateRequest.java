package com.example.burnchuck.domain.meeting.dto.request;

import static com.example.burnchuck.common.constants.ValidationMessage.*;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MeetingUpdateRequest {

    @NotBlank(message = MEETING_TITLE_NOT_BLANK)
    @Size(max = 50, message = MEETING_TITLE_SIZE)
    private String title;

    @NotBlank(message = MEETING_DESCRIPTION_NOT_BLANK)
    @Size(max = 500, message = MEETING_DESCRIPTION_SIZE)
    private String description;

    @NotBlank(message = IMG_URL_NOT_BLANK)
    @Pattern(
            regexp = "^(http|https)://.*$",
            message = IMG_URL_FORMAT
    )
    private String imgUrl;

    @NotBlank(message = MEETING_LOCATION_NOT_BLANK)
    private String location;

    @NotNull(message = MEETING_LATITUDE_NOT_NULL)
    @DecimalMin(value = "-90.0", message = MEETING_LATITUDE_RANGE)
    @DecimalMax(value = "90.0", message = MEETING_LATITUDE_RANGE)
    private Double latitude;

    @NotNull(message = MEETING_LONGITUDE_NOT_NULL)
    @DecimalMin(value = "-180.0", message = MEETING_LONGITUDE_RANGE)
    @DecimalMax(value = "180.0", message = MEETING_LONGITUDE_RANGE)
    private Double longitude;

    @NotNull(message = MEETING_MAX_ATTENDEES_NOT_NULL)
    private int maxAttendees;

    @NotNull(message = MEETING_DATETIME_NOT_NULL)
    @Future(message = MEETING_DATETIME_FUTURE)
    private LocalDateTime meetingDateTime;

    @NotBlank(message = MEETING_CATEGORY_NOT_NULL)
    private String categoryCode;
}
