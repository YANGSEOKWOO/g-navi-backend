# 더 가벼운 베이스 이미지 사용
FROM openjdk:17-jre-slim

# 작업 디렉토리 설정
WORKDIR /app

# JAR 파일 복사
COPY build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080 8081

# JVM 최적화 옵션 추가
ENTRYPOINT ["java", \
    "-Dspring.profiles.active=prod", \
    "-XX:+UseG1GC", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]