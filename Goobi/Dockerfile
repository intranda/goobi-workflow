FROM maven:3.6-jdk-11 AS BUILD

COPY . /goobi/
WORKDIR /goobi

RUN mvn -f Goobi/pom.xml clean package


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

COPY Goobi/install/docker/goobi.xml.template /usr/local/tomcat/conf/goobi.xml.template
COPY Goobi/install/config/ /opt/digiverso/goobi/config/
COPY Goobi/install/rulesets/ /opt/digiverso/goobi/rulesets/
COPY Goobi/install/scripts/ /opt/digiverso/goobi/scripts/
COPY Goobi/install/xslt/ /opt/digiverso/goobi/xslt/
COPY Goobi/install/docker/setenv.sh /usr/local/tomcat/bin/setenv.sh
COPY Goobi/install/docker/server.xml /usr/local/tomcat/conf/server.xml
COPY Goobi/install/docker/run.sh /run.sh
COPY Goobi/install/docker/log4j.xml /opt/digiverso/log4j.xml

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

COPY --from=BUILD  /goobi/Goobi/module-war/target/goobi.war /
RUN unzip /goobi.war -d /usr/local/tomcat/webapps/goobi && rm /goobi.war

EXPOSE 8080
CMD ["/run.sh"]
