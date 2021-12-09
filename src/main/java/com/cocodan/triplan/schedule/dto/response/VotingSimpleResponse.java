package com.cocodan.triplan.schedule.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VotingSimpleResponse {

    private Long id;
    private String title;
    private int memberCount;
}
