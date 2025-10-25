FROM bellsoft-jdk-lite-with-tini:17.0.13-12-20241031

USER root

RUN yum install wget -y

WORKDIR /

COPY --chown=node:node ./backend/target/devflow.jar app.jar

USER node

ENV JAVA_STARTUP_CMD="java --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED -Dlog4j2.formatMsgNoLookups=true -Dserver.port=8080 -jar app.jar"
