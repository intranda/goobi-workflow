FROM maven:3.9.3-eclipse-temurin-17 AS build

# you can use --build-arg build=false to skip workflow-core.war compilation, a workflow-core.war file needs to be available in target/workflow-core.war then
ARG build=true

COPY ./ /workflow/
WORKDIR /workflow
RUN echo $build; if [ "$build" = "true" ]; then mvn clean package; elif [ -f "/workflow/target/workflow-core.war" ]; then echo "using existing workflow-core.war"; else echo "not supposed to build, but no workflow-core.war found either"; exit 1; fi

# Build actual application container
FROM tomcat:9-jre17 AS assemble
LABEL maintainer="Matthias Geerdsen <matthias.geerdsen@intranda.com>"

ENV DB_SERVER workflow-db
ENV DB_PORT 3306
ENV DB_NAME goobi
ENV DB_USER goobi
ENV DB_PASSWORD goobi

RUN ["/bin/bash","-c", "mkdir -p /opt/digiverso/goobi/{activemq,config,lib,metadata,rulesets,scripts,static_assets,tmp,xslt,plugins/{administration,command,dashboard,export,GUI,import,opac,statistics,step,validation,workflow}}"]
RUN mkdir -p /usr/local/tomcat/conf/Catalina/localhost/ && mkdir -p /usr/local/tomcat/webapps/workflow

COPY install/docker/goobi.xml.template /usr/local/tomcat/conf/workflow.xml.template
COPY install/config/ /opt/digiverso/goobi/config/
COPY install/rulesets/ /opt/digiverso/goobi/rulesets/
COPY install/scripts/ /opt/digiverso/goobi/scripts/
COPY install/xslt/ /opt/digiverso/goobi/xslt/
COPY install/docker/setenv.sh /usr/local/tomcat/bin/setenv.sh
COPY install/docker/server.xml /usr/local/tomcat/conf/server.xml
COPY install/docker/run.sh /run.sh
COPY install/docker/log4j.xml /opt/digiverso/log4j.xml
COPY install/docker/log4j2.xml /opt/digiverso/log4j2.xml

COPY install/docker/dummy.sh /opt/digiverso/goobi/scripts/
RUN sed -i 's/^script_createSymLink=script_createSymLink.sh/script_createSymLink=dummy.sh/' /opt/digiverso/goobi/config/goobi_config.properties
RUN sed -i 's/^script_deleteSymLink=script_deleteSymLink.sh/script_deleteSymLink=dummy.sh/' /opt/digiverso/goobi/config/goobi_config.properties

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
        gettext-base \
        libopenjp2-7 \
        git \
        fontconfig \
        poppler-utils \
        pdftk \
        unzip && \
    apt-get -y clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* && \
    rm -rf ${CATALINA_HOME}/webapps/*

# redirect / to /workflow/
RUN mkdir ${CATALINA_HOME}/webapps/ROOT && \
    echo '<% response.sendRedirect("/workflow/"); %>' > ${CATALINA_HOME}/webapps/ROOT/index.jsp

COPY --from=build  /workflow/target/*.war /
RUN unzip /*.war -d /usr/local/tomcat/webapps/workflow && rm /*.war

# Manually patch this until 'workflow' is used everywhere
RUN sed -i 's/goobi\.xml/workflow\.xml/g' /run.sh
RUN sed -i 's/\/goobi\/jvmtemp/\/workflow\/jvmtemp/g' /run.sh

EXPOSE 8080
CMD ["/run.sh"]
