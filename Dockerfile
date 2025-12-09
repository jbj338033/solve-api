# Build stage
FROM gradle:8.14-jdk21 AS builder
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src
RUN gradle bootJar --no-daemon

# Runtime stage with Isolate
FROM ubuntu:24.04

# Install dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-21-jre-headless \
    curl \
    gnupg \
    unzip \
    # Compilers and interpreters
    gcc \
    g++ \
    python3 \
    nodejs \
    default-jdk \
    golang-go \
    rustc \
    # Isolate dependencies
    libcap-dev \
    libsystemd-dev \
    pkg-config \
    make \
    git \
    && rm -rf /var/lib/apt/lists/*

# Install Kotlin
RUN curl -L https://github.com/JetBrains/kotlin/releases/download/v2.0.0/kotlin-compiler-2.0.0.zip -o /tmp/kotlin.zip \
    && unzip /tmp/kotlin.zip -d /opt \
    && rm /tmp/kotlin.zip \
    && ln -s /opt/kotlinc/bin/kotlinc /usr/bin/kotlinc

# Build and install Isolate
RUN git clone https://github.com/ioi/isolate.git /tmp/isolate \
    && cd /tmp/isolate \
    && make isolate \
    && make install \
    && rm -rf /tmp/isolate

# Configure Isolate to use /sys/fs/cgroup
RUN echo 'cg_root = /sys/fs/cgroup' >> /usr/local/etc/isolate

# Create isolate directories
RUN mkdir -p /var/local/lib/isolate

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
