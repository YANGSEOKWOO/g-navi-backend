package com.sk.growthnav.api.conversation.repository;

import com.sk.growthnav.api.conversation.document.ConversationDocument;
import com.sk.growthnav.global.document.SenderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest  // MongoDB 테스트용 어노테이션
@ActiveProfiles("test")
@DisplayName("ConversationRepository MongoDB 통합 테스트")
class ConversationRepositoryTest {

    @Autowired
    private ConversationRepository conversationRepository;

    @BeforeEach
    void setUp() {
        // 각 테스트 전에 MongoDB 컬렉션 정리
        conversationRepository.deleteAll();
    }

    @Test
    @DisplayName("대화 저장 및 조회 성공")
    void saveAndFindConversation_Success() {
        // Given
        Long memberId = 1L;
        ConversationDocument conversation = ConversationDocument.builder()
                .memberId(memberId)
                .build();

        // 메시지 추가
        conversation.addMessage(SenderType.USER, "안녕하세요!");
        conversation.addMessage(SenderType.BOT, "안녕하세요! 무엇을 도와드릴까요?");

        // When
        ConversationDocument savedConversation = conversationRepository.save(conversation);

        // Then
        assertThat(savedConversation).isNotNull();
        assertThat(savedConversation.getId()).isNotNull();  // MongoDB가 ID 자동 생성
        assertThat(savedConversation.getMemberId()).isEqualTo(memberId);
        assertThat(savedConversation.getMessages()).hasSize(2);
        assertThat(savedConversation.getMessages().get(0).getMessageText()).isEqualTo("안녕하세요!");
        assertThat(savedConversation.getMessages().get(1).getSenderType()).isEqualTo(SenderType.BOT);
    }

    @Test
    @DisplayName("회원 ID로 대화 목록 조회 성공")
    void findByMemberId_Success() {
        // Given
        Long memberId = 1L;

        // 첫 번째 대화 생성
        ConversationDocument conversation1 = ConversationDocument.builder()
                .memberId(memberId)
                .build();
        conversation1.addMessage(SenderType.USER, "첫 번째 대화입니다");
        conversation1.addMessage(SenderType.BOT, "안녕하세요!");

        // 두 번째 대화 생성
        ConversationDocument conversation2 = ConversationDocument.builder()
                .memberId(memberId)
                .build();
        conversation2.addMessage(SenderType.USER, "두 번째 대화입니다");

        // 다른 회원의 대화 (조회되면 안 됨)
        ConversationDocument otherConversation = ConversationDocument.builder()
                .memberId(999L)
                .build();

        conversationRepository.saveAll(List.of(conversation1, conversation2, otherConversation));

        // When
        List<ConversationDocument> result = conversationRepository.findByMemberId(memberId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(ConversationDocument::getMemberId)
                .containsOnly(memberId);

        // 메시지 내용도 확인
        assertThat(result)
                .extracting(conv -> conv.getMessages().get(0).getMessageText())
                .containsExactlyInAnyOrder("첫 번째 대화입니다", "두 번째 대화입니다");
    }

    @Test
    @DisplayName("회원 ID로 대화 목록 최신순 정렬 조회 성공")
    void findByMemberIdOrderByCreatedAtDesc_Success() throws InterruptedException {
        // Given
        Long memberId = 1L;

        // 첫 번째 대화 (과거)
        ConversationDocument oldConversation = ConversationDocument.builder()
                .memberId(memberId)
                .build();
        oldConversation.addMessage(SenderType.USER, "과거 대화");
        conversationRepository.save(oldConversation);

        // 시간 차이를 위해 잠시 대기
        Thread.sleep(10);

        // 두 번째 대화 (최신)
        ConversationDocument newConversation = ConversationDocument.builder()
                .memberId(memberId)
                .build();
        newConversation.addMessage(SenderType.USER, "최신 대화");
        conversationRepository.save(newConversation);

        // When
        List<ConversationDocument> result = conversationRepository.findByMemberIdOrderByCreatedAtDesc(memberId);

        // Then
        assertThat(result).hasSize(2);

        // 최신 대화가 첫 번째로 와야 함
        assertThat(result.get(0).getMessages().get(0).getMessageText()).isEqualTo("최신 대화");
        assertThat(result.get(1).getMessages().get(0).getMessageText()).isEqualTo("과거 대화");
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 조회 시 빈 리스트 반환")
    void findByMemberId_EmptyResult() {
        // Given
        Long nonExistentMemberId = 999L;

        // When
        List<ConversationDocument> result = conversationRepository.findByMemberId(nonExistentMemberId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("대화 ID로 특정 대화 조회 성공")
    void findById_Success() {
        // Given
        ConversationDocument conversation = ConversationDocument.builder()
                .memberId(1L)
                .build();
        conversation.addMessage(SenderType.USER, "특정 대화 조회 테스트");

        ConversationDocument savedConversation = conversationRepository.save(conversation);
        String conversationId = savedConversation.getId();

        // When
        Optional<ConversationDocument> result = conversationRepository.findById(conversationId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(conversationId);
        assertThat(result.get().getMemberId()).isEqualTo(1L);
        assertThat(result.get().getMessages()).hasSize(1);
        assertThat(result.get().getMessages().get(0).getMessageText()).isEqualTo("특정 대화 조회 테스트");
    }

    @Test
    @DisplayName("메시지 추가 편의 메서드 테스트")
    void addMessage_ConvenienceMethod() {
        // Given
        ConversationDocument conversation = ConversationDocument.builder()
                .memberId(1L)
                .build();

        // When
        conversation.addMessage(SenderType.USER, "첫 번째 메시지");
        conversation.addMessage(SenderType.BOT, "두 번째 메시지");
        conversation.addMessage(SenderType.USER, "세 번째 메시지");

        ConversationDocument savedConversation = conversationRepository.save(conversation);

        // Then
        assertThat(savedConversation.getMessages()).hasSize(3);
        assertThat(savedConversation.getMessageCount()).isEqualTo(3);

        // 최신 메시지 확인
        ConversationDocument.MessageDocument latestMessage = savedConversation.getLatestMessage();
        assertThat(latestMessage).isNotNull();
        assertThat(latestMessage.getMessageText()).isEqualTo("세 번째 메시지");
        assertThat(latestMessage.getSenderType()).isEqualTo(SenderType.USER);
        assertThat(latestMessage.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("대화 업데이트 테스트")
    void updateConversation_Success() {
        // Given
        ConversationDocument conversation = ConversationDocument.builder()
                .memberId(1L)
                .build();
        conversation.addMessage(SenderType.USER, "초기 메시지");

        ConversationDocument savedConversation = conversationRepository.save(conversation);
        String conversationId = savedConversation.getId();

        // When - 메시지 추가
        savedConversation.addMessage(SenderType.BOT, "응답 메시지");
        ConversationDocument updatedConversation = conversationRepository.save(savedConversation);

        // Then
        ConversationDocument foundConversation = conversationRepository.findById(conversationId).orElseThrow();
        assertThat(foundConversation.getMessages()).hasSize(2);
        assertThat(foundConversation.getMessages().get(1).getMessageText()).isEqualTo("응답 메시지");
    }
}