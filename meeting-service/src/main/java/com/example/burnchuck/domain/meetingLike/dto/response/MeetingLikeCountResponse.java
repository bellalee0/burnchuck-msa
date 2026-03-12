package com.example.burnchuck.domain.meetingLike.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MeetingLikeCountResponse {

    private final long likes;

    public static MeetingLikeCountResponse of(long likes) {
        return new MeetingLikeCountResponse(likes);
    }
}
