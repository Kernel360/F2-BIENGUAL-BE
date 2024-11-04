package com.biengual.userapi.point.application;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biengual.core.domain.entity.user.UserEntity;
import com.biengual.core.enums.PointReason;
import com.biengual.userapi.content.domain.ContentCommand;
import com.biengual.userapi.content.domain.ContentReader;
import com.biengual.userapi.point.domain.PointService;
import com.biengual.userapi.point.domain.PointStore;
import com.biengual.userapi.pointhistory.domain.PointHistoryStore;
import com.biengual.userapi.user.domain.UserStore;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {
    private final PointStore pointStore;
    private final PointHistoryStore pointHistoryStore;
    private final UserStore userStore;
    private final ContentReader contentReader;

    @Override
    @Transactional
    public void updatePoint(ContentCommand.GetDetail command, PointReason reason) {
        if (contentReader.checkContentNeedPoint(command)) {
            Long currentPoint = pointStore.updateCurrentPointByPointReason(command.userId(), reason);
            pointHistoryStore.recordPointHistory(command.userId(), reason, currentPoint);
        }
    }

    @Override
    @Transactional
    public void updatePointFirstDailyLogin(UserEntity user) {
        // 로그인 포인트 지급
        if (user.getLastLoginTime().toLocalDate().isBefore(LocalDate.now())) {
            Long loginPoint = pointStore.updateCurrentPointByPointReason(user.getId(), PointReason.FIRST_DAILY_LOG_IN);
            pointHistoryStore.recordPointHistory(user.getId(), PointReason.FIRST_DAILY_LOG_IN, loginPoint);
        }
        // 로그인 시간 업데이트
        userStore.updateLastLoginTime(user.getId());
    }
}
