package com.biengual.userapi.user.domain;

import com.biengual.userapi.category.domain.entity.CategoryEntity;
import com.biengual.userapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCategoryEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, columnDefinition = "bigint")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private CategoryEntity category;

    @Builder
    public UserCategoryEntity(Long userId, CategoryEntity category) {
        this.userId = userId;
        this.category = category;
    }
}
