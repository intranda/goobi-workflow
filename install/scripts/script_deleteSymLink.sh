#!/bin/bash

#
# script_deleteSymLink.sh
# script called by Goobi to delete symlink to processes in user home directories
#
# intranda GmbH
#

TOMCAT_USER="tomcat"
TOMCAT_GROUP=${TOMCAT_USER}

# first argument: name of link
LINKNAME="$1"

OCR_LINK_USED=0

# if LINKNAME is not a symbolic link, then exit immediately
[[ -L "${LINKNAME}" ]] || exit 1

# get name of temporary ocr directory
PROCESSTITLE=$(basename "$LINKNAME" | sed -r "s/__\[[0-9]+\]$//")
ALTODIR="${PROCESSTITLE}_alto"

# determine source directory
SOURCEDIR=$(readlink "${LINKNAME}")

# check for linked OCR directory
[[ -L "${LINKNAME}/ocr" ]] && OCR_LINK_USED=1

# change ownership back to tomcat
sudo chown -R ${TOMCAT_USER}:${TOMCAT_GROUP} "$SOURCEDIR"
[ ${OCR_LINK_USED} -eq 1 ] && sudo chown -R ${TOMCAT_USER}:${TOMCAT_GROUP} "${LINKNAME}/ocr/"

# ensure files are writable and readable by the owner
sudo chmod -R u+rw "${SOURCEDIR}"
[ ${OCR_LINK_USED} -eq 1 ] && sudo chmod -R u+rw ${TOMCAT_USER}:${TOMCAT_GROUP} "${LINKNAME}/ocr/"

# ensure files are readable by the group
sudo chmod -R g+r "${SOURCEDIR}"
[ ${OCR_LINK_USED} -eq 1 ] && sudo chmod -R g+r ${TOMCAT_USER}:${TOMCAT_GROUP} "${LINKNAME}/ocr/"

# if an ALTO directory exists then move it to its final destination
if [ -d "${LINKNAME}/${ALTODIR}" ]
then
	if [ -d "${SOURCEDIR}/../ocr/${ALTODIR}" ]
	then
		mv "${SOURCEDIR}/${ALTODIR}" "${SOURCEDIR}/../ocr/${ALTODIR}-$(date +%s)"
		echo "moving ${ALTODIR} to ocr/${ALTODIR}-$(date +%s)"
	else
		if ! [ -d "${SOURCEDIR}/../ocr" ]
		then
			mkdir "${SOURCEDIR}/../ocr"
		fi
		mv "${SOURCEDIR}/${ALTODIR}" "${SOURCEDIR}/../ocr/"
	fi
fi

echo "$LINKNAME"

# remove the symlink
[ ${OCR_LINK_USED} -eq 1 ] && rm "${LINKNAME}/ocr"
rm "$LINKNAME"
