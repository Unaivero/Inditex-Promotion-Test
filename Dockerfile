# Multi-stage Dockerfile for InditexPromotionsTest
# Stage 1: Build environment
FROM maven:3.9.6-eclipse-temurin-11 AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml first to leverage Docker cache
COPY pom.xml .

# Download dependencies (this will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean compile test-compile -B

# Stage 2: Runtime environment
FROM openjdk:11-jre-slim

# Install system dependencies
RUN apt-get update && apt-get install -y \
    wget \
    curl \
    gnupg \
    unzip \
    xvfb \
    x11vnc \
    fluxbox \
    procps \
    && rm -rf /var/lib/apt/lists/*

# Install Google Chrome
RUN wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | apt-key add - \
    && echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" > /etc/apt/sources.list.d/google-chrome.list \
    && apt-get update \
    && apt-get install -y google-chrome-stable \
    && rm -rf /var/lib/apt/lists/*

# Install Firefox
RUN wget -O firefox.tar.bz2 "https://download.mozilla.org/?product=firefox-latest&os=linux64&lang=en-US" \
    && tar -xjf firefox.tar.bz2 -C /opt/ \
    && ln -s /opt/firefox/firefox /usr/local/bin/firefox \
    && rm firefox.tar.bz2

# Install Microsoft Edge
RUN curl https://packages.microsoft.com/keys/microsoft.asc | gpg --dearmor > microsoft.gpg \
    && install -o root -g root -m 644 microsoft.gpg /etc/apt/trusted.gpg.d/ \
    && sh -c 'echo "deb [arch=amd64,arm64,armhf signed-by=/etc/apt/trusted.gpg.d/microsoft.gpg] https://packages.microsoft.com/repos/edge stable main" > /etc/apt/sources.list.d/microsoft-edge-dev.list' \
    && apt-get update \
    && apt-get install -y microsoft-edge-stable \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r testuser && useradd -r -g testuser -u 1001 testuser

# Set working directory
WORKDIR /app

# Copy Maven from builder stage
COPY --from=builder /root/.m2 /home/testuser/.m2
COPY --from=builder /app/target /app/target
COPY --from=builder /app/pom.xml /app/pom.xml
COPY --from=builder /app/src /app/src

# Copy test configuration files
COPY src/test/resources/testng.xml /app/src/test/resources/testng.xml
COPY src/main/resources/application*.properties /app/src/main/resources/

# Create directories for reports and logs
RUN mkdir -p /app/target/allure-results \
    && mkdir -p /app/logs \
    && mkdir -p /app/screenshots \
    && mkdir -p /app/videos

# Set proper permissions
RUN chown -R testuser:testuser /app \
    && chown -R testuser:testuser /home/testuser

# Install Maven for runtime
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Create script for running tests
RUN echo '#!/bin/bash\n\
set -e\n\
\n\
# Function to start display server\n\
start_display() {\n\
    export DISPLAY=:99\n\
    Xvfb :99 -screen 0 1920x1080x24 -ac +extension GLX +render -noreset &\n\
    export XVFB_PID=$!\n\
    sleep 3\n\
    \n\
    # Start window manager (optional)\n\
    fluxbox -display :99 &\n\
    export FLUXBOX_PID=$!\n\
    \n\
    # Start VNC server for debugging (optional)\n\
    if [ "$VNC_ENABLED" = "true" ]; then\n\
        x11vnc -display :99 -nopw -listen localhost -xkb -forever -shared &\n\
        export VNC_PID=$!\n\
    fi\n\
}\n\
\n\
# Function to stop display server\n\
stop_display() {\n\
    if [ ! -z "$VNC_PID" ]; then kill $VNC_PID 2>/dev/null || true; fi\n\
    if [ ! -z "$FLUXBOX_PID" ]; then kill $FLUXBOX_PID 2>/dev/null || true; fi\n\
    if [ ! -z "$XVFB_PID" ]; then kill $XVFB_PID 2>/dev/null || true; fi\n\
}\n\
\n\
# Trap to ensure cleanup\n\
trap stop_display EXIT\n\
\n\
# Start display server if not headless\n\
if [ "$HEADLESS" != "true" ]; then\n\
    start_display\n\
fi\n\
\n\
# Set environment variables\n\
export TEST_ENVIRONMENT=${TEST_ENVIRONMENT:-dev}\n\
export BROWSER_TYPE=${BROWSER_TYPE:-chrome}\n\
export PARALLEL_THREAD_COUNT=${PARALLEL_THREAD_COUNT:-2}\n\
export GRID_ENABLED=${GRID_ENABLED:-false}\n\
export GRID_URL=${GRID_URL:-}\n\
\n\
# Run tests\n\
echo "Starting test execution..."\n\
echo "Environment: $TEST_ENVIRONMENT"\n\
echo "Browser: $BROWSER_TYPE"\n\
echo "Threads: $PARALLEL_THREAD_COUNT"\n\
echo "Grid Enabled: $GRID_ENABLED"\n\
\n\
# Execute Maven command based on test type\n\
if [ "$TEST_TYPE" = "smoke" ]; then\n\
    mvn test -Dtest.environment=$TEST_ENVIRONMENT -Dbrowser.type=$BROWSER_TYPE -Dparallel.thread.count=$PARALLEL_THREAD_COUNT -Dgroups=smoke\n\
elif [ "$TEST_TYPE" = "regression" ]; then\n\
    mvn test -Dtest.environment=$TEST_ENVIRONMENT -Dbrowser.type=$BROWSER_TYPE -Dparallel.thread.count=$PARALLEL_THREAD_COUNT -Dgroups=regression\n\
elif [ "$TEST_TYPE" = "api" ]; then\n\
    mvn test -Dtest.environment=$TEST_ENVIRONMENT -Dparallel.thread.count=$PARALLEL_THREAD_COUNT -Dgroups=api\n\
elif [ "$TEST_TYPE" = "mobile" ]; then\n\
    mvn test -Dtest.environment=$TEST_ENVIRONMENT -Dbrowser.type=$BROWSER_TYPE -Dparallel.thread.count=$PARALLEL_THREAD_COUNT -Dgroups=mobile,responsive\n\
else\n\
    mvn test -Dtest.environment=$TEST_ENVIRONMENT -Dbrowser.type=$BROWSER_TYPE -Dparallel.thread.count=$PARALLEL_THREAD_COUNT\n\
fi\n\
\n\
# Generate Allure report if requested\n\
if [ "$GENERATE_REPORT" = "true" ]; then\n\
    echo "Generating Allure report..."\n\
    mvn allure:report\n\
fi\n\
\n\
echo "Test execution completed"\n\
' > /app/run-tests.sh

RUN chmod +x /app/run-tests.sh

# Health check script
RUN echo '#!/bin/bash\n\
# Check if Java is available\n\
java -version > /dev/null 2>&1 || exit 1\n\
\n\
# Check if Maven is available\n\
mvn -version > /dev/null 2>&1 || exit 1\n\
\n\
# Check if browsers are available\n\
google-chrome --version > /dev/null 2>&1 || exit 1\n\
firefox --version > /dev/null 2>&1 || exit 1\n\
\n\
# Check if Xvfb is available\n\
which Xvfb > /dev/null 2>&1 || exit 1\n\
\n\
echo "All dependencies are available"\n\
exit 0\n\
' > /app/health-check.sh

RUN chmod +x /app/health-check.sh

# Switch to non-root user
USER testuser

# Set environment variables
ENV JAVA_OPTS="-Xmx2g -Xms1g -Dfile.encoding=UTF-8"
ENV MAVEN_OPTS="-Xmx1g"
ENV TEST_ENVIRONMENT=dev
ENV BROWSER_TYPE=chrome
ENV HEADLESS=true
ENV PARALLEL_THREAD_COUNT=2
ENV GRID_ENABLED=false
ENV VNC_ENABLED=false
ENV GENERATE_REPORT=false

# Expose VNC port for debugging
EXPOSE 5900

# Expose port for potential web server (Allure reports)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD /app/health-check.sh

# Set the entrypoint
ENTRYPOINT ["/app/run-tests.sh"]

# Default command (can be overridden)
CMD []

# Labels for metadata
LABEL maintainer="InditexPromotionsTest Team"
LABEL version="1.0.0"
LABEL description="Docker image for running InditexPromotionsTest automation suite"
LABEL java.version="11"
LABEL maven.version="3.9.6"
LABEL chrome.version="stable"
LABEL firefox.version="latest"
LABEL edge.version="stable"