# Growth Navigator Backend

SK Growth Navigator 백엔드 서비스 - AI 기반 개발자 성장 상담 플랫폼

## 🎯 프로젝트 개요

Growth Navigator는 개발자들의 커리어 성장을 돕는 AI 기반 상담 플랫폼입니다. 사용자의 프로젝트 경험과 기술 스택을 분석하여 개인화된 성장 조언을 제공합니다.

### 주요 기능

- **AI 기반 커리어 상담**: 사용자의 배경을 분석한 개인화된 조언 제공
- **프로젝트 포트폴리오 관리**: 프로젝트와 기술 스택 통합 관리
- **전문가 뉴스 큐레이션**: 전문가가 선별한 기술 뉴스 제공
- **관리자 대시보드**: 사용자 활동 분석 및 워드클라우드 생성
- **역할 기반 권한 관리**: 일반 사용자, 전문가, 관리자 구분

## 🏗️ 아키텍처

### 기술 스택

- **Framework**: Spring Boot 3.x
- **Language**: Java 17
- **Database**: 
  - PostgreSQL (사용자, 프로젝트 데이터)
  - MongoDB (대화 내역)
- **AI Integration**: FastAPI 연동
- **Deployment**: Kubernetes + Docker
- **Storage**: EFS (파일 저장)

  ### 시스템 구조
  #### 시스템 아키텍처

    <img width="736" height="582" alt="image" src="https://github.com/user-attachments/assets/8caf60f9-e19f-4a4d-88c0-53236478e890" />


  #### 인프라 아키텍처
  <img width="2088" height="1728" alt="image" src="https://github.com/user-attachments/assets/deb448a7-5ef9-48bf-a61c-8419d6d770a4" />


## 📝 API 문서

### 주요 엔드포인트

| 기능 | Method | URL | 설명 |
|------|--------|-----|------|
| 회원가입 | POST | `/api/auth/signup` | 새 사용자 등록 |
| 로그인 | POST | `/api/auth/login` | 사용자 인증 |
| 홈 화면 | GET | `/api/auth/{memberId}/home` | 홈 화면 데이터 |
| 대화 시작 | POST | `/api/conversations` | AI 상담 시작 |
| 메시지 전송 | POST | `/api/conversations/{id}/messages` | 메시지 전송 |
| 프로젝트 생성 | POST | `/api/projects` | 프로젝트 등록 |
| 뉴스 목록 | GET | `/api/news` | 승인된 뉴스 조회 |

### 인증 및 권한

- **USER**: 기본 사용자 (상담, 프로젝트 관리)
- **EXPERT**: 전문가 (뉴스 작성 권한 추가)
- **ADMIN**: 관리자 (모든 권한 + 관리 기능)

## 🗂️ 프로젝트 구조

```
src/main/java/com/sk/growthnav/
├── api/                          # API 레이어
│   ├── admin/                    # 관리자 기능
│   ├── conversation/             # 대화 관리
│   ├── external/                 # 외부 API 연동
│   ├── member/                   # 사용자 관리
│   ├── news/                     # 뉴스 관리
│   ├── project/                  # 프로젝트 관리
│   ├── skill/                    # 기술 스택 관리
│   └── wordcloud/                # 워드클라우드 분석
├── global/                       # 공통 기능
│   ├── config/                   # 설정
│   ├── exception/                # 예외 처리
│   └── init/                     # 초기 데이터
└── GrowthNavApplication.java     # 메인 클래스
```

## 🔧 설정

### 환경별 설정

- **local**: 로컬 개발 환경 (H2, 임베디드 MongoDB)
- **prod**: 운영 환경 (PostgreSQL, MongoDB)
- **test**: 테스트 환경 (인메모리 DB)

### 주요 설정 값

```yaml
# FastAPI 연동
fastapi:
  base-url: ${FASTAPI_BASE_URL}
  timeout: 30000

# 관리자 계정
admin:
  default:
    email: ${ADMIN_EMAIL}
    password: ${ADMIN_PASSWORD}

# 파일 저장
app:
  storage:
    pvc:
      path: ${APP_STORAGE_PVC_PATH}
```

## 📊 모니터링

### 헬스체크

- **애플리케이션**: `/actuator/health`
- **데이터베이스**: 자동 연결 확인
- **FastAPI**: `/ai/health`
