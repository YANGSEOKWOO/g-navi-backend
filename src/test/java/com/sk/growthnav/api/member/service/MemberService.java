//package com.sk.growthnav.api.member.service;
//
//import com.sk.growthnav.api.member.dto.MemberInfo;
//import com.sk.growthnav.api.member.entity.Member;
//import com.sk.growthnav.api.member.repository.MemberRepository;
//import com.sk.growthnav.global.apiPayload.code.FailureCode;
//import com.sk.growthnav.global.base.FailureException;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.BDDMockito.*;
//
//@ExtendWith(MockitoExtension.class)  // Mockito 사용
//@DisplayName("MemberService 테스트")
//class MemberServiceTest {
//
//    @Mock
//    private MemberRepository memberRepository;  // 가짜 Repository
//
//    @InjectMocks
//    private MemberService memberService;        // 테스트 대상
//
//    @Test
//    @DisplayName("회원 ID로 회원 정보 조회 성공")
//    void getMemberInfo_Success() {
//        // Given (준비)
//        Long memberId = 1L;
//        Member mockMember = new Member(1L, "김철수", "password123", "kim@example.com");
//
//        // Repository의 동작을 미리 정의 (Mock)
//        given(memberRepository.findById(memberId))
//                .willReturn(Optional.of(mockMember));
//
//        // When (실행)
//        MemberInfo result = memberService.getMemberInfo(memberId);
//
//        // Then (검증)
//        assertThat(result).isNotNull();
//        assertThat(result.getMemberId()).isEqualTo(1L);
//        assertThat(result.getName()).isEqualTo("김철수");
//        assertThat(result.getEmail()).isEqualTo("kim@example.com");
//
//        // Repository가 1번 호출되었는지 확인
//        then(memberRepository).should(times(1)).findById(memberId);
//    }
//
//    @Test
//    @DisplayName("존재하지 않는 회원 ID로 조회 시 예외 발생")
//    void getMemberInfo_MemberNotFound() {
//        // Given
//        Long nonExistentId = 999L;
//        given(memberRepository.findById(nonExistentId))
//                .willReturn(Optional.empty());  // 빈 Optional 반환
//
//        // When & Then
//        assertThatThrownBy(() -> memberService.getMemberInfo(nonExistentId))
//                .isInstanceOf(FailureException.class)
//                .hasFieldOrPropertyWithValue("baseErrorCode", FailureCode.MEMBER_NOT_FOUND);
//
//        then(memberRepository).should(times(1)).findById(nonExistentId);
//    }
//
//    @Test
//    @DisplayName("이메일 존재 확인 - 존재하는 경우")
//    void isEmailExists_True() {
//        // Given
//        String email = "existing@example.com";
//        given(memberRepository.existsByEmail(email))
//                .willReturn(true);
//
//        // When
//        boolean result = memberService.isEmailExists(email);
//
//        // Then
//        assertThat(result).isTrue();
//        then(memberRepository).should(times(1)).existsByEmail(email);
//    }
//
//    @Test
//    @DisplayName("이메일 존재 확인 - 존재하지 않는 경우")
//    void isEmailExists_False() {
//        // Given
//        String email = "nonexistent@example.com";
//        given(memberRepository.existsByEmail(email))
//                .willReturn(false);
//
//        // When
//        boolean result = memberService.isEmailExists(email);
//
//        // Then
//        assertThat(result).isFalse();
//        then(memberRepository).should(times(1)).existsByEmail(email);
//    }
//
//    @Test
//    @DisplayName("회원 생성 성공")
//    void createMember_Success() {
//        // Given
//        String name = "이영희";
//        String email = "lee@example.com";
//        String password = "password123";
//
//        Member savedMember = new Member(2L, name, password, email);
//
//        given(memberRepository.existsByEmail(email))
//                .willReturn(false);  // 이메일 중복 없음
//        given(memberRepository.save(any(Member.class)))
//                .willReturn(savedMember);
//
//        // When
//        Member result = memberService.createMember(name, email, password);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo(2L);
//        assertThat(result.getName()).isEqualTo(name);
//        assertThat(result.getEmail()).isEqualTo(email);
//
//        then(memberRepository).should(times(1)).existsByEmail(email);
//        then(memberRepository).should(times(1)).save(any(Member.class));
//    }
//
//    @Test
//    @DisplayName("중복된 이메일로 회원 생성 시 예외 발생")
//    void createMember_EmailDuplicated() {
//        // Given
//        String name = "박민수";
//        String email = "duplicate@example.com";
//        String password = "password123";
//
//        given(memberRepository.existsByEmail(email))
//                .willReturn(true);  // 이메일 중복 있음
//
//        // When & Then
//        assertThatThrownBy(() -> memberService.createMember(name, email, password))
//                .isInstanceOf(FailureException.class)
//                .hasFieldOrPropertyWithValue("baseErrorCode", FailureCode.MEMBER_EMAIL_DUPLICATED);
//
//        then(memberRepository).should(times(1)).existsByEmail(email);
//        then(memberRepository).should(never()).save(any(Member.class));  // save 호출되지 않음
//    }
//}