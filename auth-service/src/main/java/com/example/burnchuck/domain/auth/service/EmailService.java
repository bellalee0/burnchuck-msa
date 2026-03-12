package com.example.burnchuck.domain.auth.service;

import com.example.burnchuck.common.enums.ErrorCode;
import com.example.burnchuck.common.exception.CustomException;
import com.example.burnchuck.domain.auth.dto.request.EmailConfirmRequest;
import com.example.burnchuck.domain.auth.dto.request.EmailRequest;
import com.example.burnchuck.domain.auth.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.springframework.util.ObjectUtils;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;

    /**
     * 이메일 인증 번호 발송
     */
    public boolean sendVerificationEmail(EmailRequest request) {

        String email = request.getEmail();

        if (userRepository.existsByEmail(email)) {
            return false;
        }

        try {
            String verificationCode = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
            redisTemplate.opsForValue().set(email, verificationCode, 5, TimeUnit.MINUTES);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("[번쩍] 이메일 인증 번호입니다.");
            helper.setText("인증 번호는 <b>" + verificationCode + "</b> 입니다.<br>5분 이내에 입력해주세요.", true);

            mailSender.send(message);

            return true;

        } catch (MessagingException e) {
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    /**
     * 이메일 인증 번호 확인
     */
    public boolean verifyCode(EmailConfirmRequest request) {

        String email = request.getEmail();
        String code = request.getVerificationCode();

        String savedCode = redisTemplate.opsForValue().get(email);

        if (savedCode == null || !ObjectUtils.nullSafeEquals(savedCode, code)) {
            return false;
        }

        redisTemplate.delete(email);
        return true;
    }
}

