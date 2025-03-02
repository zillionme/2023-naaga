package com.now.naaga.player.domain;

import com.now.naaga.common.domain.BaseEntity;
import com.now.naaga.member.domain.Member;
import com.now.naaga.score.domain.Score;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.Objects;

import static java.lang.Boolean.FALSE;

@SQLDelete(sql = "UPDATE player SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
@Entity
public class Player extends BaseEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(nullable = false)
    private String nickname;

    @AttributeOverride(name = "value", column = @Column(name = "total_score"))
    @Embedded
    private Score totalScore;

    @JoinColumn(name = "member_id")
    @OneToOne
    private Member member;

    private boolean deleted = FALSE;

    protected Player() {
    }

    public Player(final String nickname,
                  final Score totalScore,
                  final Member member) {
        this(null, nickname, totalScore, member, FALSE);
    }

    public Player(final Long id, final String nickname, final Score totalScore, final Member member, final boolean deleted) {
        this.id = id;
        this.nickname = nickname;
        this.totalScore = totalScore;
        this.member = member;
        this.deleted = deleted;
    }

    public void addScore(Score score) {
        this.totalScore = this.totalScore.plus(score);
    }

    public Long getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public Score getTotalScore() {
        return totalScore;
    }

    public Member getMember() {
        return member;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Player player = (Player) o;
        return Objects.equals(id, player.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", nickname='" + nickname + '\'' +
                ", totalScore=" + totalScore +
                ", memberId=" + member.getId() +
                '}';
    }
}
