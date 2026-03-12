package com.example.burnchuck.domain.meeting.controller;

import static com.example.burnchuck.common.enums.SuccessMessage.ATTENDANCE_CANCEL_SUCCESS;
import static com.example.burnchuck.common.enums.SuccessMessage.ATTENDANCE_GET_MEETING_LIST_SUCCESS;
import static com.example.burnchuck.common.enums.SuccessMessage.ATTENDANCE_REGISTER_SUCCESS;

import com.example.burnchuck.common.dto.AuthUser;
import com.example.burnchuck.common.dto.CommonResponse;
import com.example.burnchuck.domain.meeting.dto.response.AttendanceGetMeetingListResponse;
import com.example.burnchuck.domain.meeting.service.AttendanceService;
import com.example.burnchuck.domain.meeting.service.RedissonLockAttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings")
@Tag(name = "Attendance")
public class AttendanceController {

    private final RedissonLockAttendanceService redissonLockAttendanceService;
    private final AttendanceService attendanceService;

    /**
     * 모임 참여 신청
     */
    @Operation(
            summary = "모임 참여 신청",
            description = """
                    특정 사용자와 특정 모임에 대한 User-Meeting 객체를 생성합니다.
                    """
    )
    @PostMapping("/{meetingId}/attendance")
    public ResponseEntity<CommonResponse<Void>> registerAttendance(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long meetingId
    ) {
        redissonLockAttendanceService.registerAttendance(authUser, meetingId);

        return ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse.successNodata(ATTENDANCE_REGISTER_SUCCESS));
    }

    /**
     * 모임 참여 취소
     */
    @Operation(
            summary = "모임 참여 취소",
            description = """
                    특정 사용자와 특정 모임에 대한 User-Meeting 객체를 삭제합니다.
                    """
    )
    @DeleteMapping("/{meetingId}/attendance")
    public ResponseEntity<CommonResponse<Void>> cancelAttendance(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long meetingId
    ) {
        attendanceService.cancelAttendance(authUser, meetingId);

        return ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse.successNodata(ATTENDANCE_CANCEL_SUCCESS));
    }

    /**
     * 참여 중인 모임 목록 조회
     */
    @Operation(
            summary = "참여 중인 모임 목록 조회",
            description = """
                    로그인한 사용자를 기준으로 참여 중인 모임 목록을 조회합니다.
                    """
    )
    @GetMapping("/attendance-meetings")
    public ResponseEntity<CommonResponse<AttendanceGetMeetingListResponse>> getAttendingMeetingList(
        @AuthenticationPrincipal AuthUser authUser
    ) {
        AttendanceGetMeetingListResponse response = attendanceService.getAttendingMeetingList(authUser);

        return ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse.success(ATTENDANCE_GET_MEETING_LIST_SUCCESS, response));
    }
}
