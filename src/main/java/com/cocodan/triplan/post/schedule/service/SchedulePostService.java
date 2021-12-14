package com.cocodan.triplan.post.schedule.service;

import com.cocodan.triplan.member.domain.Member;
import com.cocodan.triplan.member.dto.response.MemberGetOneResponse;
import com.cocodan.triplan.member.repository.MemberRepository;
import com.cocodan.triplan.post.schedule.domain.Like;
import com.cocodan.triplan.post.schedule.domain.SchedulePost;
import com.cocodan.triplan.post.schedule.domain.SchedulePostComment;
import com.cocodan.triplan.post.schedule.dto.request.SchedulePostCommentRequest;
import com.cocodan.triplan.post.schedule.dto.request.SchedulePostLikeRequest;
import com.cocodan.triplan.post.schedule.dto.request.SchedulePostRequest;
import com.cocodan.triplan.post.schedule.dto.response.SchedulePostCommentResponse;
import com.cocodan.triplan.post.schedule.dto.response.SchedulePostDetailResponse;
import com.cocodan.triplan.post.schedule.dto.response.SchedulePostResponse;
import com.cocodan.triplan.post.schedule.repository.SchedulePostCommentRepository;
import com.cocodan.triplan.post.schedule.repository.SchedulePostLikeRepository;
import com.cocodan.triplan.post.schedule.repository.SchedulePostRepository;
import com.cocodan.triplan.member.service.MemberService;
import com.cocodan.triplan.schedule.domain.Schedule;
import com.cocodan.triplan.schedule.domain.ScheduleTheme;
import com.cocodan.triplan.schedule.domain.vo.Theme;
import com.cocodan.triplan.schedule.repository.ScheduleRepository;
import com.cocodan.triplan.spot.domain.vo.City;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SchedulePostService {

    private final MemberService memberService;

    private final MemberRepository memberRepository;
    private final ScheduleRepository scheduleRepository;

    private final SchedulePostLikeRepository schedulePostLikeRepository;

    private final SchedulePostCommentRepository schedulePostCommentRepository;

    private final SchedulePostRepository schedulePostRepository;

    public SchedulePostService(
            MemberService memberService,
            MemberRepository memberRepository,
            ScheduleRepository scheduleRepository,
            SchedulePostLikeRepository schedulePostLikeRepository,
            SchedulePostCommentRepository schedulePostCommentRepository,
            SchedulePostRepository schedulePostRepository
    ) {
        this.memberService = memberService;
        this.memberRepository = memberRepository;
        this.scheduleRepository = scheduleRepository;
        this.schedulePostLikeRepository = schedulePostLikeRepository;
        this.schedulePostCommentRepository = schedulePostCommentRepository;
        this.schedulePostRepository = schedulePostRepository;
    }

    @Transactional(readOnly = true)
    public SchedulePost findById(Long id) {
        return schedulePostRepository.findById(id).orElseThrow(
                () -> new RuntimeException("There is no such schedule post of ID : " + id)
        );
    }

    public Long createSchedulePost(Long memberId, SchedulePostRequest request) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new RuntimeException("Invalid User Detected")
        );
        SchedulePost post = SchedulePost.builder()
                .member(member)
                .schedule(scheduleRepository.findById(request.getScheduleId()).orElseThrow(
                        () -> new RuntimeException("There is no such schedule (ID : " + request.getScheduleId() + ")")
                ))
                .title(request.title)
                .content(request.content)
                .views(0L)
                .liked(0L)
                .city(City.from(request.city))
                .build();

        SchedulePost savedSchedulePost = schedulePostRepository.save(post);
        return savedSchedulePost.getId();
    }

    @Transactional(readOnly = true)
    public SchedulePostDetailResponse getSchedulePostDetail(Long schedulePostId) {
        SchedulePost schedulePost = schedulePostRepository.findById(schedulePostId).orElseThrow(
                () -> new RuntimeException("No such post found (ID : " + schedulePostId + ")")
        );
        // TODO: 2021.12.13 Teru - 조회수에 대한 동시성 문제를 어떻게 해야 잘 해결할 수 있을지 고민... 현재는 별다른 처리를 해두지 않은 상태
        schedulePost.increaseViews();
        List<SchedulePostCommentResponse> comments = schedulePostCommentRepository.findAllBySchedulePostId(schedulePostId).stream()
                .map(schedulePostComment -> {
                    MemberGetOneResponse memberResponse = memberService.getOne(schedulePostComment.getMemberId());
                    return SchedulePostCommentResponse.of(
                            schedulePostComment,
                            memberResponse,
                            memberResponse.getId().equals(schedulePost.getMember().getId())
                    );
                }).collect(Collectors.toList());
        return SchedulePostDetailResponse.of(schedulePost, comments);
    }

    private List<SchedulePostResponse> convertToSchedulePostResponseList(List<SchedulePost> schedulePosts) {
        return schedulePosts.stream().map(schedulePost -> {
            MemberGetOneResponse memberResponse = memberService.getOne(schedulePost.getMember().getId());
            Schedule schedule = schedulePost.getSchedule();
            City city = schedulePost.getCity();
            List<Theme> themes = schedule.getScheduleThemes().stream()
                    .map(ScheduleTheme::getTheme).collect(Collectors.toList());
            String title = schedulePost.getTitle();

            return SchedulePostResponse.of(memberResponse, schedule, city, themes, title);
        }).collect(Collectors.toList());
    }

    private SchedulePost validateAuthorities(Long memberId, Long schedulePostId) {
        SchedulePost schedulePost = validateExistence(schedulePostId);
        validateOwnership(memberId, schedulePost);
        return schedulePost;
    }

    private void validateOwnership(Long memberId, SchedulePost schedulePost) {
        if (!memberId.equals(schedulePost.getMember().getId())) {
            throw new RuntimeException("Invalid access for schedule post. Only owner can access to it");
        }
    }

    private SchedulePost validateExistence(Long schedulePostId) {
        return schedulePostRepository.findById(schedulePostId).orElseThrow(
                () -> new RuntimeException("No schedule post found (ID : " + schedulePostId + ")")
        );

    }

    @Transactional
    public void deleteSchedulePost(Long memberId, Long schedulePostId) {
        SchedulePost schedulePost = validateAuthorities(memberId, schedulePostId);
        schedulePostRepository.delete(schedulePost);
    }

    @Transactional
    public void modifySchedulePost(Long memberId, Long schedulePostId, SchedulePostRequest request) {
        SchedulePost schedulePost = validateAuthorities(memberId, schedulePostId);

        schedulePost.updateTitle(request.getTitle());
        schedulePost.updateContent(request.getContent());
        schedulePost.updateCity(City.from(request.getCity()));

        schedulePostRepository.save(schedulePost);
    }

    @Transactional
    public Long toggleSchedulePostLiked(Long memberId, SchedulePostLikeRequest request) {
        // TODO: 2021.12.13 Teru - 좋아요 수에 대한 동시성 문제를 어떻게하면 더 잘 해결할 수 있을지 고민...
        Long schedulePostId = request.getSchedulePostId();
        Optional<Like> likeData =
                schedulePostLikeRepository.findByMemberIdAndSchedulePostId(memberId, schedulePostId);
        SchedulePost post = schedulePostRepository.findByIdForLikedCountUpdate(schedulePostId).orElseThrow(
                () -> new RuntimeException("No such schedule post found (ID : " + schedulePostId + ")")
        );

        if (likeData.isEmpty() && request.getFlag()) {
            Like like = new Like(memberId, post);
            schedulePostLikeRepository.save(like);
            return post.increaseLiked();
        }

        if (likeData.isPresent() && !request.getFlag()) {
            schedulePostLikeRepository.delete(likeData.get());
            return post.decreaseLiked();
        }

        // Invalid Like toggle
        return post.getLiked();
    }

    @Transactional(readOnly = true)
    public List<SchedulePostResponse> getLikedSchedulePosts(Long memberId) {
        List<Like> likeData = schedulePostLikeRepository.findAllByMemberId(memberId);
        List<SchedulePost> schedulePosts = likeData.stream().map(Like::getSchedulePost).collect(Collectors.toList());
        return convertToSchedulePostResponseList(schedulePosts);
    }

    @Transactional(readOnly = true)
    public List<SchedulePostCommentResponse> getSchedulePostComments(Long schedulePostId) {
        if (schedulePostId == null) {
            throw new RuntimeException("Invalid Request");
        }

        SchedulePost schedulePost = schedulePostRepository.findById(schedulePostId).orElseThrow(
                () -> new RuntimeException("No schedule post found (ID : " + schedulePostId + ")")
        );

        List<SchedulePostComment> schedulePostComments = schedulePostCommentRepository.findAllBySchedulePostId(schedulePostId);
        return schedulePostComments.stream().map(comment -> {
            MemberGetOneResponse memberResponse = memberService.getOne(comment.getMemberId());
            return SchedulePostCommentResponse.of(
                    comment,
                    memberResponse,
                    memberResponse.getId().equals(schedulePost.getId())
            );
        }).collect(Collectors.toList());
    }

    @Transactional
    public List<SchedulePostCommentResponse> writeSchedulePostComment(Long memberId, Long schedulePostId, SchedulePostCommentRequest request) {
        if (memberId == null || schedulePostId == null) {
            throw new RuntimeException("Invalid Request");
        }

        SchedulePost schedulePost = schedulePostRepository.findById(schedulePostId).orElseThrow(
                () -> new RuntimeException("No schedule post found (ID : " + schedulePostId + ")")
        );

        SchedulePostComment comment = SchedulePostComment.builder()
                .memberId(memberId)
                .schedulePost(schedulePost)
                .content(request.getContent())
                .build();

        schedulePostCommentRepository.save(comment);

        return getSchedulePostComments(schedulePostId);
    }
}
