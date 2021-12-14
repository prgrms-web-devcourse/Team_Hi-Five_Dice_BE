package com.cocodan.triplan.post.schedule.dto.response;

import com.cocodan.triplan.member.domain.Member;
import com.cocodan.triplan.member.domain.vo.GenderType;
import com.cocodan.triplan.member.dto.response.MemberGetOneResponse;
import com.cocodan.triplan.post.schedule.domain.SchedulePost;
import com.cocodan.triplan.post.schedule.vo.Ages;
import com.cocodan.triplan.schedule.domain.Schedule;
import com.cocodan.triplan.schedule.domain.ScheduleTheme;
import com.cocodan.triplan.schedule.domain.vo.Theme;
import com.cocodan.triplan.spot.domain.vo.City;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@Builder
public class SchedulePostResponse {

    private long writerId;

    private String profileImageUrl;

    private String nickname;

    private Ages ages;

    private GenderType genderType;

    private long postId;

    private String title;

    private City city;

    private long scheduleId;

    private List<Theme> themes;

    private LocalDate startDate;

    private LocalDate endDate;

    public static SchedulePostResponse of(MemberGetOneResponse member, Schedule schedule, City city, List<Theme> themes, String title) {
        return SchedulePostResponse.builder()
                .profileImageUrl(member.getProfileImage())
                .nickname(member.getNickname())
                .title(title)
                .ages(Ages.from(member.getBirth()))
                .genderType(member.getGender())
                .city(city)
                .themes(themes)
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .build();
    }

    public static SchedulePostResponse of(SchedulePost schedulePost, Member member, Schedule schedule) {
        return SchedulePostResponse.builder()
                .writerId((member.getId()))
                .profileImageUrl(member.getProfileImage())
                .nickname(member.getNickname())
                .ages(Ages.from(member.getBirth()))
                .genderType(member.getGender())
                .title(schedulePost.getTitle())
                .city(schedulePost.getCity())
                .scheduleId(schedule.getId())
                .themes(
                        schedule.getScheduleThemes()
                                .stream()
                                .map(ScheduleTheme::getTheme)
                                .collect(Collectors.toList())
                )
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .build();
    }
}
