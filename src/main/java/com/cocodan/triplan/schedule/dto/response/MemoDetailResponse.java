package com.cocodan.triplan.schedule.dto.response;

import com.cocodan.triplan.member.domain.Member;
import com.cocodan.triplan.member.dto.response.MemberSimpleResponse;
import com.cocodan.triplan.schedule.domain.Memo;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class MemoDetailResponse {

    private final Long id;

    private final String title;

    private final String content;

    private final MemberSimpleResponse memberSimpleResponse;

    public static MemoDetailResponse of(Memo memo, Member member) {
        return MemoDetailResponse.builder()
                .id(memo.getId())
                .title(memo.getTitle())
                .content(memo.getContent())
                .memberSimpleResponse(
                        MemberSimpleResponse.from(member)
                )
                .build();
    }
}