package com.cocodan.triplan.post.schedule.controller;

import com.cocodan.triplan.member.domain.Member;
import com.cocodan.triplan.post.schedule.dto.request.SchedulePostCreateRequest;
import com.cocodan.triplan.post.schedule.dto.response.SchedulePostCreateResponse;
import com.cocodan.triplan.post.schedule.dto.response.SchedulePostDetailResponse;
import com.cocodan.triplan.post.schedule.dto.response.SchedulePostResponse;
import com.cocodan.triplan.post.schedule.service.SchedulePostService;
import com.cocodan.triplan.post.schedule.vo.SchedulePostSortingRule;
import com.cocodan.triplan.schedule.domain.vo.Theme;
import com.cocodan.triplan.spot.domain.vo.City;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.cocodan.triplan.post.schedule.controller.SchedulePostController.schedulePostBaseUri;

@RequestMapping(schedulePostBaseUri)
@RestController
public class SchedulePostController {

    public static final String schedulePostBaseUri = "/posts";

    private final SchedulePostService schedulePostService;

    public SchedulePostController(SchedulePostService schedulePostService) {
        this.schedulePostService = schedulePostService;
    }

    @GetMapping("/schedules")
    public ResponseEntity<List<SchedulePostResponse>> schedulePostList(
            @RequestParam(defaultValue = "0") Integer pageIndex,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "전체") String searchingCity,
            @RequestParam(defaultValue = "ALL") String searchingTheme,
            @RequestParam(defaultValue = "최신순") String sorting
    ) {
        City city = City.of(searchingCity);
        Theme theme = Theme.valueOf(searchingTheme);
        SchedulePostSortingRule sortRule = SchedulePostSortingRule.of(sorting);

        List<SchedulePostResponse> schedulePostList
                = schedulePostService.getSchedulePosts(search, city, theme, sortRule, pageIndex);

        return ResponseEntity.ok(schedulePostList);
        // TODO: 검색 효율성 개선
    }

    @PostMapping("/schedules")
    public ResponseEntity<SchedulePostCreateResponse> createSchedulePost(@RequestBody SchedulePostCreateRequest request) {
        Member member = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long postId = schedulePostService.createSchedulePost(member.getId(), request);
        return ResponseEntity.ok(SchedulePostCreateResponse.from(postId));
    }

    @GetMapping("/schedules/{schedulePostId}")
    public ResponseEntity<SchedulePostDetailResponse> detailSchedulePost(@PathVariable Long schedulePostId) {
        SchedulePostDetailResponse schedulePostDetail = schedulePostService.getSchedulePostDetail(schedulePostId);
        return ResponseEntity.ok(schedulePostDetail);
    }

    @DeleteMapping("/schedules/{schedulePostId}")
    public ResponseEntity<Void> deleteSchedulePost(@PathVariable Long schedulePostId) {
        Member member = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        schedulePostService.validateRemovable(member.getId(), schedulePostId);
        schedulePostService.deleteSchedulePost(schedulePostId);
        return ResponseEntity.ok().build();
    }
}
