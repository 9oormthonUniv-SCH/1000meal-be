package com._1000meal.user.oauth;

import com._1000meal.user.domain.User;
import com._1000meal.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        // 기본 서비스로부터 사용자 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // registrationId: 구글인지 카카오인지 구분
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest
                .getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        // 사용자 정보 맵을 OAuthAttributes로 변환
        OAuthAttributes attributes = OAuthAttributes.of(
                registrationId,
                userNameAttributeName,
                oAuth2User.getAttributes()
        );

        // DB에 사용자 저장 or 업데이트
        User user = saveOrUpdate(attributes);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey()
        );
    }

    private User saveOrUpdate(OAuthAttributes attributes) {
        // 이메일이 이미 존재하면 업데이트, 아니면 새로 생성
        return userRepository.findByEmail(attributes.getEmail())
                .map(existingUser -> existingUser) // 필요하면 update 로직도 추가 가능
                .orElseGet(() -> userRepository.save(attributes.toEntity()));
    }
}