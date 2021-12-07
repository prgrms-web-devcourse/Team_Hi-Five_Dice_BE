package com.cocodan.triplan.post.schedule.domain;

import com.cocodan.triplan.common.BaseEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "schedule_post_like")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// TODO: 2021.12.07 Teru - 더 직관적인 네이밍 고민
public class Like extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @ManyToOne
    @JoinColumn(name = "schedule_post_id", referencedColumnName = "id")
    private SchedulePost schedulePost;

    public Like(Long memberId, SchedulePost schedulePost) {
        this.memberId = memberId;
        this.schedulePost = schedulePost;
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public SchedulePost getSchedulePost() {
        return schedulePost;
    }
}
