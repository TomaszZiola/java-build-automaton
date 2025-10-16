FROM eclipse-temurin:25-jdk AS builder

WORKDIR /workspace

COPY gradlew .
COPY gradle gradle
RUN chmod +x ./gradlew
ENV GRADLE_USER_HOME=/workspace/.gradle

COPY build.gradle.kts .
COPY settings.gradle.kts .

COPY src src

RUN ./gradlew --no-daemon bootJar -x test

FROM eclipse-temurin:25-jdk

WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS=""

# create app user
RUN useradd -u 10001 -m appuser

# install git in runtime image
USER root
RUN apt-get update && apt-get install -y git && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /workspaces && chown -R 10001:10001 /workspaces
USER appuser

COPY --from=builder /workspace/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
