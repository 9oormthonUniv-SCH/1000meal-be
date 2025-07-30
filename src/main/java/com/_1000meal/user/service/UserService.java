package com._1000meal.user.service;


import com._1000meal.user.domain.User;
import com._1000meal.user.dto.UpdateUserRequest;
import com._1000meal.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 사용자 정보 수정
    @Transactional
    public User updateUserInfo(User user, UpdateUserRequest request) {
        user.updateInfo(request.getName(), request.getPhoneNumber());
        return userRepository.save(user); // 저장된 User 반환 (필요시)
    }
}