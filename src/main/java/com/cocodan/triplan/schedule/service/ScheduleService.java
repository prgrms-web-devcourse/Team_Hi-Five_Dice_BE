package com.cocodan.triplan.schedule.service;

import com.cocodan.triplan.exception.common.ForbiddenException;
import com.cocodan.triplan.exception.common.NoFriendsException;
import com.cocodan.triplan.exception.common.NotFoundException;
import com.cocodan.triplan.exception.common.NotIncludeException;
import com.cocodan.triplan.friend.repository.FriendRepository;
import com.cocodan.triplan.jwt.JwtAuthentication;
import com.cocodan.triplan.member.domain.Member;
import com.cocodan.triplan.member.dto.response.MemberSimpleResponse;
import com.cocodan.triplan.member.repository.MemberRepository;
import com.cocodan.triplan.schedule.domain.*;
import com.cocodan.triplan.schedule.domain.vo.Theme;
import com.cocodan.triplan.schedule.dto.request.*;
import com.cocodan.triplan.schedule.dto.response.*;
import com.cocodan.triplan.schedule.repository.ChecklistRepository;
import com.cocodan.triplan.schedule.repository.MemoRepository;
import com.cocodan.triplan.schedule.repository.ScheduleRepository;
import com.cocodan.triplan.schedule.repository.VotingRepository;
import com.cocodan.triplan.spot.domain.Spot;
import com.cocodan.triplan.spot.service.SpotService;
import com.cocodan.triplan.util.ExceptionMessageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    private final SpotService spotService;

    private final MemberRepository memberRepository;

    private final MemoRepository memoRepository;

    private final ChecklistRepository checklistRepository;

    private final VotingRepository votingRepository;

    private final FriendRepository friendRepository;

    @Transactional
    public Long saveSchedule(ScheduleCreationRequest scheduleCreationRequest, Long memberId) {
        Schedule schedule = convertSchedule(scheduleCreationRequest, memberId);

        createOwner(schedule, memberId);

        scheduleCreationRequest.getDailyScheduleSpotCreationRequests()
                .forEach(this::saveSpot);

        Schedule save = scheduleRepository.save(schedule);

        addScheduleMember(scheduleCreationRequest, save);

        return save.getId();
    }

    private void addScheduleMember(ScheduleCreationRequest scheduleCreationRequest, Schedule save) {
        for (Long id : scheduleCreationRequest.getIdsOfFriends()) {
            createScheduleMember(save, id);
        }
    }

    private void saveSpot(DailyScheduleSpotCreationRequest dailyScheduleSpotCreationRequest) {
        if (isNotSavedSpot(dailyScheduleSpotCreationRequest)) {
            spotService.createSpot(dailyScheduleSpotCreationRequest);
        }
    }

    private Schedule convertSchedule(ScheduleCreationRequest scheduleCreationRequest, Long memberId) {
        Schedule schedule = createSchedule(scheduleCreationRequest, memberId);

        createScheduleThemes(scheduleCreationRequest.getThemes(), schedule);

        createScheduleDailySpots(scheduleCreationRequest, schedule);

        return schedule;
    }

    private void createOwner(Schedule schedule, Long memberId) {
        ScheduleMember.builder()
                .schedule(schedule)
                .memberId(memberId)
                .build();
    }

    private Schedule createSchedule(ScheduleCreationRequest scheduleCreationRequest, Long memberId) {
        return Schedule.builder()
                .title(scheduleCreationRequest.getTitle())
                .startDate(scheduleCreationRequest.getStartDate())
                .endDate(scheduleCreationRequest.getEndDate())
                .memberId(memberId)
                .build();
    }

    private void createScheduleThemes(List<String> themes, Schedule schedule) {
        themes
                .stream()
                .map(s -> Theme.from(s.toUpperCase()))
                .forEach(theme -> new ScheduleTheme(schedule, theme));
    }

    private void createScheduleDailySpots(ScheduleCreationRequest scheduleCreationRequest, Schedule schedule) {
        scheduleCreationRequest.getDailyScheduleSpotCreationRequests()
                .forEach(request -> createDailyScheduleSpot(schedule, request));
    }

    private void createDailyScheduleSpot(
            Schedule schedule,
            DailyScheduleSpotCreationRequest dailyScheduleSpotCreationRequest
    ) {
        DailyScheduleSpot.builder()
                .spotId(dailyScheduleSpotCreationRequest.getSpotId())
                .placeName(dailyScheduleSpotCreationRequest.getPlaceName())
                .dateOrder(dailyScheduleSpotCreationRequest.getDateOrder())
                .spotOrder(dailyScheduleSpotCreationRequest.getSpotOrder())
                .schedule(schedule)
                .build();
    }

    private boolean isNotSavedSpot(DailyScheduleSpotCreationRequest dailyScheduleSpotCreationRequest) {
        return !spotService.existsById(dailyScheduleSpotCreationRequest.getSpotId());
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedules(Long memberId) {
        return scheduleRepository.findByMemberId(memberId)
                .stream()
                .map(ScheduleResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ScheduleResponse getSchedule(Long scheduleId) {
        Schedule schedule = scheduleRepository.findOneWithSpotsById(scheduleId)
                .orElseThrow(
                        () -> new NotFoundException(
                                MessageFormat.format(
                                        "{} :: Schedule Not Found. " +
                                                "There does not exist a Schedule with the given ID : {}",
                                                // "\n\tRequested by a member(ID: {}, Token: {}",
                                        LocalDateTime.now(),
                                        scheduleId
                                        // authentication.getId(),
                                        // authentication.getToken()
                                )
                        )
                );

        List<Long> scheduleMemberIds = getMemberIds(schedule);

        List<Member> members = memberRepository.findByIdIn(scheduleMemberIds);

        List<Spot> spots = spotService.findByIdIn(getSpotIds(schedule));

        return ScheduleResponse.of(schedule, spots, members);
    }

    private List<Long> getMemberIds(Schedule schedule) {
        return schedule.getScheduleMembers().stream()
                .map(ScheduleMember::getMemberId)
                .collect(Collectors.toList());
    }

    private List<Long> getSpotIds(Schedule dailyScheduleSpots) {
        return dailyScheduleSpots.getDailyScheduleSpots().stream()
                .map(DailyScheduleSpot::getSpotId)
                .collect(Collectors.toList());
    }

    @Transactional
    public void modifySchedule(Long scheduleId, ScheduleModificationRequest scheduleModificationRequest, Long memberId) {
        Schedule schedule = findScheduleById(scheduleId);

        validateScheduleMember(scheduleId, memberId);

        scheduleModificationRequest.getDailyScheduleSpotCreationRequests()
                .forEach(this::saveSpot);

        schedule.updateTitle(scheduleModificationRequest.getTitle());

        schedule.clearThemes();
        createScheduleThemes(scheduleModificationRequest.getThemes(), schedule);

        schedule.removeAllSpots();
        convertDailyScheduleSpotList(schedule, scheduleModificationRequest);
    }

    private Schedule findScheduleById(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(
                        () -> new NotFoundException(
                                // TODO: [TP-126] Teru - 로깅 양식 통일 -> Controller & Service 전반적인 수정 필요
                                MessageFormat.format(
                                        "{} :: Schedule Not Found. " +
                                                "There does not exist a Schedule with the given ID : {}",
                                                // "\n\tRequested by a member(ID: {}, Token: {}",
                                        LocalDateTime.now(),
                                        scheduleId
                                        // authentication.getId(),
                                        // authentication.getToken()
                                )
                        )
                );
    }

    private void validateScheduleMember(Long scheduleId, Long memberId) {
        if (scheduleRepository.countByScheduleIdAndMemberId(scheduleId, memberId) == 0) {
            throw new ForbiddenException(Schedule.class, scheduleId, memberId);
        }
    }

    private void convertDailyScheduleSpotList(
            Schedule schedule,
            ScheduleModificationRequest scheduleModificationRequest
    ) {
        scheduleModificationRequest.getDailyScheduleSpotCreationRequests()
                .forEach(request -> createDailyScheduleSpot(schedule, request));
    }

    @Transactional
    public void deleteSchedule(Long scheduleId, Long memberId) {
        Schedule schedule = findScheduleById(scheduleId);

        if (!isConstructor(schedule, memberId)) {
            throw new ForbiddenException(Schedule.class, schedule.getId(), memberId);
        }

        scheduleRepository.delete(schedule);
    }

    private boolean isConstructor(Schedule schedule, Long memberId) {
        return schedule.getMemberId().equals(memberId);
    }

    @Transactional
    public Long saveMemo(Long scheduleId, MemoRequest memoRequest, Long memberId) {
        Schedule schedule = findScheduleById(scheduleId);

        validateScheduleMember(scheduleId, memberId);

        Memo memo = createMemo(memoRequest, schedule, memberId);

        return memoRepository.save(memo).getId();
    }

    private Memo createMemo(MemoRequest memoRequest, Schedule schedule, Long memberId) {
        return Memo.builder()
                .schedule(schedule)
                .title(memoRequest.getTitle())
                .content(memoRequest.getContent())
                .memberId(memberId)
                .build();
    }


    @Transactional(readOnly = true)
    public List<MemoResponse> getMemos(Long scheduleId, Long memberId) {
        validateScheduleMember(scheduleId, memberId);

        return memoRepository.findByScheduleId(scheduleId)
                .stream()
                .map(MemoResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MemoResponse getMemo(Long scheduleId, Long memoId, Long memberId) {
        Memo memo = findMemoById(memoId);

        validateScheduleMemo(scheduleId, memoId);

        validateScheduleMember(scheduleId, memberId);

        Long ownerId = memo.getMemberId();

        Member member = findMemberById(ownerId);

        return MemoResponse.of(memo, member);
    }

    private Member findMemberById(Long ownerId) {
        return memberRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException(
                        MessageFormat.format(
                                "{} :: Member Not Found. " +
                                        "There does not exist a Member with the given ID : {}",
                                        // "\n\tRequested by a member(ID: {}, Token: {}",
                                LocalDateTime.now(),
                                ownerId
                                // authentication.getId(),
                                // authentication.getToken()
                        )
                ));
    }

    private Memo findMemoById(Long memoId) {
        return memoRepository.findById(memoId)
                .orElseThrow(() -> new NotFoundException(
                        MessageFormat.format(
                                "{} :: Memo Not Found. " +
                                        "There does not exist a Memo with the given ID : {}",
                                        // "\n\tRequested by a member(ID: {}, Token: {}",
                                LocalDateTime.now(),
                                memoId
                                // authentication.getId(),
                                // authentication.getToken()
                        )
                ));
    }

    private void validateScheduleMemo(Long scheduleId, Long memoId) {
        boolean notExists = !memoRepository.existsByIdAndScheduleId(memoId, scheduleId);

        if (notExists) {
            throw new NotIncludeException(Schedule.class, Memo.class, scheduleId, memoId);
        }
    }


    @Transactional
    public void modifyMemo(Long scheduleId, Long memoId, MemoRequest memoRequest, Long memberId) {
        Memo memo = findMemoById(memoId);

        validateScheduleMemo(scheduleId, memoId);

        validateMemoOwner(memo, memberId);

        memo.modify(memoRequest.getTitle(), memoRequest.getContent());
    }

    private void validateMemoOwner(Memo memo, Long memberId) {
        if (!memo.getMemberId().equals(memberId)) {
            throw new ForbiddenException(Memo.class, memo.getId(), memberId);
        }
    }

    @Transactional
    public void deleteMemo(Long scheduleId, Long memoId, Long memberId) {
        Memo memo = findMemoById(memoId);

        validateScheduleMemo(scheduleId, memoId);

        validateMemoOwner(memo, memberId);

        memoRepository.delete(memo);
    }

    // 체크리스트
    @Transactional
    public Long saveChecklist(Long scheduleId, ChecklistCreationRequest checklistCreationRequest, Long memberId) {
        Schedule schedule = findScheduleById(scheduleId);

        validateScheduleMember(scheduleId, memberId);

        Checklist checklist = createChecklist(checklistCreationRequest, schedule);

        return checklistRepository.save(checklist).getId();
    }

    private Checklist createChecklist(ChecklistCreationRequest checklistCreationRequest, Schedule schedule) {
        return Checklist.builder()
                .title(checklistCreationRequest.getTitle())
                .schedule(schedule)
                .day(checklistCreationRequest.getDay())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ChecklistResponse> getChecklists(Long scheduleId, Long memberId) {
        validateScheduleMember(scheduleId, memberId);

        return checklistRepository.findByScheduleId(scheduleId).stream()
                .map(ChecklistResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void doCheck(Long scheduleId, Long checklistId, Long memberId, boolean flag) {
        Checklist checklist = findChecklistById(checklistId);

        validateScheduleChecklist(scheduleId, checklistId);

        validateScheduleMember(scheduleId, memberId);

        checklist.check(flag);
    }

    private Checklist findChecklistById(Long checklistId) {
        return checklistRepository.findById(checklistId)
                .orElseThrow(() -> new NotFoundException(
                        MessageFormat.format(
                                "{} :: Checklist Not Found. " +
                                        "There does not exist a Checklist with the given ID : {}",
                                        // "\n\tRequested by a member(ID: {}, Token: {}",
                                LocalDateTime.now(),
                                checklistId
                                // authentication.getId(),
                                // authentication.getToken()
                        )
                ));
    }

    private void validateScheduleChecklist(Long scheduleId, Long checklistId) {
        boolean notExists = !checklistRepository.existsByIdAndScheduleId(checklistId, scheduleId);

        if (notExists) {
            throw new NotIncludeException(Schedule.class, Checklist.class, scheduleId, checklistId);
        }
    }

    @Transactional
    public void deleteChecklist(Long scheduleId, Long checklistId, Long memberId) {
        Checklist checklist = findChecklistById(checklistId);

        validateScheduleChecklist(scheduleId, checklistId);

        validateScheduleMember(scheduleId, memberId);

        checklistRepository.delete(checklist);
    }


    // 투표
    @Transactional
    public Long saveVoting(Long scheduleId, VotingCreationRequest votingCreationRequest, Long memberId) {
        Schedule schedule = findScheduleById(scheduleId);

        validateScheduleMember(scheduleId, memberId);

        Voting voting = createVoting(votingCreationRequest, memberId, schedule);

        createVotingContents(votingCreationRequest, voting);

        return votingRepository.save(voting).getId();
    }

    private Voting createVoting(VotingCreationRequest votingCreationRequest, Long memberId, Schedule schedule) {
        return Voting.builder()
                .schedule(schedule)
                .title(votingCreationRequest.getTitle())
                .multipleFlag(votingCreationRequest.isMultipleFlag())
                .memberId(memberId)
                .build();
    }

    private void createVotingContents(VotingCreationRequest votingCreationRequest, Voting voting) {
        votingCreationRequest.getContents()
                .forEach(content -> createVotingContent(voting, content));
    }

    private void createVotingContent(Voting voting, String votingContentName) {
        VotingContent.builder()
                .voting(voting)
                .content(votingContentName)
                .build();
    }

    private void validateScheduleVoting(Long scheduleId, Long votingId) {
        boolean notExists = !votingRepository.existsByIdAndScheduleId(votingId, scheduleId);

        if (notExists) {
            throw new NotIncludeException(Schedule.class, Voting.class, scheduleId, votingId);
        }
    }

    @Transactional(readOnly = true)
    public List<VotingResponse> getVotingList(Long scheduleId, Long memberId) {
        validateScheduleMember(scheduleId, memberId);

        return votingRepository.findByScheduleId(scheduleId)
                .stream()
                .map(VotingResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VotingResponse getVoting(Long scheduleId, Long votingId, Long memberId) {
        Voting voting = findVotingById(votingId);

        validateScheduleVoting(scheduleId, votingId);

        validateScheduleMember(scheduleId, memberId);

        Long ownerId = voting.getMemberId();

        Member owner = findMemberById(ownerId);

        return VotingResponse.of(voting, owner, memberId);
    }

    private Voting findVotingById(Long votingId) {
        return votingRepository.findById(votingId)
                .orElseThrow(() -> new NotFoundException(
                        MessageFormat.format(
                                "{} :: Voting Not Found. " +
                                        "There does not exist a Voting with the given ID : {}",
                                        // "\n\tRequested by a member(ID: {}, Token: {}",
                                LocalDateTime.now(),
                                votingId
                                // authentication.getId(),
                                // authentication.getToken()
                        )
                ));
    }

    @Transactional
    public void doVote(Long scheduleId, Long votingId, VotingRequest votingRequest, Long memberId) {
        Voting voting = findVotingById(votingId);

        validateScheduleVoting(scheduleId, votingId);

        validateScheduleMember(scheduleId, memberId);

        voting.vote(votingRequest.getVotingMap(), memberId);
    }

    @Transactional
    public void deleteVoting(Long scheduleId, Long votingId, Long memberId) {
        Voting voting = findVotingById(votingId);

        validateScheduleVoting(scheduleId, votingId);

        validateVotingOwner(voting, memberId);

        votingRepository.delete(voting);
    }

    private void validateVotingOwner(Voting voting, Long memberId) {
        if (!voting.getMemberId().equals(memberId)) {
            throw new ForbiddenException(Voting.class, voting.getId(), memberId);
        }
    }

    // 여행 멤버
    @Transactional
    public void addScheduleMember(Long scheduleId, ScheduleMemberRequest scheduleMemberRequest, Long memberId) {
        Schedule schedule = findScheduleById(scheduleId);

        validateScheduleMember(scheduleId, memberId);

        long friendId = scheduleMemberRequest.getFriendId();

        validateFriends(friendId, memberId);

        createScheduleMember(schedule, friendId);
    }

    private void createScheduleMember(Schedule schedule, long friendId) {
        ScheduleMember.builder()
                .memberId(friendId)
                .schedule(schedule)
                .build();
    }

    private void validateFriends(long friendId, Long memberId) {
        friendRepository.findByFromIdAndToId(memberId, friendId)
                .orElseThrow(() -> new NoFriendsException(friendId, memberId));
    }

    @Transactional(readOnly = true)
    public List<MemberSimpleResponse> getScheduleMembers(Long scheduleId, Long memberId) {
        Schedule schedule = findScheduleById(scheduleId);

        validateScheduleMember(scheduleId, memberId);

        List<Long> ids = schedule.getScheduleMembers().stream()
                .map(ScheduleMember::getMemberId)
                .collect(Collectors.toList());

        return memberRepository.findByIdIn(ids)
                .stream()
                .map(MemberSimpleResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteScheduleMember(Long scheduleId, Long deletedId, Long memberId) {
        Schedule schedule = findScheduleById(scheduleId);

        validateScheduleMember(scheduleId, memberId);

        if (isConstructor(schedule, deletedId)) {
            throw new IllegalArgumentException(ExceptionMessageUtils.getMessage("exception.bad_request"));
        }

        ScheduleMember deletedMember = findDeletedMember(schedule, deletedId);

        schedule.deleteScheduleMember(deletedMember);
    }

    private ScheduleMember findDeletedMember(Schedule schedule, Long deletedId) {
        return schedule.getScheduleMembers().stream()
                .filter(scheduleMember -> scheduleMember.getMemberId().equals(deletedId))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                ExceptionMessageUtils.getMessage("exception.bad_request")
                        )
                );
    }

    @Transactional
    public void exitSchedule(Long scheduleId, Long memberId) {
        Schedule schedule = findScheduleById(scheduleId);

        validateScheduleMember(scheduleId, memberId);

        if (memberId.equals(schedule.getMemberId())) {
            scheduleRepository.delete(schedule);
            return;
        }

        ScheduleMember deletedMember = findDeletedMember(schedule, memberId);

        schedule.deleteScheduleMember(deletedMember);
    }
}
