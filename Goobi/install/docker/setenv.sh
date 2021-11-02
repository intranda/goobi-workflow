#! /bin/sh

# overwrite app specific log4j configuration
export JAVA_OPTS="${JAVA_OPTS} -Dlog4j.configuration=file:///opt/digiverso/log4j.xml"
