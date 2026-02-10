#!/bin/bash

set -e

[ -z "$CONFIGSOURCE" ] && CONFIGSOURCE="default"

set -u

echo "Generating config file /opt/digiverso/goobi/config/goobi_config.properties"
/usr/bin/python3 /config.py

echo "Setting database configuration from environment..."
envsubst '\$DB_HOST \$DB_PORT \$DB_NAME \$DB_USER \$DB_PASSWORD' </usr/local/tomcat/conf/workflow.xml.template > /usr/local/tomcat/conf/Catalina/localhost/workflow.xml

set +u

if [[ -v PW_GOOBITESTUSER ]]; then
  while ! mysqladmin ping -h "${DB_HOST}" -u "${DB_USER}" --password="${DB_PASSWORD}" --silent; do
      echo "Waiting for database to boot..."
      sleep 2
  done
  echo "Setting password for test users"
  SALT=$(head -c 16 /dev/urandom | base64)
  export SALT
  ENCRYPTED_PW=$(python3 -c "import hashlib,base64,os;d=hashlib.sha256(os.getenv('SALT').encode()+os.getenv('PW_GOOBITESTUSER').encode()).digest();[d:=hashlib.sha256(d).digest() for _ in range(1,10000)];print(base64.b64encode(d).decode())")
  mysql -h "${DB_HOST}" -u "${DB_USER}" --password="${DB_PASSWORD}" -e "USE goobi;UPDATE benutzer SET salt='$SALT', encryptedPassword='$ENCRYPTED_PW' WHERE login IN ('testadmin','testmetadata','testbookmanager','testscanning','testqc','testprojectmanagement','goobi');"
fi


if [[ ${LOAD_DEFAULT_PLUGINS,,} == true ]]; then
  echo "Checking if default plugins are present"
  cp -r --update=none /workflow-template/default-plugins/plugins/ /opt/digiverso/goobi/plugins/
  cp -r --update=none /workflow-template/default-plugins/config/ /opt/digiverso/goobi/config/
  cp -r --update=none /workflow-template/default-plugins/lib/ /opt/digiverso/goobi/lib/
fi

set -u

if [ -n "${WORKING_STORAGE:-}" ]
then
  CATALINA_TMPDIR="${WORKING_STORAGE}/workflow/jvmtemp"
  mkdir -p "${CATALINA_TMPDIR}"
  echo >> /usr/local/tomcat/bin/setenv.sh
  echo "CATALINA_TMPDIR=${CATALINA_TMPDIR}" >> /usr/local/tomcat/bin/setenv.sh
fi

case $CONFIGSOURCE in
  s3)
    if [ -z "$AWS_S3_BUCKET" ]
    then
      echo "AWS_S3_BUCKET is required"
      exit 1
    fi
    echo "Pulling configuration from s3 bucket"
    aws s3 cp s3://$AWS_S3_BUCKET/goobi/config/ /opt/digiverso/goobi/config/ --recursive
    aws s3 cp s3://$AWS_S3_BUCKET/goobi/rulesets/ /opt/digiverso/goobi/rulesets/ --recursive
    aws s3 cp s3://$AWS_S3_BUCKET/goobi/xslt/ /opt/digiverso/goobi/xslt/ --recursive
    ;;
  folder)
    if [ -z "$CONFIG_FOLDER" ]
    then
      echo "CONFIG_FOLDER is required"
      exit 1
    fi

    if ! [ -d "$CONFIG_FOLDER" ]
    then
      echo "CONFIG_FOLDER: $CONFIG_FOLDER does not exists or is not a folder"
      exit 1
    fi
    
    echo "Copying configuration from local folder"
    [ -d "$CONFIG_FOLDER"/config ] && cp -arv --update=none "$CONFIG_FOLDER"/config/* /opt/digiverso/goobi/config/
    [ -d "$CONFIG_FOLDER"/rulesets ] && cp -arv --update=none "$CONFIG_FOLDER"/rulesets/* /opt/digiverso/goobi/rulesets/
    [ -d "$CONFIG_FOLDER"/scripts ] && cp -arv --update=none "$CONFIG_FOLDER"/scripts/* /opt/digiverso/goobi/scripts/
    [ -d "$CONFIG_FOLDER"/xslt ] && cp -arv --update=none "$CONFIG_FOLDER"/xslt/* /opt/digiverso/goobi/xslt/
    ;;

  *)
    echo "Keeping configuration"
    ;;
esac



echo "Starting application server..."
exec catalina.sh run

