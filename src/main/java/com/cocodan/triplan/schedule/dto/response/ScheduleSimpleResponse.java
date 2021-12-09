package com.cocodan.triplan.schedule.dto.response;

import com.cocodan.triplan.schedule.domain.vo.Theme;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class ScheduleSimpleResponse {

    private Long id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Theme> themas;

    @Builder
    public ScheduleSimpleResponse(Long id, String title, LocalDate startDate, LocalDate endDate, List<Theme> themas) {
        this.id = id;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.themas = themas;
    }
}
