package com.biengual.userapi.user.infrastructure;

import com.biengual.userapi.annotation.DataProvider;
import com.biengual.userapi.message.error.exception.CommonException;
import com.biengual.userapi.user.domain.UserReader;
import com.biengual.userapi.user.domain.entity.UserEntity;
import com.biengual.userapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import static com.biengual.userapi.message.error.code.UserErrorCode.USER_NOT_FOUND;

@DataProvider
@RequiredArgsConstructor
public class UserReaderImpl implements UserReader {
    private final UserRepository userRepository;

    @Override
    public UserEntity findUser(Long userId, String email) {
        return userRepository.findByIdAndEmail(userId, email)
            .orElseThrow(() -> new CommonException(USER_NOT_FOUND));
    }
}
