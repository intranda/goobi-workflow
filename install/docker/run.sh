#!/bin/bash

set -e

WORKFLOW_UID=${WORKFLOW_UID:-1000}
WORKFLOW_GID=${WORKFLOW_GID:-1000}
groupmod -o -g "${WORKFLOW_GID}" user
usermod -o -u "${WORKFLOW_UID}" user

[ -z "$CONFIGSOURCE" ] && CONFIGSOURCE="default"
[ -z "${WORKFLOW_BASE_PATH+x}" ] && WORKFLOW_BASE_PATH="/workflow"

set -u

# Create directories if not present (because of bind mount)
mkdir -p /opt/digiverso/goobi/{activemq,config,lib,metadata,rulesets,scripts,static_assets,tmp,xslt,plugins/{administration,command,dashboard,export,generic,GUI,import,opac,statistics,step,theme,validation,workflow}}

echo "Setting properties from environment variables"
/usr/bin/python3 /config.py

set +u

# /path/to/application -> path#to#application  (Tomcat nested-context convention)
# / (root)             -> ROOT
if [[ "$WORKFLOW_BASE_PATH" == "/" ]]; then
  WEBAPP_NAME="ROOT"
  WORKFLOW_BASE_PATH=""
elif ! [[ "$WORKFLOW_BASE_PATH" =~ ^(/[a-zA-Z0-9_-]+)+$ ]]; then
  echo "WORKFLOW_BASE_PATH invalid: 'WORKFLOW_BASE_PATH'"
  exit 1
else
  WEBAPP_NAME="${WORKFLOW_BASE_PATH#/}"
  WEBAPP_NAME="${WEBAPP_NAME//\//#}"
  WORKFLOW_BASE_PATH="${WORKFLOW_BASE_PATH#/}"
  # redirect to application path
  rm -rf "${CATALINA_HOME}/webapps/ROOT" && mkdir "${CATALINA_HOME}/webapps/ROOT" && echo "<% response.sendRedirect(\"/${WORKFLOW_BASE_PATH}/\"); %>" > "${CATALINA_HOME}/webapps/ROOT/index.jsp"
fi
WEBAPP_DIR="${CATALINA_HOME}/webapps/${WEBAPP_NAME}"

if [[ "$WEBAPP_NAME" != "workflow" && -d "${CATALINA_HOME}/webapps/workflow" && ! -d "$WEBAPP_DIR" ]]; then
    mv "${CATALINA_HOME}/webapps/workflow" "$WEBAPP_DIR"
fi

echo "Setting database configuration from environment..."
envsubst "\$DB_HOST \$DB_PORT \$DB_NAME \$DB_USER \$DB_PASSWORD" < "${CATALINA_HOME}/conf/workflow.xml.template" > "${CATALINA_HOME}/conf/Catalina/localhost/${WEBAPP_NAME}.xml"

export MYSQL_PWD="${DB_PASSWORD}"

while ! mysql -h "${DB_HOST}" -u "${DB_USER}" -P "${DB_PORT}" -e "SELECT 1" >/dev/null 2>&1; do
    echo "Waiting for database to boot..."
    sleep 2
done

TABLE_COUNT=$(mysql -h "${DB_HOST}" -u "${DB_USER}" -P "${DB_PORT}" -Nse "SELECT COUNT(*) FROM information_schema.tables WHERE TABLE_SCHEMA = '${DB_NAME}';")

if [[ "${TABLE_COUNT}" -eq 0 ]]; then
  echo "Initializing database..."
  mysql -h "${DB_HOST}" -u "${DB_USER}" -P "${DB_PORT}" "${DB_NAME}" < /workflow-template/db/goobi_blank.sql
  echo "Done"
else
  echo "Database already initialized. Skipping"
fi

if [[ -v PW_GOOBITESTUSER ]]; then
  echo "Setting password for test users"
  SALT=$(head -c 16 /dev/urandom | base64)
  export SALT
  ENCRYPTED_PW=$(python3 -c "import hashlib,base64,os;d=hashlib.sha256(os.getenv('SALT').encode()+os.getenv('PW_GOOBITESTUSER').encode()).digest();[d:=hashlib.sha256(d).digest() for _ in range(1,10000)];print(base64.b64encode(d).decode())")
  mysql -h "${DB_HOST}" -u "${DB_USER}" -e "USE goobi;UPDATE benutzer SET salt='$SALT', encryptedPassword='$ENCRYPTED_PW' WHERE login IN ('testadmin','testmetadata','testbookmanager','testscanning','testqc','testprojectmanagement','goobi');"
fi


if [[ -d "/workflow-template/default-plugins" ]]; then
  echo "Checking if default plugins are present"
  cp -r /workflow-template/default-plugins/plugins/* /opt/digiverso/goobi/plugins/
  cp -r --update=none /workflow-template/default-plugins/config/* /opt/digiverso/goobi/config/
  cp -r /workflow-template/default-plugins/lib/* /opt/digiverso/goobi/lib/
fi

set -u

if [ -n "${WORKING_STORAGE:-}" ]
then
  CATALINA_TMPDIR="${WORKING_STORAGE}/workflow/jvmtemp"
  mkdir -p "${CATALINA_TMPDIR}"
  echo >> "${CATALINA_HOME}/bin/setenv.sh"
  echo "CATALINA_TMPDIR=${CATALINA_TMPDIR}" >> "${CATALINA_HOME}/bin/setenv.sh"
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

echo "Updating file ownership..."
# this will cause less disk access than `chown -R`
find "${CATALINA_HOME}" /opt/digiverso/goobi/ \! -user user \( -exec chown user:user '{}' + -o -true \)

echo "Starting application server..."
exec gosu user catalina.sh run

