FROM tomcat:10.1-jdk21

# Tomcat付属のサンプルアプリを消す
RUN rm -rf /usr/local/tomcat/webapps/*

# アプリ本体をROOTに置く
COPY src/main/webapp/ /usr/local/tomcat/webapps/ROOT/

# 公開用の接続設定で context.xml を上書きする
COPY deploy/context.xml /usr/local/tomcat/webapps/ROOT/META-INF/context.xml

# Java をコンパイルして WEB-INF/classes に出力する
COPY src/main/java/ /tmp/src/
RUN mkdir -p /usr/local/tomcat/webapps/ROOT/WEB-INF/classes \
 && find /tmp/src -name "*.java" > /tmp/sources.txt \
 && javac -encoding UTF-8 \
      -cp /usr/local/tomcat/lib/servlet-api.jar \
      -d /usr/local/tomcat/webapps/ROOT/WEB-INF/classes \
      @/tmp/sources.txt \
 && rm -rf /tmp/src /tmp/sources.txt

ENV JAVA_OPTS="-Xmx320m"

EXPOSE 8080