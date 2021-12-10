package com.cocodan.triplan.post.schedule.dto.response;

import com.cocodan.triplan.member.domain.vo.GenderType;
import com.cocodan.triplan.post.schedule.domain.SchedulePost;
import com.cocodan.triplan.post.schedule.vo.Ages;
import com.cocodan.triplan.schedule.dto.response.DailyScheduleSpotResponse;
import com.cocodan.triplan.spot.domain.vo.City;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@Builder
public class SchedulePostDetailResponse {

    private String nickname;

    private Ages ages;

    private GenderType gender;

    private City city;

    private LocalDate startDate;

    private LocalDate endDate;

    private String title;

    private String content;

    private List<DailyScheduleSpotResponse> dailyScheduleSpots;

    private LocalDateTime createdAt;

    private Long views;

    private Long liked;

    // TODO: 2021.12.09 Teru - Comment 추가

    public static SchedulePostDetailResponse from(SchedulePost schedulePost) {
        return SchedulePostDetailResponse.builder()
                .nickname(schedulePost.getMember().getNickname())
                .ages(Ages.from(schedulePost.getMember().getBirth()))
                .gender(schedulePost.getMember().getGender())
                .city(schedulePost.getCity())
                .startDate(schedulePost.getSchedule().getStartDate())
                .endDate(schedulePost.getSchedule().getEndDate())
                .title(schedulePost.getTitle())
                .content(schedulePost.getContent())
                .dailyScheduleSpots(schedulePost.getSchedule().getDailyScheduleSpots().stream()
                        .map(DailyScheduleSpotResponse::from)
                        .collect(Collectors.toList())
                )
                .createdAt(schedulePost.getCreatedDate())
                .views(schedulePost.getViews())
                .liked(schedulePost.getLiked())
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchedulePostDetailResponse that = (SchedulePostDetailResponse) o;
        return nickname.equals(that.nickname) && ages == that.ages && gender == that.gender && city == that.city && startDate.equals(that.startDate) && endDate.equals(that.endDate) && title.equals(that.title) && content.equals(that.content) && dailyScheduleSpots.equals(that.dailyScheduleSpots) && createdAt.equals(that.createdAt) && views.equals(that.views) && liked.equals(that.liked);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nickname, ages, gender, city, startDate, endDate, title, content, dailyScheduleSpots, createdAt, views, liked);
    }
}