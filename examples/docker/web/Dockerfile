FROM vertx-nashorn:latest
COPY . /code
WORKDIR /code
EXPOSE "8080"
# Debug
# EXPOSE 5005
# CMD ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005", "-jar", "/usr/vertx/run.jar"]