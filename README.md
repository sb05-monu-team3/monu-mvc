# 📰 Monew 프로젝트
> MongoDB 및 PostgreSQL 백업/복구 기반 뉴스 통합 플랫폼

- **프로젝트 기간:** 2025.10.17 ~ 2025.11.10  
- **API 명세서:** [Swagger UI ↗](http://sprint-project-1196140422.ap-northeast-2.elb.amazonaws.com/sb/monew/api/swagger-ui/index.html)  
- **협업 문서:** [Notion ↗](https://polydactyl-pufferfish-876.notion.site/MoNew-28e08cfefb45803ebd28ffcd05a97b2e?source=copy_link)
- **이슈 관리:** [GitHub Issue Tracker ↗](*#)
- **커뮤니케이션:** Discord  

---

## 📖 프로젝트 소개
Monew는 여러 뉴스 API를 통합하여 사용자 맞춤 뉴스를 제공하고, 사용자 활동 내역 및 의견을 기록/관리할 수 있는 플랫폼입니다.  
PostgreSQL과 MongoDB 기반으로 데이터를 안전하게 저장하고, Spring Batch로 뉴스 백업/수집을 자동화합니다.  
운영 및 모니터링은 Spring Actuator와 Prometheus를 활용하며, 대용량 데이터 처리와 안정성을 고려한 설계가 적용됩니다.  

---

## 👩🏻‍💻 팀원 구성

| 이름 | 역할 | GitHub |
|------|------|--------|
| 정기주 | 팀장 / 백엔드 개발 | [GitHub](https://github.com/jeonggiju) |
| 김용희 | 백엔드 개발 | [GitHub](https://github.com/backKim1024) |
| 민재영 | 백엔드 개발 | [GitHub](https://github.com/jymin0) |
| 박지석 | 백엔드 개발 | [GitHub](https://github.com/commicat2) |
| 이성훈 | 백엔드 개발 | [GitHub](https://github.com/polodumbo) |
| 주세훈 | 백엔드 개발 | [GitHub](https://github.com/Jusehun) |

---

## 🧩 기술 스택

### ⚙️ Backend
- Spring Boot
- MapStruct
- JPA

### 🗄 Database
- PostgreSQL
- MongoDB

### 🚀 Batch / Monitoring
- Spring Batch
- Spring Actuator

### 🤝 협업 Tool
- Git / GitHub
- Notion
- Discord

---

## 🗂️ Repository 구성

| Repo | 설명 |
|------|------|
| monew-mvc | 메인 API 서버 (Spring Boot MVC) |
| monew-batch | 뉴스 수집 및 백업 Batch 서비스 |
| monew-actuator | 모니터링 및 상태 관리 서비스 |

---

## 🚀 브랜치 전략
- `main`: 배포용  
- `develop`: 통합 개발 (추후)  
- `feature/*`: 기능 단위 개발  
  - 예시: `feature/news-api`, `feature/user-preference`, `feature/batch-backup`, `feature/actuator-monitor`  

---

## 📄 커밋 컨벤션
- feat: 새로운 기능 추가  
- fix: 버그 수정  
- refactor: 코드 리팩토링  
- test: 테스트 코드 추가  
- docs: 문서 변경  
- chore: 빌드, 설정 관련 작업  

---

## 📂 프로젝트 구조 (예시)

monew-mvc/  
├─ src/  
│   ├─ main/  
│   │   ├─ java/  
│   │   │   └─ com/monu/  
│   │   │       ├─ config/  
│   │   │       ├─ controller/  
│   │   │       ├─ dto/  
│   │   │       ├─ entity/  
│   │   │       ├─ repository/  
│   │   │       ├─ service/  
│   │   │       └─ util/  
│   │   └─ resources/  
│   │       └─ application.yml  
└─ test/  

---

## 📎 팀원별 구현 예정 기능

| 팀원 | 기능 |
|------|------|
| 정기주 | 기능 a 구현 |
| 김용희 | 기능 b 구현 |
| 민재영 | 기능 c 구현 |
| 박지석 | 기능 d 구현 |
| 이성훈 | 기능 e 구현 |
| 주세훈 | 기능 f 구현 |


