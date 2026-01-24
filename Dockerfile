# 1단계: 빌드 환경 (Gradle 이미지를 사용)
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY . .
# 테스트는 건너뛰고 빌드만 수행 (배포 속도 향상)
RUN ./gradlew clean build -x test --no-daemon

# 2단계: 실행 환경 (Eclipse Temurin 이미지를 사용 - 매우 안정적)
# JDK 17을 사용하는 가벼운 JRE(Java Runtime Environment) 이미지입니다.
# 'focal'은 Ubuntu 20.04 LTS 기반임을 의미하며, 안정적입니다.
FROM eclipse-temurin:17-jre-focal
WORKDIR /app

# 빌드 단계에서 만들어진 JAR 파일을 복사해옴
# 주의: build/libs/*.jar 경로가 맞는지 확인 필요 (일반적으로 맞음)
COPY --from=builder /app/build/libs/*.jar app.jar

# 실행 포트 노출 (Render는 이 포트를 감지함)
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
