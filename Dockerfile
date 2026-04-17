# Stage 1: Build
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# Gradle 래퍼와 설정 파일들을 먼저 복사 (캐시 활용)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# 권한 부여 (윈도우/맥 환경 차이로 인한 gradlew 실행 오류 방지)
RUN chmod +x ./gradlew

# 의존성 먼저 다운로드
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사 및 빌드
COPY src src
RUN ./gradlew clean bootJar --no-daemon

# Stage 2: Run (jre로 수정, AS builder 제거)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# builder 스테이지에서 생성된 JAR 파일만 복사 (--from=builder로 수정)
COPY --from=builder /app/build/libs/*.jar app.jar

# 컨테이너 실행 시 환경 변수로 설정을 주입받을 수 있도록 구성
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "app.jar"]

EXPOSE 8080