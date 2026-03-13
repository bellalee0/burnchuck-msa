package com.example.burnchuck.domain.handler;

import com.example.burnchuck.common.dto.AuthUser;
import com.example.burnchuck.common.enums.UserRole;
import com.example.burnchuck.common.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class StompHandler implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes == null || !sessionAttributes.containsKey("accessToken")) {
                throw new AccessDeniedException("인증되지 않은 접속입니다.");
            }
            String token = (String) sessionAttributes.get("accessToken");

            Long id = jwtUtil.extractId(token);
            String email = jwtUtil.extractEmail(token);
            String nickname = jwtUtil.extractNickname(token);
            String roleString = jwtUtil.extractRole(token);

            UserRole role = UserRole.valueOf(roleString);

            AuthUser authUser = new AuthUser(id, email, nickname, role);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    authUser,
                    null,
                    authUser.getAuthorities()
            );

            accessor.setUser(authentication);

            log.info("STOMP 연결 성공: User={}, Role={}", nickname, role);
        }

        return message;
    }
}