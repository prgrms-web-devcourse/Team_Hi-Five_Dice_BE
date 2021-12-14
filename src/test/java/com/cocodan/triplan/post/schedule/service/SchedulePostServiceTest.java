package com.cocodan.triplan.post.schedule.service;

import com.cocodan.triplan.member.domain.vo.GenderType;
import com.cocodan.triplan.member.dto.response.MemberGetOneResponse;
import com.cocodan.triplan.member.service.MemberService;
import com.cocodan.triplan.post.schedule.domain.SchedulePost;
import com.cocodan.triplan.post.schedule.dto.request.SchedulePostLikeRequest;
import com.cocodan.triplan.post.schedule.dto.request.SchedulePostRequest;
import com.cocodan.triplan.post.schedule.dto.response.SchedulePostDetailResponse;
import com.cocodan.triplan.post.schedule.dto.response.SchedulePostResponse;
import com.cocodan.triplan.post.schedule.vo.Ages;
import com.cocodan.triplan.post.schedule.vo.SchedulePostSortingRule;
import com.cocodan.triplan.schedule.domain.Schedule;
import com.cocodan.triplan.schedule.domain.ScheduleTheme;
import com.cocodan.triplan.schedule.domain.vo.Theme;
import com.cocodan.triplan.schedule.dto.request.DailyScheduleSpotCreationRequest;
import com.cocodan.triplan.schedule.dto.request.Position;
import com.cocodan.triplan.schedule.dto.request.ScheduleCreationRequest;
import com.cocodan.triplan.schedule.dto.response.DailyScheduleSpotResponse;
import com.cocodan.triplan.schedule.service.ScheduleService;
import com.cocodan.triplan.spot.domain.vo.City;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SchedulePostServiceTest {

    @Autowired
    SchedulePostService schedulePostService;

    @Autowired
    ScheduleService scheduleService;

    @Autowired
    MemberService memberService;

    private static Long testMemberId;

    private static Long createdScheduleId;

    private static final String EMAIL = "KimLeePark@gmail.com";
    private static final String NAME = "김이박";
    private static final String PHONE = "01077775555";
    private static final String BIRTH = "19901111";
    private static final String GENDER = GenderType.MALE.getTypeStr();
    private static final String NICKNAME = "TestNickname";
    private static final String PROFILE_IMAGE = "https://wwww.someonesownserver.org/img/1";

    @BeforeAll
    void setup() {
        testMemberId = memberService.create(
                EMAIL,
                NAME,
                PHONE,
                BIRTH,
                GENDER,
                NICKNAME,
                PROFILE_IMAGE
        ).getId();
    }

    @Test
    @DisplayName("생성된 여행 공유 게시글 리스트를 정상적으로 조회 할 수 있다.")
    @Transactional
    void getRecentSchedulePostList() {
        // 여행 생성
        ScheduleCreationRequest scheduleCreationRequest = createScheduleCreation();
        Long createdScheduleId = scheduleService.saveSchedule(scheduleCreationRequest, testMemberId);

        // 여행 공유 게시글 생성
        SchedulePostRequest request = SchedulePostRequest.builder()
                .title("1번 여행 게시글")
                .content("1번 여행 게시글 본문")
                .city("서울")
                .scheduleId(createdScheduleId)
                .build();
        Long createdSchedulePostId = schedulePostService.createSchedulePost(testMemberId, request);

        // 여행 공유 게시글 조회
        List<SchedulePostResponse> posts = schedulePostService.getSchedulePosts("", City.ALL, Theme.ALL, SchedulePostSortingRule.RECENT, 0);
        assertThat(posts.size()).isEqualTo(1);
        assertThat(posts.get(0).getProfileImageUrl()).isEqualTo(PROFILE_IMAGE);
        assertThat(posts.get(0).getNickname()).isEqualTo(NICKNAME);
        assertThat(posts.get(0).getTitle()).isEqualTo("1번 여행 게시글");
        assertThat(posts.get(0).getGenderType().getTypeStr()).isEqualTo(GENDER);
        assertThat(posts.get(0).getCity()).isEqualTo(City.SEOUL);
        assertThat(posts.get(0).getStartDate()).isEqualTo(LocalDate.of(2021, 12, 1));
        assertThat(posts.get(0).getEndDate()).isEqualTo(LocalDate.of(2021, 12, 3));
        assertThat(posts.get(0).getThemes()).contains(Theme.ACTIVITY, Theme.FOOD);

        // TODO: 다양한 조건으로 테스트 추가
    }

    private ScheduleCreationRequest createScheduleCreation() {
        return new ScheduleCreationRequest("title", LocalDate.of(2021, 12, 1), LocalDate.of(2021, 12, 3), List.of("activity", "food"),
                List.of(new DailyScheduleSpotCreationRequest(1L, "address1", "roadAddress1", "010-1111-2222", "불국사1", new Position(37.1234, 125.3333), LocalDate.of(2021, 12, 1), 1),
                        new DailyScheduleSpotCreationRequest(2L, "address2", "roadAddress2", "010-1111-2223", "불국사2", new Position(37.1234, 125.3333), LocalDate.of(2021, 12, 1), 2),
                        new DailyScheduleSpotCreationRequest(3L, "address3", "roadAddress3", "010-1111-2224", "불국사3", new Position(37.1234, 125.3333), LocalDate.of(2021, 12, 1), 3),
                        new DailyScheduleSpotCreationRequest(4L, "address4", "roadAddress4", "010-1111-2225", "불국사4", new Position(37.1234, 125.3333), LocalDate.of(2021, 12, 2), 1),
                        new DailyScheduleSpotCreationRequest(5L, "address5", "roadAddress5", "010-1111-2226", "불국사5", new Position(37.1234, 125.3333), LocalDate.of(2021, 12, 2), 2),
                        new DailyScheduleSpotCreationRequest(6L, "address6", "roadAddress6", "010-1111-2227", "불국사6", new Position(37.1234, 125.3333), LocalDate.of(2021, 12, 2), 3),
                        new DailyScheduleSpotCreationRequest(7L, "address7", "roadAddress7", "010-1111-2228", "불국사7", new Position(37.1234, 125.3333), LocalDate.of(2021, 12, 3), 1),
                        new DailyScheduleSpotCreationRequest(8L, "address8", "roadAddress8", "010-1111-2229", "불국사8", new Position(37.1234, 125.3333), LocalDate.of(2021, 12, 3), 2)
                ));
    }

    private Long createSchedulePost1() {
        ScheduleCreationRequest scheduleCreationRequest = createScheduleCreation();
        createdScheduleId = scheduleService.saveSchedule(scheduleCreationRequest, testMemberId);
        SchedulePostRequest request = SchedulePostRequest.builder()
                .title("1번 여행!")
                .content("Apple Inc. is an American multinational technology company that specializes in consumer electronics, computer software and online services. Apple is the largest information technology company by revenue (totaling $274.5 billion in 2020) and, since January 2021, the world's most valuable company. As of 2021, Apple is the fourth-largest PC vendor by unit sales[9] and fourth-largest smartphone manufacturer.[10][11] It is one of the Big Five American information technology companies, alongside Amazon, Google (Alphabet), Facebook (Meta), and Microsoft.[12][13][14]\n" +
                        "\n" +
                        "Apple was founded in 1976 by Steve Jobs, Steve Wozniak and Ronald Wayne to develop and sell Wozniak's Apple I personal computer. It was incorporated by Jobs and Wozniak as Apple Computer, Inc. in 1977, and sales of its computers, among them the Apple II, grew quickly. It went public in 1980, to instant financial success. Over the next few years, Apple shipped new computers featuring innovative graphical user interfaces, such as the original Macintosh, announced in a critically acclaimed advertisement, \"1984\", directed by Ridley Scott. The high cost of its products and limited application library caused problems, as did power struggles between executives. In 1985, Wozniak departed Apple amicably,[15] while Jobs resigned to found NeXT, taking some Apple employees with him.[16]\n" +
                        "\n" +
                        "As the market for personal computers expanded and evolved throughout the 1990s, Apple lost considerable market share to the lower-priced duopoly of Microsoft Windows on Intel PC clones. The board recruited CEO Gil Amelio, who prepared the struggling company for eventual success with extensive reforms, product focus and layoffs in his 500-day tenure. In 1997, Amelio bought NeXT to resolve Apple's unsuccessful operating-system strategy and entice Jobs back to the company; he replaced Amelio. Apple became profitable again through a number of tactics. First, a revitalizing campaign called \"Think different\", and by launching the iMac and iPod. In 2001, it opened a retail chain, the Apple Stores, and has acquired numerous companies to broaden its software portfolio. In 2007, the company launched the iPhone to critical acclaim and financial success. Jobs resigned in 2011 for health reasons, and died two months later. He was succeeded as CEO by Tim Cook.\n" +
                        "\n" +
                        "The company receives significant criticism regarding the labor practices of its contractors, its environmental practices, and its business ethics, including anti-competitive behavior and materials sourcing. In August 2018, Apple became the first publicly traded U.S. company to be valued at over $1 trillion,[17][18] and, two years later, the first valued at over $2 trillion.[19][20] The company enjoys a high level of brand loyalty, and is ranked as the world's most valuable brand; as of January 2021, there are 1.65 billion Apple products in active use.[21]")
                .city("서울")
                .scheduleId(createdScheduleId)
                .build();
        return schedulePostService.createSchedulePost(testMemberId, request);
    }

    private Long createSchedulePost2() {
        ScheduleCreationRequest scheduleCreationRequest2 = createScheduleCreation();
        Long createdScheduleId2 = scheduleService.saveSchedule(scheduleCreationRequest2, testMemberId);
        SchedulePostRequest request2 = SchedulePostRequest.builder()
                .title("2번 여행!")
                .content("삼성 그룹(三星-, The Samsung Group, 약칭: 삼성, Samsung)은 대한민국에 본사를 둔 다국적 기업집단이다.\n" +
                        "\n" +
                        "처음에는 이병철 창업주가 삼성물산이라는 이름으로 자본금 3만 원(현재가치 3억 원)에 지금의 삼성 그룹을 창립하였다.\n" +
                        "\n" +
                        "\"삼성 그룹\"이라는 상호의 회사는 실제로 존재하는 것이 아니며, 삼성전자를 중심으로, 삼성물산, 삼성생명 등 다수의 자회사를 두고 있다. 공정거래위원회는 삼성전자와 그 계열사를 1987년 대규모 기업집단으로 지정하였다.\n" +
                        "\n" +
                        "1938년에 이병철이 대구에서 삼성상회를 창립하였고 그 탓인지 삼성 라이온즈가 대구에서 창단될 당시 대구에 거점을 둔 삼성 그룹 계열사가 제일모직 밖에 없었던 것 때문에 김재하 전 대구 FC 단장 등[1] 임원들이 제일모직에서 삼성 라이온즈로 대거 차출됐으며 대구를 연고로 한 제일모직 축구단 선수 중 한 명이었던 김호는 뒷날 수원 삼성 블루윙즈 초대감독을 역임했는데 이 팀은 대구가 한때 연고지 물망에[2] 올랐었고 제일모직 부지 안에 잔디축구장이 조성된 데다[3] 대구시민운동장이 바로 옆에 붙어있었던 탓인지 제일모직 축구단은 제일모직 잔디축구장과 대구시민운동장을 모두 이용했다. 이후 \"삼성\"이라는 상호 아래 여러 계열사를 설립하면서 그 규모를 키웠으며, 1950년대 후반, 인수합병의 대표주자로 나서면서 오늘날 재계 서열 1위의 거대 기업집단으로 성장하였다.\n" +
                        "\n" +
                        "또한 삼성 그룹은 2013년 380조원 규모의 매출을 올렸다. 한국은행에 따르면 같은 해 대한민국의 명목 국내총생산(GDP)은 1428조 원이다.[4] 해외 매출 비중이 훨씬 큰 삼성의 매출액은 GDP와 직접 비교하기 어렵지만, 그럼에도 삼성의 매출액이 대한민국 GDP의 26.6%나 차지한다는 점은 시사하는 바가 크다.[4] 삼성의 수출은 2013년 1572억 달러로 한국 전체 수출액 6171억 달러의 25%에 해당한다.[4]\n" +
                        "\n" +
                        "삼성 그룹은 브랜드 파이낸스에서 선정하는 글로벌 브랜드가치순위 500대 기업에서 2018년 기준 4위에 올랐다. 브랜드 파이낸스는 매년 세계 기업의 브랜드가치를 평가하여 보고서를 작성, 브랜드가치 500대기업을 발표하고있는데, 브랜드 파이낸스는 2018년 삼성의 브랜드가치가 92289백만달러(약 104조원)의 가치를 지녔다고 평가했다.")
                .city("부산")
                .scheduleId(createdScheduleId)
                .build();
        return schedulePostService.createSchedulePost(testMemberId, request2);
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

    @Test
    @DisplayName("여행 공유 게시글 생성 로직이 정상적으로 동작한다.")
    @Transactional
    void createSchedulePost() {
        ScheduleCreationRequest scheduleCreationRequest = createScheduleCreation();
        Long createdScheduleId = scheduleService.saveSchedule(scheduleCreationRequest, testMemberId);

        SchedulePostRequest request = SchedulePostRequest.builder()
                .title("1번 여행!")
                .content("어디로든 갔다옴~")
                .city("서울")
                .scheduleId(createdScheduleId)
                .build();
        Long createdSchedulePostId = schedulePostService.createSchedulePost(testMemberId, request);
        SchedulePost post1 = schedulePostService.findById(createdSchedulePostId);

        assertThat(post1.getMember().getId()).isEqualTo(testMemberId);
        assertThat(memberService.getOne(post1.getMember().getId()).getEmail()).isEqualTo(EMAIL);
        assertThat(memberService.getOne(post1.getMember().getId()).getName()).isEqualTo(NAME);
        assertThat(memberService.getOne(post1.getMember().getId()).getPhoneNumber()).isEqualTo(PHONE);
        assertThat(memberService.getOne(post1.getMember().getId()).getBirth()).isEqualTo(BIRTH);
        assertThat(memberService.getOne(post1.getMember().getId()).getNickname()).isEqualTo(NICKNAME);
        assertThat(memberService.getOne(post1.getMember().getId()).getProfileImage()).isEqualTo(PROFILE_IMAGE);
        assertThat(post1.getTitle()).isEqualTo("1번 여행!");
        assertThat(post1.getContent()).isEqualTo("어디로든 갔다옴~");
        assertThat(post1.getCity()).isEqualTo(City.SEOUL);
        assertThat(post1.getSchedule().getId()).isEqualTo(createdScheduleId);
    }

    @Test
    @DisplayName("여행 공유 게시글을 상세 조회 할 수 있다.")
    @Transactional
    void getSchedulePostDetail() {
        Long createdSchedulePostId = createSchedulePost1();
        SchedulePost post = schedulePostService.findById(createdSchedulePostId);
        long initialViews = post.getViews();

        SchedulePostDetailResponse schedulePostDetail = schedulePostService.getSchedulePostDetail(createdSchedulePostId);

        assertThat(schedulePostDetail.getTitle()).isEqualTo("1번 여행!");
        assertThat(schedulePostDetail.getContent()).isEqualTo("Apple Inc. is an American multinational technology company that specializes in consumer electronics, computer software and online services. Apple is the largest information technology company by revenue (totaling $274.5 billion in 2020) and, since January 2021, the world's most valuable company. As of 2021, Apple is the fourth-largest PC vendor by unit sales[9] and fourth-largest smartphone manufacturer.[10][11] It is one of the Big Five American information technology companies, alongside Amazon, Google (Alphabet), Facebook (Meta), and Microsoft.[12][13][14]\n" +
                "\n" +
                "Apple was founded in 1976 by Steve Jobs, Steve Wozniak and Ronald Wayne to develop and sell Wozniak's Apple I personal computer. It was incorporated by Jobs and Wozniak as Apple Computer, Inc. in 1977, and sales of its computers, among them the Apple II, grew quickly. It went public in 1980, to instant financial success. Over the next few years, Apple shipped new computers featuring innovative graphical user interfaces, such as the original Macintosh, announced in a critically acclaimed advertisement, \"1984\", directed by Ridley Scott. The high cost of its products and limited application library caused problems, as did power struggles between executives. In 1985, Wozniak departed Apple amicably,[15] while Jobs resigned to found NeXT, taking some Apple employees with him.[16]\n" +
                "\n" +
                "As the market for personal computers expanded and evolved throughout the 1990s, Apple lost considerable market share to the lower-priced duopoly of Microsoft Windows on Intel PC clones. The board recruited CEO Gil Amelio, who prepared the struggling company for eventual success with extensive reforms, product focus and layoffs in his 500-day tenure. In 1997, Amelio bought NeXT to resolve Apple's unsuccessful operating-system strategy and entice Jobs back to the company; he replaced Amelio. Apple became profitable again through a number of tactics. First, a revitalizing campaign called \"Think different\", and by launching the iMac and iPod. In 2001, it opened a retail chain, the Apple Stores, and has acquired numerous companies to broaden its software portfolio. In 2007, the company launched the iPhone to critical acclaim and financial success. Jobs resigned in 2011 for health reasons, and died two months later. He was succeeded as CEO by Tim Cook.\n" +
                "\n" +
                "The company receives significant criticism regarding the labor practices of its contractors, its environmental practices, and its business ethics, including anti-competitive behavior and materials sourcing. In August 2018, Apple became the first publicly traded U.S. company to be valued at over $1 trillion,[17][18] and, two years later, the first valued at over $2 trillion.[19][20] The company enjoys a high level of brand loyalty, and is ranked as the world's most valuable brand; as of January 2021, there are 1.65 billion Apple products in active use.[21]");
        assertThat(schedulePostDetail.getCity()).isEqualTo(City.SEOUL);
        assertThat(schedulePostDetail.getCreatedAt()).isEqualTo(post.getCreatedDate());
        assertThat(schedulePostDetail.getViews()).isEqualTo(post.getViews());
        assertThat(initialViews + 1).isEqualTo(post.getViews());
        assertThat(schedulePostDetail.getLiked()).isEqualTo(post.getLiked());
        assertThat(schedulePostDetail.getStartDate()).isEqualTo(post.getSchedule().getStartDate());
        assertThat(schedulePostDetail.getEndDate()).isEqualTo(post.getSchedule().getEndDate());
        assertThat(schedulePostDetail.getGender()).isEqualTo(post.getMember().getGender());
        assertThat(schedulePostDetail.getNickname()).isEqualTo(post.getMember().getNickname());
        assertThat(schedulePostDetail.getAges()).isEqualTo(Ages.from(post.getMember().getBirth()));

        // TODO: 2021.12.10 Teru - equals method를 오버라이드 하지 않고, 그냥 DTO의 toString() 을 통해서 String 비교로 검증하도록 수정.
        Assertions.assertArrayEquals(
                schedulePostDetail.getDailyScheduleSpots().toArray(),
                post.getSchedule().getDailyScheduleSpots().stream()
                        .map(DailyScheduleSpotResponse::from)
                        .toArray()
                );
    }

    @Test
    @DisplayName("생성된 공유 게시글을 삭제할 수 있다")
    @Transactional
    void deleteSchedulePost() {
        Long createdSchedulePostId = createSchedulePost1();
        SchedulePost post = schedulePostService.findById(createdSchedulePostId);

        // 게시글 생성 확인
        assertThat(schedulePostService.findById(createdSchedulePostId).getId()).isEqualTo(createdScheduleId);

        // 게시글 삭제하기
        schedulePostService.deleteSchedulePost(testMemberId, createdScheduleId);

        // 게시글이 삭제되었는지 검증하기
        Assertions.assertThrows(RuntimeException.class,
                () -> schedulePostService.findById(createdSchedulePostId)
        );
    }

    @Test
    @DisplayName("작성한 공유 게시글을 수정할 수 있다")
    @Transactional
    void modifySchedulePost() {
        Long createdSchedulePostId = createSchedulePost1();
        SchedulePost post = schedulePostService.findById(createdSchedulePostId);

        SchedulePostRequest modifyRequest = SchedulePostRequest.builder()
                .title("가자 우주로!")
                .content("삼성 그룹(三星-, The Samsung Group, 약칭: 삼성, Samsung)은 대한민국에 본사를 둔 다국적 기업집단이다.\n" +
                        "\n" +
                        "처음에는 이병철 창업주가 삼성물산이라는 이름으로 자본금 3만 원(현재가치 3억 원)에 지금의 삼성 그룹을 창립하였다.\n" +
                        "\n" +
                        "\"삼성 그룹\"이라는 상호의 회사는 실제로 존재하는 것이 아니며, 삼성전자를 중심으로, 삼성물산, 삼성생명 등 다수의 자회사를 두고 있다. 공정거래위원회는 삼성전자와 그 계열사를 1987년 대규모 기업집단으로 지정하였다.\n" +
                        "\n" +
                        "1938년에 이병철이 대구에서 삼성상회를 창립하였고 그 탓인지 삼성 라이온즈가 대구에서 창단될 당시 대구에 거점을 둔 삼성 그룹 계열사가 제일모직 밖에 없었던 것 때문에 김재하 전 대구 FC 단장 등[1] 임원들이 제일모직에서 삼성 라이온즈로 대거 차출됐으며 대구를 연고로 한 제일모직 축구단 선수 중 한 명이었던 김호는 뒷날 수원 삼성 블루윙즈 초대감독을 역임했는데 이 팀은 대구가 한때 연고지 물망에[2] 올랐었고 제일모직 부지 안에 잔디축구장이 조성된 데다[3] 대구시민운동장이 바로 옆에 붙어있었던 탓인지 제일모직 축구단은 제일모직 잔디축구장과 대구시민운동장을 모두 이용했다. 이후 \"삼성\"이라는 상호 아래 여러 계열사를 설립하면서 그 규모를 키웠으며, 1950년대 후반, 인수합병의 대표주자로 나서면서 오늘날 재계 서열 1위의 거대 기업집단으로 성장하였다.\n" +
                        "\n" +
                        "또한 삼성 그룹은 2013년 380조원 규모의 매출을 올렸다. 한국은행에 따르면 같은 해 대한민국의 명목 국내총생산(GDP)은 1428조 원이다.[4] 해외 매출 비중이 훨씬 큰 삼성의 매출액은 GDP와 직접 비교하기 어렵지만, 그럼에도 삼성의 매출액이 대한민국 GDP의 26.6%나 차지한다는 점은 시사하는 바가 크다.[4] 삼성의 수출은 2013년 1572억 달러로 한국 전체 수출액 6171억 달러의 25%에 해당한다.[4]\n" +
                        "\n" +
                        "삼성 그룹은 브랜드 파이낸스에서 선정하는 글로벌 브랜드가치순위 500대 기업에서 2018년 기준 4위에 올랐다. 브랜드 파이낸스는 매년 세계 기업의 브랜드가치를 평가하여 보고서를 작성, 브랜드가치 500대기업을 발표하고있는데, 브랜드 파이낸스는 2018년 삼성의 브랜드가치가 92289백만달러(약 104조원)의 가치를 지녔다고 평가했다.")
                .city("부산")
                .scheduleId(createdScheduleId)
                .build();
        schedulePostService.modifySchedulePost(testMemberId, modifyRequest);

        // assert
        SchedulePost modifiedPost = schedulePostService.findById(createdSchedulePostId);
        assertThat(modifiedPost.getTitle()).isEqualTo("가자 우주로!");
        assertThat(modifiedPost.getContent()).isEqualTo("삼성 그룹(三星-, The Samsung Group, 약칭: 삼성, Samsung)은 대한민국에 본사를 둔 다국적 기업집단이다.\n" +
                "\n" +
                "처음에는 이병철 창업주가 삼성물산이라는 이름으로 자본금 3만 원(현재가치 3억 원)에 지금의 삼성 그룹을 창립하였다.\n" +
                "\n" +
                "\"삼성 그룹\"이라는 상호의 회사는 실제로 존재하는 것이 아니며, 삼성전자를 중심으로, 삼성물산, 삼성생명 등 다수의 자회사를 두고 있다. 공정거래위원회는 삼성전자와 그 계열사를 1987년 대규모 기업집단으로 지정하였다.\n" +
                "\n" +
                "1938년에 이병철이 대구에서 삼성상회를 창립하였고 그 탓인지 삼성 라이온즈가 대구에서 창단될 당시 대구에 거점을 둔 삼성 그룹 계열사가 제일모직 밖에 없었던 것 때문에 김재하 전 대구 FC 단장 등[1] 임원들이 제일모직에서 삼성 라이온즈로 대거 차출됐으며 대구를 연고로 한 제일모직 축구단 선수 중 한 명이었던 김호는 뒷날 수원 삼성 블루윙즈 초대감독을 역임했는데 이 팀은 대구가 한때 연고지 물망에[2] 올랐었고 제일모직 부지 안에 잔디축구장이 조성된 데다[3] 대구시민운동장이 바로 옆에 붙어있었던 탓인지 제일모직 축구단은 제일모직 잔디축구장과 대구시민운동장을 모두 이용했다. 이후 \"삼성\"이라는 상호 아래 여러 계열사를 설립하면서 그 규모를 키웠으며, 1950년대 후반, 인수합병의 대표주자로 나서면서 오늘날 재계 서열 1위의 거대 기업집단으로 성장하였다.\n" +
                "\n" +
                "또한 삼성 그룹은 2013년 380조원 규모의 매출을 올렸다. 한국은행에 따르면 같은 해 대한민국의 명목 국내총생산(GDP)은 1428조 원이다.[4] 해외 매출 비중이 훨씬 큰 삼성의 매출액은 GDP와 직접 비교하기 어렵지만, 그럼에도 삼성의 매출액이 대한민국 GDP의 26.6%나 차지한다는 점은 시사하는 바가 크다.[4] 삼성의 수출은 2013년 1572억 달러로 한국 전체 수출액 6171억 달러의 25%에 해당한다.[4]\n" +
                "\n" +
                "삼성 그룹은 브랜드 파이낸스에서 선정하는 글로벌 브랜드가치순위 500대 기업에서 2018년 기준 4위에 올랐다. 브랜드 파이낸스는 매년 세계 기업의 브랜드가치를 평가하여 보고서를 작성, 브랜드가치 500대기업을 발표하고있는데, 브랜드 파이낸스는 2018년 삼성의 브랜드가치가 92289백만달러(약 104조원)의 가치를 지녔다고 평가했다.");
        assertThat(modifiedPost.getCity()).isEqualTo(City.BUSAN);
    }

    @Test
    @DisplayName("작성된 공유 게시글에 좋아요 토글을 할 수 있다")
    @Transactional
    void toggleSchedulePostLiked() {
        Long createdSchedulePostId = createSchedulePost1();
        SchedulePost post = schedulePostService.findById(createdSchedulePostId);
        // 좋아요 누르기 전 좋아요 수
        long beforeLiked = post.getLiked();

        // 좋아요 누르기
        SchedulePostLikeRequest doSchedulePostLike = new SchedulePostLikeRequest(createdSchedulePostId, true);
        Long afterLiked = schedulePostService.toggleSchedulePostLiked(testMemberId, doSchedulePostLike);
        // 좋아요 취소
        SchedulePostLikeRequest doSchedulePostLikeAgain = new SchedulePostLikeRequest(createdSchedulePostId, false);
        Long afterLikedAgain = schedulePostService.toggleSchedulePostLiked(testMemberId, doSchedulePostLikeAgain);

        // 좋아요 누른 후 좋아요 수
        assertThat(beforeLiked + 1).isEqualTo(afterLiked);
        // 좋아요 취소된 후 좋아요 수
        assertThat(beforeLiked).isEqualTo(afterLikedAgain);
    }

    @Test
    @DisplayName("좋아요를 누른 게시글만 모아서 볼 수 있다")
    @Transactional
    void getLikedSchedulePostsOnly() {
        Long createdSchedulePostId1 = createSchedulePost1();
        Long createdSchedulePostId2 = createSchedulePost2();
        // 좋아요 한 게시글 없음
        List<SchedulePostResponse> emptySchedulePostList = schedulePostService.getLikedSchedulePosts(testMemberId);
        // 1번 여행 좋아요!
        SchedulePostLikeRequest doSchedulePostLike1 = new SchedulePostLikeRequest(createdSchedulePostId1, true);
        schedulePostService.toggleSchedulePostLiked(testMemberId, doSchedulePostLike1);
        List<SchedulePostResponse> schedulePostListAfterLikeTrip1 = schedulePostService.getLikedSchedulePosts(testMemberId);
        // 2번 여행도 좋아요!
        SchedulePostLikeRequest doSchedulePostLike2 = new SchedulePostLikeRequest(createdSchedulePostId2, true);
        schedulePostService.toggleSchedulePostLiked(testMemberId, doSchedulePostLike2);
        List<SchedulePostResponse> schedulePostListAfterLikeTrip2 = schedulePostService.getLikedSchedulePosts(testMemberId);

        // then
        assertThat(emptySchedulePostList).isEmpty();
        assertThat(schedulePostListAfterLikeTrip1.size()).isEqualTo(1);
        List<SchedulePost> post1 = new ArrayList<>();
        post1.add(schedulePostService.findById(createdSchedulePostId1));
        SchedulePostResponse response1 = convertToSchedulePostResponseList(post1).get(0);

        assertThat(schedulePostListAfterLikeTrip1.get(0).getProfileImageUrl()).isEqualTo(response1.getProfileImageUrl());
        assertThat(schedulePostListAfterLikeTrip1.get(0).getNickname()).isEqualTo(response1.getNickname());
        assertThat(schedulePostListAfterLikeTrip1.get(0).getTitle()).isEqualTo(response1.getTitle());
        assertThat(schedulePostListAfterLikeTrip1.get(0).getAges()).isEqualTo(response1.getAges());
        assertThat(schedulePostListAfterLikeTrip1.get(0).getGenderType()).isEqualTo(response1.getGenderType());
        assertThat(schedulePostListAfterLikeTrip1.get(0).getCity()).isEqualTo(response1.getCity());
        assertThat(schedulePostListAfterLikeTrip1.get(0).getThemes()).isEqualTo(response1.getThemes());
        assertThat(schedulePostListAfterLikeTrip1.get(0).getStartDate()).isEqualTo(response1.getStartDate());
        assertThat(schedulePostListAfterLikeTrip1.get(0).getEndDate()).isEqualTo(response1.getEndDate());

        assertThat(schedulePostListAfterLikeTrip2.size()).isEqualTo(2);
    }
}