package com.biengual.core.domain.entity.pointhistory;

import com.biengual.core.domain.entity.BaseEntity;
import com.biengual.core.enums.PointReason;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 포인트 사용 내역 및 이유 등을 기록하기 위한 엔티티
 * @author 김영래
 */
@Entity
@Getter
@Table(name = "point_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistoryEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, columnDefinition = "bigint")
    private Long userId;

    @Column(name = "point_change", nullable = false, columnDefinition = "int")
    private int pointChange;

    @Column(name = "point_balance", nullable = false, columnDefinition = "int")
    private int pointBalance;

    @Enumerated(EnumType.STRING)
    private PointReason reason;

    @Column(nullable = false)
    private boolean processed;

    @Builder
    private PointHistoryEntity(
        Long userId, int pointChange, int pointBalance, PointReason reason
    ) {
        this.userId = userId;
        this.pointChange = pointChange;
        this.pointBalance = pointBalance;
        this.reason = reason;
        this.processed = false;
    }

    public static PointHistoryEntity createPointHistory(
        Long userId, int pointChange, Long currentBalance, PointReason reason
    ) {
        return PointHistoryEntity.builder()
            .userId(userId)
            .pointChange(pointChange)
            .pointBalance(currentBalance.intValue())
            .reason(reason)
            .build();
    }

    public void updateProcessed(boolean processed) {
        this.processed = processed;
    }

}