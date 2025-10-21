# 📰 Monu 프로젝트

> MongoDB 및 PostgreSQL 백업/복구 기반 뉴스 통합 플랫폼

## ⚙️ Repository 구성
| Repo | 설명 |
|------|------|
| monu-mvc | 메인 API 서버 (Spring Boot MVC) |
| monu-batch | 뉴스 수집 및 백업 Batch 서비스 |
| monu-actuator | 모니터링 및 상태 관리 서비스 |

## 🧩 기술 스택
- **Backend:** Spring Boot 3.3.x, JPA, QueryDSL, MapStruct
- **Batch:** Spring Batch, Scheduler, AWS S3
- **Monitoring:** Spring Actuator, Prometheus
- **DB:** PostgreSQL, MongoDB
- **Infra:** AWS ECS, S3, GitHub Actions

## 🚀 브랜치 전략
- `main`: 배포용
- `develop`: 통합 개발
- `feature/*`: 기능 단위 개발

## 🧪 테스트
- JUnit5, Mockito, Spring Boot Test

## 📄 커밋 컨벤션
- feat: 새로운 기능 추가
- fix: 버그 수정
- refactor: 코드 리팩토링
- test: 테스트 코드 추가
- docs: 문서 변경
- chore: 빌드, 설정 관련 작업
