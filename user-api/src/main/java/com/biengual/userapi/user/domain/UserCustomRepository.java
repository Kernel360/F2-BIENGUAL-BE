package com.biengual.userapi.user.domain;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User 비지니스 로직에 사용하는 QueryDsl Repository
 *
 * @author 문찬욱
 */
@Repository
@RequiredArgsConstructor
public class UserCustomRepository {
    private final JPAQueryFactory queryFactory;

    // 서비스에 사용하는 user 정보를 조회하기 위한 쿼리
    public Optional<UserInfo.MyInfoExceptMyCategories> findMyInfoExceptMyCategories(Long userId) {
        QUserEntity userEntity = QUserEntity.userEntity;

        UserInfo.MyInfoExceptMyCategories myInfoExceptMyCategories = queryFactory.select(
                Projections.constructor(
                    UserInfo.MyInfoExceptMyCategories.class,
                    userEntity.username,
                    userEntity.nickname,
                    userEntity.email,
                    userEntity.phoneNumber,
                    userEntity.birth,
                    userEntity.gender
                )
            )
            .from(userEntity)
            .where(userEntity.id.eq(userId))
            .fetchOne();

        return Optional.ofNullable(myInfoExceptMyCategories);
    }

    // user의 생성 날짜 및 수정 날짜를 조회하는 쿼리
    public Optional<UserInfo.MySignUpTime> findMySignUpTime(Long userId) {
        QUserEntity userEntity = QUserEntity.userEntity;

        UserInfo.MySignUpTime mySignUpTime = queryFactory.select(
                Projections.constructor(
                    UserInfo.MySignUpTime.class,
                    userEntity.createdAt,
                    userEntity.updatedAt
                )
            )
            .from(userEntity)
            .where(userEntity.id.eq(userId))
            .fetchOne();

        return Optional.ofNullable(mySignUpTime);
    }
}