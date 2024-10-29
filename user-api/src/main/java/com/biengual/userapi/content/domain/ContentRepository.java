package com.biengual.userapi.content.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import com.biengual.userapi.core.domain.entity.content.entity.ContentEntity;

public interface ContentRepository extends JpaRepository<ContentEntity, Long> {
}
