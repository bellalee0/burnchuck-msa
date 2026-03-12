package com.example.burnchuck.domain.document;

import com.example.burnchuck.common.entity.Meeting;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(indexName = "meetings")
@Setting(settingPath = "elasticsearch/settings.json")
public class MeetingDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "title_analyzer")
    private String title;

    @Field(type = FieldType.Keyword)
    private String categoryCode;

    @GeoPointField
    private GeoPoint geoPoint;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime meetingDatetime;

    @Field(type = FieldType.Integer)
    private Integer meetingHour;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdDatetime;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Long)
    private Long likes;

    @Field(type = FieldType.Keyword)
    private String imgUrl;

    @Field(type = FieldType.Keyword)
    private String location;

    @Field(type = FieldType.Integer)
    private int maxAttendees;

    @Field(type = FieldType.Integer)
    private int currentAttendees;

    public MeetingDocument(Meeting meeting, long likes, int currentAttendees) {
        this.id = meeting.getId();
        this.title = meeting.getTitle();
        this.categoryCode = meeting.getCategory().getCode();
        this.geoPoint = new GeoPoint(meeting.getLatitude(), meeting.getLongitude());
        this.meetingDatetime = meeting.getMeetingDateTime();
        this.meetingHour = meeting.getMeetingDateTime().getHour();
        this.createdDatetime = meeting.getCreatedDatetime();
        this.status = meeting.getStatus().toString();
        this.likes = likes;
        this.imgUrl = meeting.getImgUrl();
        this.location = meeting.getLocation();
        this.maxAttendees = meeting.getMaxAttendees();
        this.currentAttendees = currentAttendees;
    }
}
