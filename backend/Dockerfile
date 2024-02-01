FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} app.jar

# .env 파일을 복사하여 컨테이너 안에 위치시킵니다.
COPY .env /app/.env

# 8081 포트를 열어줍니다.
EXPOSE 8081

ENTRYPOINT ["java","-jar","/app.jar"]