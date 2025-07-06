package com.sk.growthnav.api.external.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * FastAPI 프로젝트 임베딩 API 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectEmbeddingResponse {

    String status;                  // 처리 상태
    String message;                 // 처리 메시지
    String employee_id;             // 직원 ID
    String project_name;            // 프로젝트명
    String document_id;             // 저장된 문서 ID
    Boolean embedding_success;      // 임베딩 성공 여부
    Boolean stored_in_vectordb;     // 벡터DB 저장 성공 여부
    String timestamp;               // 처리 시간


}