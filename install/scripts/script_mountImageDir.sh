#!/bin/bash

#
# script_mountImageDir.sh
# script called by Goobi to link processes into user home directories using mount --bind
#
# intranda GmbH
#

TOMCAT_USER="tomcat"
TOMCAT_GROUP=${TOMCAT_USER}


# first argument: source dir
# second argument: name of link
# third argument: user name

SOURCEDIR="$1"
LINKNAME="$2"
USER="$3"

PROCESSTITLE=$(basename $(echo $2))
PERMOUDIR=/opt/digiverso/other/mounts/

echo "Mounting: $LINKNAME";

## create dir as root because its chrooted
sudo /bin/mkdir "$LINKNAME"

sudo /bin/chown ${TOMCAT_USER}:${TOMCAT_GROUP} "$LINKNAME"

if [ "${USER}" = "root" ]
then
	echo "setting to read-only"
	sudo /bin/chmod -R g-w "${SOURCEDIR}"
else
	sudo /bin/chmod -R g+w "${SOURCEDIR}"
fi
 
## mount folder
sudo /bin/mount --bind "$SOURCEDIR" "$LINKNAME"
 
## set rights
sudo /bin/chown -R "$USER" "$SOURCEDIR" 

## save mount call in persistent mount directory
echo "sudo /bin/mount --bind \"$SOURCEDIR\" \"$LINKNAME\"" >> $PERMOUDIR/${PROCESSTITLE}_${USER}



