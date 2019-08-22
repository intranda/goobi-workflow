#!/bin/bash

#
# script_createSymLink.sh
# script called by Goobi to link processes into user home directories
#
# intranda GmbH
#

# create/move alto directory?
USE_OCR=0

# create link to ocr directory in images directory?
LINK_OCR=0

# create link to tei directory in images directory?
LINK_TEI=0


# first argument: source directory
# second argument: name of link
# third argument: user name
SOURCEDIR="$1"
LINKNAME="$2"
USER="$3"

PROCESSTITLE=$(basename "$LINKNAME" | sed -r "s/__\[[0-9]+\]$//")

echo $SOURCEDIR
echo $LINKNAME


if [ ${USE_OCR} -eq 1 ]
then
	OCRDIR=${PROCESSTITLE}_alto
	OCRDIR_DEST=${PROCESSTITLE}_alto
	if [ -d "$SOURCEDIR/../ocr" ]; then
		if [ -d "${SOURCEDIR}/../ocr/${OCRDIR_DEST}" ]; then
			mv "$SOURCEDIR/../ocr/${OCRDIR_DEST}" "${SOURCEDIR}/${OCRDIR}"
		else
			mkdir "${SOURCEDIR}/${OCRDIR}"
		fi
	else
		mkdir "${SOURCEDIR}/${OCRDIR}"
	fi
fi



if [ ${LINK_OCR} -eq 1 ]
then
	if [ -d "$SOURCEDIR/../ocr" ] && ! [ -e "$SOURCEDIR/ocr" ]; then
		ln -s "$SOURCEDIR/../ocr" "$SOURCEDIR/ocr"
	fi
fi



if [ ${LINK_TEI} -eq 1 ]
then
	if [ -d "$SOURCEDIR/../export/${PROCESSTITLE}_tei" ] && ! [ -L "$SOURCEDIR/${PROCESSTITLE}_tei" ]; then
		ln -s "$SOURCEDIR/../export/${PROCESSTITLE}_tei" "$SOURCEDIR/${PROCESSTITLE}_tei"
		sudo /bin/chown -R "$USER" "$SOURCEDIR/../export/${PROCESSTITLE}_tei"
		sudo /bin/chmod g+w "$SOURCEDIR/../export/${PROCESSTITLE}_tei/"*.xml
	fi

	if [ -L "$SOURCEDIR/${PROCESSTITLE}_tei" ]; then
		sudo /bin/chown -R "$USER" "$SOURCEDIR/../export/${PROCESSTITLE}_tei"
		sudo /bin/chmod g+w "$SOURCEDIR/../export/${PROCESSTITLE}_tei/"*.xml
	fi
fi

if [ "${USER}" = "root" ]
then
  echo "setting to read-only"
  sudo /bin/chmod -R g-w "${SOURCEDIR}"
else
  sudo /bin/chmod -R g+w "${SOURCEDIR}"
fi


ln -s "$SOURCEDIR" "$LINKNAME"
sudo /bin/chown -R "$USER" "$SOURCEDIR" 

