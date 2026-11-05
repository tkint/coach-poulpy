# The server image. Build from the repository root, since the server depends on core modules:
#   docker build -f server/Dockerfile -t coach-poulpy-server .
# See docs/deploying-the-server.md.

FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY . .
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon -q :server:installDist

FROM eclipse-temurin:21-jre
# The release, reported on /health; the Release workflow passes it, any other build says "dev".
ARG APP_VERSION=dev
RUN useradd --system --uid 10001 --no-create-home coach
COPY --from=build /src/server/build/install/server /opt/server
USER coach
ENV APP_VERSION=${APP_VERSION} \
    PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
    CMD ["bash", "-c", "exec 3<>/dev/tcp/127.0.0.1/${PORT} && printf 'GET /health HTTP/1.0\\r\\n\\r\\n' >&3 && grep -q '200 OK' <&3"]
ENTRYPOINT ["/opt/server/bin/server"]
