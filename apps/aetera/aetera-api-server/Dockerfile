# Build context is the repository root so that other services can be built the same way.
#
#   ./gradlew :aetera-app:bootJar
#   docker build -f apps/aetera/aetera-api-server/Dockerfile -t aetera-api-server .
#
# Alternatively skip this file entirely and use Paketo buildpacks, which produce an optimised,
# CVE-patched image without a Dockerfile:  ./gradlew :aetera-app:bootBuildImage

FROM eclipse-temurin:21-jre-alpine AS extractor
WORKDIR /build
COPY apps/aetera/aetera-api-server/app/build/libs/*.jar application.jar
# Splits the fat jar into layers that change at different rates, so a code-only change re-uploads
# a few hundred KB instead of the whole dependency set.
RUN java -Djarmode=tools -jar application.jar extract --layers --launcher --destination extracted

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S -G app app

COPY --from=extractor --chown=app:app /build/extracted/dependencies/ ./
COPY --from=extractor --chown=app:app /build/extracted/spring-boot-loader/ ./
COPY --from=extractor --chown=app:app /build/extracted/snapshot-dependencies/ ./
COPY --from=extractor --chown=app:app /build/extracted/application/ ./

USER app
EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
