FROM maven:3.9.3-eclipse-temurin-17 AS BUILD
COPY . /goobi/
WORKDIR /goobi
RUN mvn clean package

# Build actual application container
FROM tomcat:8.5-jre8 as ASSEMBLE
LABEL maintainer="Matthias Geerdsen <matthias.geerdsen@intranda.com>"

ENV DB_SERVER goobi-db
ENV DB_PORT 3306
ENV DB_NAME goobi
ENV DB_USER goobi
ENV DB_PASSWORD goobi

RUN ["/bin/bash","-c", "mkdir -p /opt/digiverso/goobi/{activemq,config,lib,metadata,rulesets,scripts,static_assets,tmp,xslt,plugins/{administration,command,dashboard,export,GUI,import,opac,statistics,step,validation,workflow}}"]
RUN mkdir -p /usr/local/tomcat/conf/Catalina/localhost/ && mkdir -p /usr/local/tomcat/webapps/goobi

COPY install/docker/goobi.xml.template /usr/local/tomcat/conf/goobi.xml.template
COPY install/config/ /opt/digiverso/goobi/config/
COPY install/rulesets/ /opt/digiverso/goobi/rulesets/
COPY install/scripts/ /opt/digiverso/goobi/scripts/
COPY install/xslt/ /opt/digiverso/goobi/xslt/
COPY install/docker/setenv.sh /usr/local/tomcat/bin/setenv.sh
COPY install/docker/server.xml /usr/local/tomcat/conf/server.xml
COPY install/docker/run.sh /run.sh
COPY install/docker/log4j.xml /opt/digiverso/log4j.xml

RUN sed -i 's/TOMCATUSER=tomcat/TOMCATUSER=root/' /opt/digiverso/goobi/scripts/iii.sh

RUN apt-get update && \
    apt-get -y install rsync \
        sudo \
        imagemagick \
        libtiff-tools \
        graphicsmagick \
        exiv2 \
        bc \
        rename \
        file \
        mysql-client \
        gettext-base && \
    apt-get -y clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* && \
    rm -rf ${CATALINA_HOME}/webapps/*

# redirect / to /goobi/
RUN mkdir ${CATALINA_HOME}/webapps/ROOT && \
    echo '<% response.sendRedirect("/goobi/"); %>' > ${CATALINA_HOME}/webapps/ROOT/index.jsp

COPY --from=BUILD  target/workflow-core*.war /goobi.war
RUN unzip /goobi.war -d /usr/local/tomcat/webapps/goobi && rm /goobi.war

EXPOSE 8080
CMD ["/run.sh"]
