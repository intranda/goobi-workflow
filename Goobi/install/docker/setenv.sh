#! /bin/sh

# enable awareness of cgroup memory limits in docker container
export CATALINA_OPTS="$CATALINA_OPTS -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"

# overwrite app specific log4j configuration
export JAVA_OPTS="${JAVA_OPTS} -Dlog4j.configuration=file:///opt/digiverso/log4j2.xml"
