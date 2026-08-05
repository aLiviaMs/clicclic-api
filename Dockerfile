# ---- Stage 1: build ----
# Uses a full JDK + Maven image (heavy, ~500MB+) just to compile.
# This entire stage is discarded later — none of its weight ends up
# in the final image, only the .jar it produces.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml first and download dependencies before copying source code.
# Docker caches each instruction as a "layer": as long as pom.xml doesn't
# change, this dependency download is reused on the next build instead of
# re-running — turns a 2-minute rebuild into a few seconds during development.
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

# ---- Stage 2: runtime ----
# A JRE (Java Runtime Environment) is enough to RUN a .jar — it doesn't
# need the compiler tooling the JDK/Maven stage required. Much smaller,
# smaller image = faster deploys and a lower attack surface.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Containers default to running as root, which is unnecessary risk: if an
# attacker exploited a bug in the app, root-in-container is a worse outcome
# than a restricted user. Standard hardening practice.
RUN addgroup -S clicclic && adduser -S clicclic -G clicclic
USER clicclic

# Only the compiled artifact crosses over from the build stage —
# not Maven, not the JDK, not the source code.
COPY --from=build /app/target/clicclic-api.jar app.jar

EXPOSE 8080

# MaxRAMPercentage instead of a fixed -Xmx: the JVM reads the container's
# memory limit (set later at deploy time, e.g. 512MB on Fly.io/Render) and
# sizes its heap as a percentage of that automatically, instead of a
# hardcoded number that could exceed whatever the container is actually
# allowed to use.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
