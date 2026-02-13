FROM maven:3-eclipse-temurin-21 AS build

# you can use --build-arg build=false to skip workflow-core.war compilation, a workflow-core.war file needs to be available in target/workflow-core.war then
ARG build=true

COPY ./ /workflow/
WORKDIR /workflow
RUN echo $build; if [ "$build" = "true" ]; then mvn clean package; elif [ -f "/workflow/target/workflow-core.war" ]; then echo "using existing workflow-core.war"; else echo "not supposed to build, but no workflow-core.war found either"; exit 1; fi

# Build actual application container
FROM tomcat:10-jre21 AS assemble
LABEL maintainer="Matthias Geerdsen <matthias.geerdsen@intranda.com>"

##### SYSTEM PACKAGE INSTALLATION AND UPDATES
RUN apt-get update && \
    apt-get -y install rsync \
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
        unzip \
        python3 \
        mysql-client && \
    apt-get -y clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

RUN rm -rf ${CATALINA_HOME}/webapps/*
# redirect / to /workflow/
RUN mkdir ${CATALINA_HOME}/webapps/ROOT && \
    echo '<% response.sendRedirect("/workflow/"); %>' > ${CATALINA_HOME}/webapps/ROOT/index.jsp
COPY --from=build  /workflow/target/*.war /
RUN unzip /*.war -d /usr/local/tomcat/webapps/workflow && rm /*.war

# Structure is also created again in the run.sh in case of a run bind mount to not crash the container
RUN ["/bin/bash","-c", "mkdir -p /opt/digiverso/goobi/{activemq,config,lib,metadata,rulesets,scripts,static_assets,tmp,xslt,plugins/{administration,command,dashboard,export,GUI,import,opac,statistics,step,validation,workflow}}"]
RUN ["/bin/bash","-c", "mkdir -p /workflow-template/default-plugins/{config,lib,plugins/{administration,command,dashboard,export,GUI,import,opac,statistics,step,validation,workflow}}"]
RUN mkdir -p /usr/local/tomcat/conf/Catalina/localhost/ && mkdir -p /usr/local/tomcat/webapps/workflow

# Install default plugins
RUN set -eu; \
    \
    # OPAC plugins \
    for plugin in pica marc; do \
      curl -fL \
        "https://github.com/intranda/goobi-plugin-opac-${plugin}/releases/latest/download/plugin-opac-${plugin}-base.jar" \
        -o "/workflow-template/default-plugins/plugins/opac/plugin-opac-${plugin}-base.jar"; \
    done; \
    \
    # Step plugins: GUI + base JARs \
    for plugin in file-upload imageqa; do \
      curl -fL \
        "https://github.com/intranda/goobi-plugin-step-${plugin}/releases/latest/download/plugin-step-${plugin}-gui.jar" \
        -o "/workflow-template/default-plugins/plugins/GUI/plugin-step-${plugin}-gui.jar"; \
      curl -fL \
        "https://github.com/intranda/goobi-plugin-step-${plugin}/releases/latest/download/plugin-step-${plugin}-base.jar" \
        -o "/workflow-template/default-plugins/plugins/step/plugin-step-${plugin}-base.jar"; \
    done; \
    \
    # Step plugin configs \
    curl -fL \
      https://github.com/intranda/goobi-plugin-step-file-upload/releases/latest/download/plugin_intranda_step_fileUpload.xml \
      -o /workflow-template/default-plugins/config/plugin_intranda_step_fileUpload.xml; \
    curl -fL \
      https://github.com/intranda/goobi-plugin-step-imageqa/releases/latest/download/plugin_intranda_step_imageQA.xml \
      -o /workflow-template/default-plugins/config/plugin_intranda_step_imageQA.xml; \
    \
    # Dashboard: Extended \
    base=https://github.com/intranda/goobi-plugin-dashboard-extended/releases/latest/download; \
    curl -fL "$base/plugin-dashboard-extended-gui.jar" \
      -o /workflow-template/default-plugins/plugins/GUI/plugin-dashboard-extended-gui.jar; \
    curl -fL "$base/plugin-dashboard-extended-base.jar" \
      -o /workflow-template/default-plugins/plugins/dashboard/plugin-dashboard-extended-base.jar; \
    curl -fL "$base/plugin_intranda_dashboard_extended.xml" \
      -o /workflow-template/default-plugins/config/plugin_intranda_dashboard_extended.xml; \
    \
    # REST: intranda REST \
    curl -fL \
      https://github.com/intranda/goobi-plugin-rest-intranda/releases/latest/download/plugin-rest-intranda-api.jar \
      -o /workflow-template/default-plugins/lib/plugin-rest-intranda-api.jar; \
    \
    # Controlling: intranda statistics \
    base=https://github.com/intranda/goobi-plugin-statistics-intranda/releases/latest/download; \
    for file in \
      plugin-statistics-intranda-gui.jar \
      plugin-statistics-intranda-base.jar \
      statistics_template.pdf \
      statistics_template.xlsx; \
    do \
      curl -fL "$base/$file" \
        -o "/workflow-template/default-plugins/plugins/statistics/$file"; \
    done

# Prepare template configuration for Goobi workflow
ENV CONFIGSOURCE=folder
ENV CONFIG_FOLDER=/workflow-template
COPY install/config/ /workflow-template/config
COPY install/rulesets/ /workflow-template/rulesets
COPY install/scripts/ /workflow-template/scripts
COPY install/xslt/ /workflow-template/xslt
COPY install/db/goobi_blank.sql /workflow-template/db/goobi_blank.sql
RUN mv /workflow-template/config/goobi_config.properties /workflow-template/config/goobi_config.user.properties
# Script adjustments
COPY install/docker/dummy.sh /workflow-template/scripts/
RUN echo -e '#!/bin/bash\nplaceholder="$1"\nVerzeichnis="$2"\n/bin/mkdir "$Verzeichnis"' > /workflow-template/scripts/script_createDirUserHome.sh && \
    rm /workflow-template/scripts/*SymLink.sh && \
    cp /workflow-template/scripts/dummy.sh /workflow-template/scripts/script_createSymLink.sh && \
    cp /workflow-template/scripts/dummy.sh /workflow-template/scripts/script_deleteSymLink.sh
RUN sed -i '\|/usr/bin/sudo /bin/chown|d' /workflow-template/scripts/iii.sh && \
    sed -i 's/sudo //g' /workflow-template/scripts/script_mountImageDir.sh && \
    sed -i 's/sudo //g' /workflow-template/scripts/script_umountImageDir.sh

# General configurations
COPY install/docker/goobi.xml.template /usr/local/tomcat/conf/workflow.xml.template
COPY install/docker/setenv.sh /usr/local/tomcat/bin/setenv.sh
COPY install/docker/server.xml /usr/local/tomcat/conf/server.xml
COPY install/docker/config.py /config.py
COPY install/docker/log4j.xml /opt/digiverso/log4j.xml
COPY install/docker/log4j2.xml /opt/digiverso/log4j2.xml
COPY install/docker/run.sh /run.sh


EXPOSE 8080
CMD ["/run.sh"]
