package com.cocodan.triplan.post.schedule.dto.response;

import com.cocodan.triplan.member.domain.vo.GenderType;
import com.cocodan.triplan.member.dto.response.MemberGetOneResponse;
import com.cocodan.triplan.post.schedule.domain.SchedulePostComment;
import com.cocodan.triplan.post.schedule.vo.Ages;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SchedulePostCommentResponse {

    private String nickname;

    private Ages ages;

    private GenderType gender;

    private String content;

    private LocalDateTime createdAt;

    private Long memberId;

    private boolean schedulePostWriter;

    public static SchedulePostCommentResponse of(SchedulePostComment comment, MemberGetOneResponse member, boolean schedulePostWriter) {
        return SchedulePostCommentResponse.builder()
                .nickname(member.getNickname())
                .memberId(member.getId())
                .ages(Ages.from(member.getBirth()))
                .gender(member.getGender())
                .content(comment.getContent())
                .createdAt(comment.getCreatedDate())
                .schedulePostWriter(schedulePostWriter)
                .build();
    }
}
