package com.cocodan.triplan.schedule.dto.response;

import com.cocodan.triplan.schedule.domain.DailyScheduleSpot;
import com.cocodan.triplan.spot.domain.Spot;
import com.cocodan.triplan.spot.dto.response.SpotResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ScheduleSpotResponse {

    private final Long id;

    private final SpotResponse spotResponse;

    private final LocalDate date;

    private final int order;

    public static ScheduleSpotResponse of(Spot spot, DailyScheduleSpot dailyScheduleSpot) {
        return ScheduleSpotResponse.builder()
                .id(dailyScheduleSpot.getId())
                .date(dailyScheduleSpot.getDate())
                .order(dailyScheduleSpot.getOrder())
                .spotResponse(SpotResponse.from(spot))
                .build();
    }
}