FROM openjdk:jre-alpine
VOLUME /tmp
ARG JAR_FILE

ENV _JAVA_OPTIONS "-Xms256m -Xmx512m -Djava.awt.headless=true"

COPY ${JAR_FILE} /opt/homeAutomation.jar

RUN addgroup homeAutomation && \
    adduser -D -S -h /var/cache/homeAutomation -s /sbin/nologin -G homeAutomation homeAutomation

WORKDIR /opt
USER homeAutomation
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/opt/homeAutomation.jar"]
