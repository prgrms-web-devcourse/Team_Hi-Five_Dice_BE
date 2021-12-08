package com.cocodan.triplan.schedule.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Voting {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", referencedColumnName = "id")
    private Schedule schedule;

    @OneToMany(mappedBy = "voting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VotingContent> votingContents = new ArrayList<>();

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Builder
    public Voting(String title, Schedule schedule, Long memberId) {
        this.schedule = schedule;
        this.title = title;
        this.memberId = memberId;
        this.schedule.getVotingList().add(this);
    }

    public void vote(Long contentId, boolean flag, Long memberId){
        for (VotingContent votingContent : votingContents) {
            if (votingContent.getId().equals(contentId)) {
                voteByFlag(flag, memberId, votingContent);
                break;
            }
        }
    }

    private void voteByFlag(boolean flag, Long memberId, VotingContent votingContent) {
        if (flag) {
            votingContent.vote(memberId);
            return;
        }

        votingContent.cancel(memberId);
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<VotingContent> getVotingContents() {
        return votingContents;
    }
}
