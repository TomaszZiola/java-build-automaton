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
ENV JAVA_OPTS=""

USER root
RUN apt-get update \
 && apt-get install -y \
    ca-certificates \
    curl \
    git \
 && rm -rf /var/lib/apt/lists/* \
 && mkdir -p /opt/jdks

# JDK 21
RUN curl -fsSL https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.4+7/OpenJDK21U-jdk_aarch64_linux_hotspot_21.0.4_7.tar.gz \
    | tar -xz -C /opt/jdks \
 && mv /opt/jdks/jdk-21.0.4+7 /opt/jdks/jdk-21

# JDK 17
RUN curl -fsSL https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.12%2B7/OpenJDK17U-jdk_aarch64_linux_hotspot_17.0.12_7.tar.gz \
    | tar -xz -C /opt/jdks \
 && mv /opt/jdks/jdk-17.0.12+7 /opt/jdks/jdk-17

RUN useradd -u 10001 -m appuser \
 && mkdir -p /workspaces \
 && chown -R 10001:10001 /workspaces

USER appuser

COPY --from=builder /workspace/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
