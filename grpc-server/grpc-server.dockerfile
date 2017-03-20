FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD build/libs/grpc-server.jar app.jar
RUN sh -c 'touch /app.jar'
ENV JAVA_OPTS=""

COPY src/main/resources/scripts/ip.sh /ip.sh

RUN chmod +x /ip.sh

entrypoint ["/ip.sh"]
cmd ["java"]
