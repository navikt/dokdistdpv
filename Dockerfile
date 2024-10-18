FROM eclipse-temurin:21-jre AS builder
WORKDIR /build
COPY app/target/app.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM ghcr.io/navikt/baseimages/temurin:21
WORKDIR /app
COPY --from=builder build/dependencies/ ./
COPY --from=builder build/snapshot-dependencies/ ./
COPY --from=builder build/spring-boot-loader/ ./
COPY --from=builder build/application/ ./
COPY export-vault-secrets.sh /init-scripts/10-export-vault-secrets.sh
COPY dokdistdpv-java-opts.sh /init-scripts/20-dokdistdpv-java-opts.sh
COPY run-java.sh /
USER root
RUN apt-get install -y --no-install-recommends jq
RUN chmod +x /run-java.sh
USER apprunner

ENV MAIN_CLASS="org.springframework.boot.loader.launch.JarLauncher"