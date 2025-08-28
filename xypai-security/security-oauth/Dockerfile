FROM openjdk:11-jdk
LABEL maintainer="XY相遇派团队 <dev@xypai.com>"

# 时区修改为东八区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /security-oauth
ARG PACKAGE_PATH=./target/security-oauth.jar
ADD ${PACKAGE_PATH:-./} security-oauth.jar

EXPOSE 9401

ENV JAVA_OPTS="\
-server \
-Xms512m \
-Xmx1024m \
-XX:MetaspaceSize=256m \
-XX:MaxMetaspaceSize=512m\
-Dspring.profiles.active=prod"

ENTRYPOINT ["sh","-c","java -Djava.security.egd=file:/dev/./urandom -jar $JAVA_OPTS security-oauth.jar"]
