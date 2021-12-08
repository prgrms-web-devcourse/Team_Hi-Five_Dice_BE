package com.cocodan.triplan.schedule.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VotingContent {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "count")
    private int count;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voting_id", referencedColumnName = "id")
    private Voting voting;

    @OneToMany(mappedBy = "votingContent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VotingMember> votingMembers = new ArrayList<>();

    @Builder
    public VotingContent(String content, Voting voting) {
        this.content = content;
        this.voting = voting;
        this.voting.getVotingContents().add(this);
    }

    public void vote(Long memberId) {
        for (VotingMember votingMember : votingMembers) {
            if (votingMember.getMemberId().equals(memberId)) {
                return ;
            }
        }

        votingMembers.add(createVotingMember(memberId));
        count++;
    }

    private VotingMember createVotingMember(Long memberId) {
        return VotingMember.builder()
                .votingContent(this)
                .memberId(memberId)
                .build();
    }

    public void cancel(Long memberId) {
        boolean hasVoted = votingMembers.removeIf(votingMember -> votingMember.getMemberId().equals(memberId));

        if (hasVoted) {
            count--;
        }
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public int getCount() {
        return count;
    }

    public List<VotingMember> getVotingMembers() {
        return votingMembers;
    }
}
