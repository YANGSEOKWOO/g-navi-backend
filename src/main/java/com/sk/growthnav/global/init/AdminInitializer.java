package com.sk.growthnav.global.init;

import com.sk.growthnav.api.member.entity.Member;
import com.sk.growthnav.api.member.entity.MemberRole;
import com.sk.growthnav.api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer {

    private final MemberRepository memberRepository;

    @Value("${admin.default.email}")
    private String adminEmail;

    @Value("${admin.default.password}")
    private String adminPassword;

    @Value("${admin.default.name}")
    private String adminName;

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    @Transactional
    public void initAdmin() {
        // 이미 Admin이 있는지 확인
        boolean hasAdmin = memberRepository.existsByRole(MemberRole.ADMIN);

        if (!hasAdmin) {
            // 운영 환경에서는 비밀번호가 환경변수로 제공되었는지 확인
            if ("prod".equals(activeProfile) && (adminPassword == null || adminPassword.isEmpty())) {
                log.error("❌ 운영 환경에서는 ADMIN_PASSWORD 환경변수가 필수입니다!");
                throw new IllegalStateException("Admin password is required in production environment");
            }

            Member admin = Member.builder()
                    .name(adminName)
                    .email(adminEmail)
                    .password(adminPassword)  // TODO: BCrypt 암호화
                    .role(MemberRole.ADMIN)
                    .isExpert(true)
                    .build();

            Member savedAdmin = memberRepository.save(admin);

            if ("local".equals(activeProfile)) {
                log.info("🎯 [로컬] 관리자 계정 생성 완료!");
                log.info("📧 이메일: {}", savedAdmin.getEmail());
                log.info("🔑 비밀번호: {}", adminPassword);
                log.info("👤 관리자 ID: {}", savedAdmin.getId());
            } else {
                log.info("🎯 [운영] 관리자 계정 생성 완료!");
                log.info("📧 이메일: {}", savedAdmin.getEmail());
                log.info("👤 관리자 ID: {}", savedAdmin.getId());
                log.info("🔐 비밀번호: [보안상 비표시]");
            }
        } else {
            log.info("✅ 관리자 계정이 이미 존재합니다.");
        }
    }
}