package com.biengual.core.domain.entity.paymenthistory;

import static com.biengual.core.constant.RestrictionConstant.*;

import java.time.LocalDateTime;

import com.biengual.core.domain.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_content_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentContentHistoryEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "bigint")
    private Long userId;

    @Column(nullable = false, columnDefinition = "bigint")
    private Long contentId;

    @Column(nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime expiredAt;

    @Builder
    public PaymentContentHistoryEntity(Long id, Long userId, Long contentId, LocalDateTime expiredAt) {
        this.id = id;
        this.userId = userId;
        this.contentId = contentId;
        this.expiredAt = expiredAt;
    }

    public static PaymentContentHistoryEntity createPaymentHistory(Long userId, Long contentId) {
        return PaymentContentHistoryEntity.builder()
            .userId(userId)
            .contentId(contentId)
            .expiredAt(LocalDateTime.now().plusDays(PERIOD_FOR_POINT_CONTENT_ACCESS))
            .build();
    }
}

