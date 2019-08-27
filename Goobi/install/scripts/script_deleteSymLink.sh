#!/bin/bash

#
# script_deleteSymLink.sh
# script called by Goobi to delete symlink to processes in user home directories
#
# intranda GmbH
#

TOMCAT_USER="tomcat8"
TOMCAT_GROUP=${TOMCAT_USER}

# first argument: name of link
LINKNAME="$1"

# if LINKNAME is not a symbolic link, then exit immediately
[[ -L "${LINKNAME}" ]] || exit 1

# get name of temporary ocr directory
PROCESSTITLE=$(basename "$LINKNAME" | sed -r "s/__\[[0-9]+\]$//")
OCRDIR=${PROCESSTITLE}_alto
OCRDIR_DEST=${PROCESSTITLE}_alto

# determine source directory
SOURCEDIR=$(readlink "${LINKNAME}")

# change ownership back to tomcat
sudo chown -R ${TOMCAT_USER}:${TOMCAT_GROUP} $SOURCEDIR

# ensure files are writable and readable by the owner
sudo chmod -R u+rw ${SOURCEDIR}

# ensure files are readable by the group
sudo chmod -R g+r ${SOURCEDIR}

# if an OCR directory exists then move it to its final destination
if [ -d "${LINKNAME}/${OCRDIR}" ]
then
	if [ -d "${SOURCEDIR}/../ocr/${OCRDIR}" ]
	then
		mv "${SOURCEDIR}/${OCRDIR}" "${SOURCEDIR}/../ocr/${OCRDIR_DEST}-$(date +%s)"
		echo "moving ${OCRDIR} to ocr/${OCRDIR_DEST}-$(date +%s)"
	else
		if ! [ -d "${SOURCEDIR}/../ocr" ]
		then
			mkdir "${SOURCEDIR}/../ocr"
		fi	
		mv "${SOURCEDIR}/${OCRDIR}" "${SOURCEDIR}/../ocr/${OCRDIR_DEST}"
	fi
fi

echo $LINKNAME

# remove the symlink
rm "$LINKNAME"
