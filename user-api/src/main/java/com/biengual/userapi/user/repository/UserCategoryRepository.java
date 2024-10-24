package com.biengual.userapi.user.repository;

import com.biengual.userapi.user.domain.UserCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * UserCategoryEntity의 Repository 계층의 인터페이스
 *
 * @author 문찬욱
 */
public interface UserCategoryRepository extends JpaRepository<UserCategoryEntity, Long> {
}
