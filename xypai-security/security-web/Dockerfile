FROM openjdk:11-jdk
LABEL maintainer="XY相遇派团队 <dev@xypai.com>"

# 时区修改为东八区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /security-web
ARG PACKAGE_PATH=./target/security-web.jar
ADD ${PACKAGE_PATH:-./} security-web.jar

EXPOSE 9402

ENV JAVA_OPTS="\
-server \
-Xms256m \
-Xmx512m \
-XX:MetaspaceSize=256m \
-XX:MaxMetaspaceSize=512m\
-Dspring.profiles.active=prod"

ENTRYPOINT ["sh","-c","java -Djava.security.egd=file:/dev/./urandom -jar $JAVA_OPTS security-web.jar"]
