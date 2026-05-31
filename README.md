# parking-now

목적지 주변 주차장 검색 및 혼잡도 예측, 대체 주차장 추천 서비스 백엔드

## 기술 스택

- Java 17 · Spring Boot 3.5 · Spring Data JPA
- PostgreSQL (AWS RDS) · Gradle

## 실행 방법

`src/main/resources/application-local.yaml` 생성 후 DB 접속 정보 입력

```yaml
spring:
  datasource:
    url: jdbc:postgresql://{DB_HOST}:{DB_PORT}/{DB_NAME}
    username: {DB_USERNAME}
    password: {DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
```

```bash
./gradlew bootRun
```

Swagger: `http://localhost:8080/swagger-ui/index.html`

## 환경변수 (운영 환경)

| 변수명 | 설명 |
|---|---|
| DB_URL | DB 접속 URL |
| DB_USERNAME | DB 사용자명 |
| DB_PASSWORD | DB 비밀번호 |
| PREDICT_SERVICE_URL | AI 예측 서버 URL |

## API

| Method | URL | 설명 |
|---|---|---|
| GET | /api/v1/parking/search | 주차장 검색 + 혼잡도 예측 (arrivalTime 있을 때) |
| GET | /api/v1/parking/{pkltCd} | 주차장 상세 조회 |
| GET | /api/v1/recommend/alternatives | 대체 주차장 추천 |
