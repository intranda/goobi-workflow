#!/bin/bash

#
# script_createSymLink.sh
# script called by Goobi to link processes into user home directories
#
# intranda GmbH
#

# create/move alto directory?
MOVE_ALTO=0

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

OCR_LINK_USED=0

PROCESSTITLE=$(basename "$LINKNAME" | sed -r "s/__\[[0-9]+\]$//")

echo "$SOURCEDIR"
echo "$LINKNAME"


if [ ${MOVE_ALTO} -eq 1 ]
then
	ALTODIR="${PROCESSTITLE}_alto"
	if [ -d "$SOURCEDIR/../ocr" ]; then
		if [ -d "${SOURCEDIR}/../ocr/${ALTODIR}" ]; then
			mv "$SOURCEDIR/../ocr/${ALTODIR}" "${SOURCEDIR}/"
		else
			mkdir "${SOURCEDIR}/${ALTODIR}"
		fi
	else
		mkdir "${SOURCEDIR}/${ALTODIR}"
	fi
fi



if [ ${LINK_OCR} -eq 1 ]
then
	if [ -d "$SOURCEDIR/../ocr" ] && ! [ -e "$SOURCEDIR/ocr" ]; then
		ln -s "$SOURCEDIR/../ocr" "$SOURCEDIR/ocr"
		OCR_LINK_USED=1
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
  [ ${OCR_LINK_USED} -eq 1 ] && sudo /bin/chmod -R g-w "$SOURCEDIR/ocr"
else
  sudo /bin/chmod -R g+w "${SOURCEDIR}"
  [ ${OCR_LINK_USED} -eq 1 ] && sudo /bin/chmod -R g+w "$SOURCEDIR/ocr"
fi


ln -s "$SOURCEDIR" "$LINKNAME" &&
sudo /bin/chown -R "$USER" "$SOURCEDIR"

[ ${OCR_LINK_USED} -eq 1 ] && sudo /bin/chown -R "$USER" "$SOURCEDIR/ocr/"

ls -al "$LINKNAME"
