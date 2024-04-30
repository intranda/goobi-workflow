#!/bin/bash
set -u

####################
#####  iii.sh
#####  Copyright (c) 2010-2011 - Jan Toenjes <jan.toenjes@intranda.com>
#####
##
##  History:
##   0.4    - added MAXPROCS option and using xargs now
##   0.5    - added JPEG_QUALITY option - request from greifswald
##   0.6    - added tiffwriter.conf utf-8 conversion for gdz
##   0.7    - added email notification after compress_jpeg step
##   0.8    - highly improoved straighten stuff
##   0.8.1  - fixed tiffwriter.conf encoding detection, fixed default email success header
##   0.9    - switched from convert to mogrify
##   0.10   - added skip of jpeg compression in case the picture is bitonal
##   0.11   - switched from jpeg compression to LZW
##   0.11.1 - changed default email header - vorgangstitel now in subject
##   0.11.2 - changed tmp filename, added datestring
##   0.12   - added renaming of *.TIF to *.tif after changing to workingdir
##   0.13   - added compress_images -> resizing and dpi change
##   0.13.1 - changed TIF to tif renaming loop from if to for
##   0.13.2 - changed TIF to tif renaming loop again
##   0.13.3 - changed LZW to JPEG compression
##   0.14   - added check if *.tif files exist before trying to convert
##   0.15   - added convert option if files are .jpg to convert them to TIFF/LZW
##   0.15.1 - better error handling due to more error stream redirections i.e. (tiffinfo)
##   0.16   - added convert_images option that converts djvu and sid files based on file extension
##   0.16.1 - be more tolerant with warnings at bad tiff-header fields
##   0.17   - added feature to set correct tiffheader field 259 when compression is raw but field not set
##   0.17.1 - changed TIFF-Header Software (c) to from 2010 to 2010-2011
##   0.17.2 - be more tolerant with warnings at straighten
##   0.18   - added case to convert_images option with different fileformats in one folder
##   0.19   - changed all backticks to $() for better compatibillity
##            corrected typo error at correct259) option
##	      changed renaming of *.TIFF only to global tolower
##            changed output of ddjvu and geoutils firstcheck to be stdout instead of stderr
##            added VERBOSE option for debugging information
##   0.20   - be more tolerant with warnings, added -quiet parameter to all imagemagick tools calls
##   0.20.1 - changed monochrome compression to FAX compression (CCITT Group 4)
##   0.21   - added check if picture is 16bit and set 8bit depth manually if yes during jpeg compression
##   0.22   - added complete new way of deskewing images and detecting and removing black borders
##   0.23   - added convert_office option to convert office documents to pdf using unoconv
##   0.24   - added jpeg generation for Wellcome
##   0.24.1 - be more tolerant with warnings and errors at jpeg generation for wellcome
##   0.25   - added possibility of running all commands with a nice valie
##   0.26   - added new clean_tiffheader option to clean tiff header using -strip parameter from imagemagick mogrify
##   0.27   - removed unoconf stuff
##   0.28   - added check for cifs file system and use workaround to rename files to lower case then
##   0.28.1 - added several comments and simplified moving to jpg folder at create_jpeg option
##   0.29   - added function to compare version numbers to see if installed imagemagick version can create tiled tiffs
##            the code is from: http://stackoverflow.com/questions/4023830/bash-how-compare-two-strings-in-version-format
##          - added create_tile_tiff option
##          - moved preparing stuff to prepare function to provide a better help screen if no valid action was given
##          - added SHOWARNINGS option to hide warnings if wanted...
##  0.30    - added switch to use graphicsmagick instead of imagemagick
##          - changed default indent to tab
##  0.31    - moved some imagemagick and other external tool calls to find -print0 | xargs -0 to have better multicore support
##  0.32    - merged watermark generation from hu-berlin by Michael Voss - mvoss@ub.hu-berlin.de
##  0.33    - moved CIFS config option to top
##          - changed create_jpeg to create folder first, rename folder if exist and convert writes jpgs directly into folder, removed file moving
##          - added option to replace white spaces with underscore in prepare function
##  0.34    - write tiff header if tiffwriter.conf is utf-8 OR ASCII (was utf-8 only before)
##  0.35    - improved watermark generation to render same size of watermark for image, even if image size differs
##  0.36    - if bash is not version 4 rename files to lowercase using tr and not using bash internal function
##  0.37    - removed sleep setting after each command
##          - changed variable writing from $NAME to ${NAME}
##          - added new cut_pixel_bottom setting to cut a specific amount of pixel on the bottom of each image - used to crop an existing footer
##  0.38    - removed -strip option from all graphicsmagick (gm) calls because it is unknown
##  0.39    - removed -quiet for gm, readded -strip, don't ask...
##  0.40    - added possibility to pass CUTPIXELAMOUNT as third parameter to this script
##          - be more verbose at cut_pixel_bottom and store information about each image in logfile
##  0.41    - added create_thumbnails option to create thumbs that fit in a box. The box parameters must be defined in the settings
##  0.42    - changed create_thumbnails option to be leiden specific
##  0.43    - changed create_thumbnails option to read only the first image from multi image tiff files and create jpg and no tiff files
##  0.44    - changed create_thumbnails option to include -quiet parameter at convert call
##  0.45    - enhanced create_tile_tiff option to specify degrees in case you'd like to rotate it. Possible values are 90, 180 and 270 specified as
##            third optional option. Added possibillity to tile only one image as well.
##  0.46    - changed error handling at generate_watermark option because warnings are send to stderr from gm composite and there is no -quiet option
##            available as in the imagemagick calls to supress warnings...
##  0.47    - added -depth 8 parameter at every call at convert_jpeg to prevent errors when automatically setting it to 1 together
##            with JPEG wich might cause trouble (i.e. at ZLB)
##  0.48    - added "" arrount ${i} at renaming files to lowercase to prevent errors with spaces...
##  0.49    - more "" arround ${i} and default settings to tomcat8...
##  0.50    - remove alpha channel in tiff jpegs/ ##
##  0.50.1  - remove -alpha off option for gm mogrify calls
##  0.52    - only extensions are renamed to lower case, not the whole file name
##  0.53    - generate_watermark only with imagemagick
##          - check for gm installed
##  0.54    - add +matte on gm calls that hat -compress JPEG (same as -alpha off on imagemagick)
##  0.55    - removed TMPFILE and one old deprecated block for straightening
##  0.56    - check if exiv2 is installed, used for trustytahr wrong tiffheader stuff
##  0.57    - add more error handling
##  0.58    - write_tiffheader: more generic UTF8/ASCII check; check for tiffwriter.conf; more error handling in function prepare
##  0.59    - convert_jpeg: convert *.jpg to Tif, now using JPEG instead of LZW
##  0.60    - prepare: use two step renaming for CIFS
##  0.61    - prepare: make prepare available as just and only prepare command
##  0.62    - new: tiffjpeg_resize, convert_jpeg simplified, more "" for vars
##  0.63    - convert_jpeg: -colorspace RGB if using gm - otherwise gm won't compress tiff(cmyk) to tiff/jpeg(rgb)
##  0.64    - new: create_jpeg_size: create jpg in a _jpg folder with a given size ($3 in KB)
##  0.65    - new: tif2jpg: Create tif from jpg, /bin/bash $0 tif2jpg {origpath} {tifpath}
##  0.66    - new: mogrify-fx: Apply fx (imagemagick only)
##  0.67    - rename tiff->tif, jpeg->jpg, error handling
##  0.68    - new: convert_jpeg_rm_icc - like convert_jpeg, but apply and remove ICC profile if existent.
##  0.69    - new: decompress tif to uncompressed tif.
##  0.70    - new: create_tiffjpeg - convert all tiff and jpg files to tiff/jpeg w/ target folder (convert, not mogrify)
##  0.71    - new: added option to copy jpeg files, too at the create_jpeg case...
##  0.72    - fix: only check for mail program, when mail notification is activated
##  0.73    - write_tiffheader and convert_jpeg: skip files if compression is Old-style JPEG
##  0.74    - new: compress_jpeg_if_uncompressed
##
#####  VERSION = 0.74
#####
####################


##### TODO
# - realize check for valid int with a regex and remove checks like if [ "$foo" -gt 0 ] . it works but is crap if $foo is not an int
# - check possibillity to scale images in 6000x4000px box to create smaler derivatives for the intranda viewer and enhance performance
#   for the display. convert image.tif -scale 6000x4000 newimage.tif or convert image.tif -thumbnail 6000x4000 newimage.tif
# - just to note, ddjvu --format=tiff -eachpage input.djvu %d.tif converts a multipage djvu file into single tiff images
# - check if tomcat user and group exist...
# - write error stream into goobi process log using addToProcessLog WebAPI command
# - error handling for rename, which always exits with 0
#####


##### CHANGE PARAMETERS AS NEEDED #####

## specify tomcat user and group
TOMCATUSER=tomcat
TOMCATGROUP=$TOMCATUSER

## how many processes shall run at the same time
MAXPROCS=2

##jpeg compression level used at convert_jpeg_quality
JPEG_QUALITY=92

## new dpi when resizing and compressing
DPINEW=200

## specify some tiff header values
#TIFFSET_MAKE="HERSTELLER DER BILDER"
TIFFSET_SOFTWARE="(c) 2010-2014 intranda GmbH"

## send email after successfull step compress_jpeg
SEND_EMAIL_AFTER_COMPRESS=false
SEND_EMAIL_ADDRESS_SUCCESS="jan.vonde@intranda.com"
SEND_EMAIL_ADDRESS_FAILURE="jan.vonde@intranda.com"
SEND_EMAIL_HEADER_SUCCESS="SUCCESS: compression - "
SEND_EMAIL_HEADER_FAILURE="FAILURE: compression - "


## path to MrSID installation directory
SIDPATH="/opt/GeoExpressCLUtils-8.0.0.3065/"

## path to pbm_findskew binary
PBMFINDSKEW="/opt/digiverso/goobi/scripts/pbm_findskew"

## set to 0 if you want the script to be silent
VERBOSE=1

## set to 1 to activate the usage of nice
NICEENABLED=1
## set nice level from -20 to 19 (default = 10)
NICELEVEL=10

## set to 0 if you would like to hide warnings
SHOWWARNINGS=0

## set to 1 if you would like to use graphicsmagick instead of imagemagick
USEGM=1

## if generate_watermark parameter is used, this image will be rendered as watermark in the original image
WATERMARKIMAGEFILE=/home/jan/Dokumente/intranda/adminscripts/scripts/ubhu.png

## if set to DISABLED you can not use the cut_pixel_bottom function. This is the default
## value. If set to any integer, this is the amount of pixel that will be cut.
CUTPIXELAMOUNT="DISABLED"

## Box size in px for thumbnails. If you end up with a > then smaller images are _not_ upscaled.
## The quotes are important, otherwise the 0> will be interpreted as new line...
THUMBNAILBOXSIZE="150x150>"


## set to true if the files are stored on a CIFS share
CIFS="false"

## replace spaces with underscores
REPLACEWHITESPACES="true"

## replace all non [:alnum:. -] with underscores (not including spaces)
REPLACENONWORDCHARS="true"

## copies jpg files during the create_jpeg call to the derivatives folder. Set to 1 to enable. Default is 0
COPYJPEGFILESDURINGCREATEJPEG=0


##### DO NOT CHANGE BELOW THIS #####
if [ ${VERBOSE} == "1" ]; then
	echo "   ++++++++++ intranda image improve ++++++++++ "
fi



## error handling
type -P mogrify &>/dev/null || { echo "ERROR: imagemagick is required but seems not to be installed.  Aborting." >&2; exit 1; }
type -P convert &>/dev/null || { echo "ERROR: imagemagick is required but seems not to be installed.  Aborting." >&2; exit 1; }
type -P identify &>/dev/null || { echo "ERROR: imagemagick is required but seems not to be installed.  Aborting." >&2; exit 1; }
type -P composite &>/dev/null || { echo "ERROR: imagemagick is required but seems not to be installed.  Aborting." >&2; exit 1; }
type -P tiffset &>/dev/null || { echo "ERROR: the package libtiff-tools (ubuntu) or tiff (sles) is required but seems not to be installed.  Aborting." >&2; exit 1; }
type -P gm &>/dev/null || { echo "ERROR: the package graphicsmagick is required but seems not to be installed.  Aborting." >&2; exit 1; }
type -P exiv2 &>/dev/null || { echo "ERROR: the package exiv2 is required but seems not to be installed.  Aborting." >&2; exit 1; }


## function to compare version strings
vercomp () {
	if [[ $1 == $2 ]]; then
		return 0
	fi
	local IFS=.
	local i ver1=($1) ver2=($2)
	# fill empty fields in ver1 with zeros
	for ((i=${#ver1[@]}; i<${#ver2[@]}; i++)); do
		ver1[i]=0
	done
	for ((i=0; i<${#ver1[@]}; i++)); do
		if [[ -z ${ver2[i]} ]]; then
			# fill empty fields in ver2 with zeros
			ver2[i]=0
		fi
		if ((10#${ver1[i]} > 10#${ver2[i]})); then
			return 1
		fi
		if ((10#${ver1[i]} < 10#${ver2[i]})); then
			return 2
		fi
	done
	return 0
}

IMAGEMAGICVERSIONCURRENT="$(convert -version | grep Version | cut -d" " -f 3)";
IMAGEMAGICVERSIONNEEDED="6.4.7-10"
vercomp $IMAGEMAGICVERSIONCURRENT $IMAGEMAGICVERSIONNEEDED
RETURN=$?
if ([ "$RETURN" == "0" ] || [ "$RETURN" == "1" ]); then
	echo "";
else
	echo
	echo "WARNING: imagemagick version is too old, You can not use the create_tile_tiff option.";
fi
if [ "${USEGM}" == "1" ]; then type -P gm &>/dev/null || { echo "ERROR: graphicsmagick was configured but seems not to be installed.  Aborting." >&2; exit 1; }; fi
type -P iconv &>/dev/null || { echo "ERROR: can't find iconv. take a look where you can get it...  Aborting." >&2; exit 1; }
if [ "${SEND_EMAIL_AFTER_COMPRESS}" == "true" ]; then
  type -P mail &>/dev/null || { echo "ERROR: can't find mail. Please install and configure postfix as well as the mailutils package.  Aborting." >&2; exit 1; }
fi
type -P ddjvu &>/dev/null || { if [ "${SHOWWARNINGS}" == "1" ]; then echo "WARNING: can't find ddjvu. For djvu conversion please install the djvulibre-bin package." >&1; fi }
type -P nice &>/dev/null || { if [ "${SHOWWARNINGS}" == "1" ]; then echo "WARNING: can't find nice. If you would like to run processes with a nice value then install nice ;-)." >&1; fi }
type -P bc &>/dev/null || { echo "ERROR: bc is required but seems not to be installed.  Aborting." >&2; exit 1; }
type -P pnmcrop &>/dev/null || { echo "ERROR: netpbm is required but seems not to be installed.  Aborting." >&2; exit 1; }
type -P rename &>/dev/null || { echo "ERROR: rename is required but seems not to be installed.  Aborting." >&2; exit 1; }
type -P ${PBMFINDSKEW}  &>/dev/null || { if [ "${SHOWWARNINGS}" == "1" ]; then echo "WARNING: can't find pbm_findskew. Please download it from http://sourceforge.net/projects/pagetools/. You can not use the straighten option." >&1; fi }
type -P ${SIDPATH}/bin/mrsidgeodecode &>/dev/null || { if [ "${SHOWWARNINGS}" == "1" ]; then echo "WARNING: can't find mrsidgeodecode. Please download it from http://www.lizardtech.com/downloads/tools.php." >&1; fi }
if [ "$#" -lt "2" ]; then echo -e "ERROR: You need to give two parameter, first option and second a path. Aborting." >&2; exit 1; fi
type -P tr &>/dev/null || { echo "ERROR: tr is required but seems not to be installed.  Aborting." >&2; exit 1; }
if [ "${CUTPIXELAMOUNT}" != "DISABLED" ]; then
	if [ ! "${CUTPIXELAMOUNT}" -gt 0 ]; then echo "ERROR: the variable CUTPIXELAMOUNT was set but is not a valid pixel number. Aborting." >&2; fi
	type -P tee &>/dev/null || { echo "ERROR: tee is required but seems not to be installed.  Aborting." >&2; exit 1; }

	if [ "$#" == "3" ]; then
		if [ "$3" -gt 0 ]; then
			echo "INFO: Found valid number as third parameter to this script. Using this as CUTPIXELAMOUNT.";
			CUTPIXELAMOUNT=$3
		fi
	fi

	if [ "${USEGM}" != "1" ]; then
		if [ "${SHOWWARNINGS}" == "1" ]; then
			echo "WARNING: You are not using cut_pixel_bottom with graphicsmagick. This may result in invalid colors."
		fi
	fi
fi

## usually the second parameter is a folder, except if only a single file wants to be tiled. therefore we need to check
## here if the create_tile_tiff option was used together with a single file only

TILETIFFSINGLEIMAGE=0
TILETIFFSINGLEIMAGENAME=0
ROTATEDEGREE=0
if [ ! -d "$2" ]; then
	if [ "$1" == "create_tile_tiff" ]; then
		if [ -f "$2" ]; then
			echo "INFO: Found file instead of directory together with create_tile_tiff option. Tiling only this single image.";
			TILETIFFSINGLEIMAGE=1
		else
			echo -e "ERROR: Unable to determine what you want to do with -> $2 <- and create_tile_tiff. It's not a file! Aborting." >&2; exit 1;
		fi
	else
		echo -e "ERROR: The specified directory -> $2 <- does not exist.  Aborting." >&2; exit 1;
	fi
fi


if [ "$1" == "create_tile_tiff" ]; then
	if [ "$#" == "3" ]; then
		if ( [ "$3" == "90" ] || [ "$3" == "180" ] || [ "$3" == "270" ] ); then
			echo "INFO: Found valid number as third parameter to this script. Using this as degree for clockwise rotation at create_tile_tiff option..";
			ROTATEDEGREE=$3
		else
			echo "ERROR: Found third parameter together with create_tile_tiff option, but it is not 90, 180 or 270 but $3. Aborting." >&2; exit 1;
		fi
	fi

	if [ "${USEGM}" != "1" ]; then
		if [ "${SHOWWARNINGS}" == "1" ]; then
			echo "WARNING: You are not using create_tile_tiff with graphicsmagick. This may result in invalid files."
		fi
	fi
fi




## check if workingpath is a directory. if create_tile_tiff option is used it may be a file as well, we need to
## make sure, that we cut of the filename if that's the case
if [ -d "$2" ]; then
	WORKINGPATH=$2
else
	WORKINGPATH=$(/usr/bin/dirname $2)
	TILETIFFSINGLEIMAGENAME=$(/usr/bin/basename $2)
fi

## variable declaration
ACTION=$1
WORKINGFOLDER=$(/usr/bin/basename "${WORKINGPATH}")
SKIPSTEP=0


function prepare() {
	## change rights
	if [ ${VERBOSE} == "1" ]; then echo "Setting right ownership of files"; fi
	/usr/bin/sudo /bin/chown -R $TOMCATUSER:$TOMCATGROUP "${WORKINGPATH}"
	if [ "$?" != "0" ]; then echo -e "ERROR: an error occured during preparation. Aborting!" >&2; exit 1; fi


	## change directory
	cd "${WORKINGPATH}" || { echo "ERROR: could not cd to ${WORKINGPATH}. Aborting!" >&2; exit 1; }


	## determine if WORKINGPATH is on CIFS
	df -t cifs "${WORKINGPATH}" >/dev/null 2>&1 && CIFS="true"
	if ([ ${VERBOSE} == "1" ] && [ $CIFS == "true" ]); then
		echo "Mounted on CIFS"
	else
		echo "Not mounted on CIFS"
	fi

	## rename all file extensions to lower case, also tiff -> tif and jpeg -> jpg
	if [ ${VERBOSE} == "1" ]; then echo "Renaming everything to small letters"; fi

	if [ $CIFS == "true" ]
	then
		rename -f 's/(.*)\.(.*)/$1\.\L$2-renaming/' * || { echo "ERROR: could not rename file extensions to lower case. Aborting!" >&2; exit 1; }
		if ls *.* | grep -E ".*\.tiff-renaming$" -q; then
			rename -f 's/(.*)\.(tiff-renaming$)/$1\.tif-renaming/' *.tiff-renaming || { echo "ERROR: could not rename file extension tiff to tif. Aborting!" >&2; exit 1; }
		fi
		if ls *.* | grep -E ".*\.jpeg-renaming$" -q; then
			rename -f 's/(.*)\.(jpeg-renaming$)/$1\.jpg-renaming/' *.jpeg-renaming || { echo "ERROR: could not rename file extension jpeg to jpg. Aborting!" >&2; exit 1; }
		fi
		rename -f 's/-renaming//' * || { echo "ERROR: could not rename file extensions to lower case. Aborting!" >&2; exit 1; }
	else
		rename 's/(.*)\.(.*)/$1\.\L$2/' * || { echo "ERROR: could not rename file extensions to lower case. Aborting!" >&2; exit 1; }
		if ls *.* | grep -E ".*\.tiff$" -q; then
			rename 's/(.*)\.(tiff$)/$1\.tif/' *.tiff || { echo "ERROR: could not rename file extension tiff to tif. Aborting!" >&2; exit 1; }
		fi
		if ls *.* | grep -E ".*\.jpeg$" -q; then
			rename 's/(.*)\.(jpeg$)/$1\.jpg/' *.jpeg || { echo "ERROR: could not rename file extension jpeg to jpg. Aborting!" >&2; exit 1; }
		fi
	fi
	if ls *.* | grep -E ".*\.([a-z]*[A-Z]+[a-z]*)+$|.*\.tiff$|.*\.jpeg$" -q; then
		echo "ERROR: could not rename file extensions. Aborting!" >&2; exit 1;
	fi

	## replace spaces with underscores in filenames
	if [ "$REPLACEWHITESPACES" == "true" ]; then
		echo "Replacing whitespaces in filenames with underscores";
		find . -type f -name "* *" | rename 's/ /_/g'
		if [ "$?" != "0" ]; then echo -e "ERROR: an error occured during preparation. Aborting!" >&2; exit 1; fi
	fi

	## replace non-word and non "-" characters with underscores in filenames
	if [ "$REPLACENONWORDCHARS" == "true" ]; then
		echo "Replacing non-word and non "-" characters in filenames with underscores";
		rename -E 's/[^[:alnum:]. -]/_/g' *
		if [ "$?" != "0" ]; then echo -e "ERROR: an error occured during preparation. Aborting!" >&2; exit 1; fi
	fi


	tiffiles=$(ls *.tif 2> /dev/null | wc -l)
	jpgfiles=$(ls *.jpg 2> /dev/null | wc -l)
	jp2files=$(ls *.jp2 2> /dev/null | wc -l)
}


case "$ACTION" in
	## Resize and compress all TIFF files to a new DPI value
	compress_images)
		prepare
		if [ ${VERBOSE} == "1" ]; then echo "Resizing and compressing all *.tif images."; fi

		## if file extension exists
		if [ "${tiffiles}" != "0" ] ; then
			for i in *.tif; do
				## if picture is not bitonal compress
				if [ "$(tiffinfo ${i} 2>&1 | grep Bits | awk {'print $2'})" != "1" ]; then
					## GET NEEDED PARAMETERS
					WIDTH=$(identify -format %w ${i} 2>&1 | tail -n 1)
					HEIGHT=$(identify -format %h ${i} 2>&1 | tail -n 1)
					DPI=$(tiffinfo ${i} 2>&1 | grep "Resolution" | awk {'print $2'} | sed 's/,//g')

					## calculate exactly
					WIDTHHUNDRED=$(echo "scale=3; $WIDTH / $DPI" | bc -l)
					HEIGHTHUNDRED=$(echo "scale=3; $HEIGHT / $DPI" | bc -l)

					## and round back :P
					float=$(echo "$WIDTHHUNDRED * $DPINEW" | bc -l)
					WIDTHNEW=$(echo "($float)/1" | bc)

					float=$(echo "$HEIGHTHUNDRED * $DPINEW" | bc -l)
					HEIGHTNEW=$(echo "($float)/1" | bc)

					# remove embedded color profiles and other stuff from image -> mogrify
					if [ "${NICEENABLED}" == "1" ]; then
						if [ "${USEGM}" == "1" ]; then
							nice -n ${NICELEVEL} gm convert "${i}" -strip -resize $(echo $WIDTHNEW)x$(echo $HEIGHTNEW) -density $DPINEW -compress LZW "${i}"
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in compress_image. Aborting!" >&2; exit 1; fi
						else
							nice -n ${NICELEVEL} convert "${i}" -quiet -strip -resize $(echo $WIDTHNEW)x$(echo $HEIGHTNEW) -density $DPINEW -compress LZW "${i}"
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in compress_image. Aborting!" >&2; exit 1; fi
						fi
					else
						if [ "${USEGM}" == "1" ]; then
							gm convert "${i}" -strip -resize $(echo $WIDTHNEW)x$(echo $HEIGHTNEW) -density $DPINEW -compress LZW "${i}"
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in compress_image. Aborting!" >&2; exit 1; fi
						else
							convert "${i}" -quiet -strip -resize $(echo $WIDTHNEW)x$(echo $HEIGHTNEW) -density $DPINEW -compress LZW "${i}"
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in compress_image. Aborting!" >&2; exit 1; fi
						fi
					fi
				fi
			done
		fi

		## send email after success or fail
		if [ "${SEND_EMAIL_AFTER_COMPRESS}" == "true" ]; then
			if [ "$(ls | wc -w)" == "$(ls ../orig_${WORKINGFOLDER} | wc -w)" ]; then
				echo "SUCCESS: conversion finished in folder: ${WORKINGFOLDER}" | mail -s "${SEND_EMAIL_HEADER_SUCCESS} ${WORKINGFOLDER}" $SEND_EMAIL_ADDRESS_SUCCESS
			fi
			if [ "$(ls | wc -w)" != "$(ls ../orig_${WORKINGFOLDER} | wc -w)" ]; then
				echo "FAILURE: conversion failed in foler: ${WORKINGFOLDER}" | mail -s "${SEND_EMAIL_HEADER_FAILURE} ${WORKINGFOLDER}" $SEND_EMAIL_ADDRESS_FAILURE
			fi
		fi
	;;


	# mogrify to tiff/jpeg, resize to $3 (percent).
	tiffjpeg_resize)
		prepare
		set -e
		[ "$#" -ne "3" ] && { echo "usage error, check number of arguments" >&2; exit 1; }
		RESIZE_PERCENT="$3"
		echo "${RESIZE_PERCENT}" | grep -E "^[0-9]+%$" --silent || { echo "usage error, \"$3\" is not a valid % expression" >&2; exit 1; }

		if [ ${VERBOSE} == "1" ]; then echo "Resizing and compressing all *.tif images."; fi
		## if file extension exists
		if [ "${tiffiles}" != "0" ] ; then
			for i in *.tif; do
				## if picture is not bitonal:
				if [ "$(tiffinfo "${i}" 2>&1 | grep Bits | awk {'print $2'})" != "1" ]; then
					DPI=$(tiffinfo "${i}" 2>&1 | grep "Resolution" | awk {'print $2'} | sed 's/,//g')
					NEWDPI=$( echo "${DPI} * ${RESIZE_PERCENT%\%}/100 + 0.5" | bc -l )
					NEWDPI=$( echo "${NEWDPI}/1" | bc )
					# remove embedded profiles # weil dpi auch in komischen photoshop-profilen stehen kann
					nice -n ${NICELEVEL} gm mogrify "${i}" +matte -depth 8 -compress jpeg -resize "${RESIZE_PERCENT}" +profile '*' -density ${NEWDPI} "${i}"
				fi
			done
		fi
		set +e
	;;


	## Create JPEG files from TIFF or JP2 files and stores them in a new folder (used at the Wellcome Trust)
	create_jpeg)
		prepare
		if [ ${VERBOSE} == "1" ]; then echo "Creating JPEG from JP2 or TIFF files"; fi

		FOLDER=$(echo ${WORKINGPATH} | sed -re 's/master_//' -e 's/_master|_media/_jpg/')

		if [ ! -d $FOLDER ] ; then
			mkdir $FOLDER;
			if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_jpeg. Aborting!" >&2; exit 1; fi
		else
			mv $FOLDER ${FOLDER}_$(date +%s)
			if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_jpeg. Aborting!" >&2; exit 1; fi
			mkdir $FOLDER
			if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_jpeg. Aborting!" >&2; exit 1; fi
		fi

		if [ "${tiffiles}" != "0" ] ; then
			echo "Converting TIFF files";
			for i in *.tif; do
	  			if [ "${NICEENABLED}" == "1" ]; then
					if [ "${USEGM}" == "1" ]; then
						nice -n ${NICELEVEL} gm convert "${i}" -strip "$FOLDER/${i%.*}.jpg";
						if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_jpeg. Aborting!" >&2; exit 1; fi
					else
						nice -n ${NICELEVEL} convert "${i}" -strip -quiet "$FOLDER/${i%.*}.jpg";
						if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_jpeg. Aborting!" >&2; exit 1; fi
					fi
				else
					if [ "${USEGM}" == "1" ]; then
						gm convert "${i}" -strip "$FOLDER/${i%.*}.jpg";
						if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_jpeg. Aborting!" >&2; exit 1; fi
					else
						convert "${i}" -strip -quiet "$FOLDER/${i%.*}.jpg";
						if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_jpeg. Aborting!" >&2; exit 1; fi
					fi
				fi
			done
		fi

		if [ "$jp2files" != "0" ] ; then
			echo "Converting JPEG2000 files";
			for i in *.jp2; do
		  		if [ "${NICEENABLED}" == "1" ]; then
					if [ "${USEGM}" == "1" ]; then
						nice -n ${NICELEVEL} gm convert "${i}" -strip "$FOLDER/${i%.*}.jpg" 2>&1;
						if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_jpeg. Aborting!" >&2; exit 1; fi
					else
						nice -n ${NICELEVEL} convert "${i}" -strip -quiet "$FOLDER/${i%.*}.jpg" 2>&1;
						if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_jpeg. Aborting!" >&2; exit 1; fi
					fi
				else
					if [ "${USEGM}" == "1" ]; then
						gm convert "${i}" -strip "$FOLDER/${i%.*}.jpg" 2>&1;
						if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_jpeg. Aborting!" >&2; exit 1; fi
					else
						convert "${i}" -strip -quiet "$FOLDER/${i%.*}.jpg" 2>&1;
						if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_jpeg. Aborting!" >&2; exit 1; fi
					fi
				fi
			done
		fi

                if [ "${jpgfiles}" != "0" ] ; then
                        if [ "${COPYJPEGFILESDURINGCREATEJPEG}" == "1" ]; then
                                echo "Duplicating JPEG files";
                                for i in *.jpg; do
                                        cp "${i}" "$FOLDER/${i}";
                                done
                        fi
                fi
	;;


	## Create JPEG files from TIFF files and stores them in a new folder (used at Genus)
	create_jpeg_size)
		prepare
		if [ ${VERBOSE} == "1" ]; then echo "Creating JPEG from TIFF files"; fi
		if [ "$#" -lt "3" ]; then echo "Wrong number of arguments, expecting 3, got $#."; exit 1; fi

		FOLDER=$(echo ${WORKINGPATH} | sed -re 's/master_//' -e 's/_master|_media/_jpg/')

		if [ ! -d $FOLDER ] ; then
			mkdir $FOLDER;
			if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_jpeg. Aborting!" >&2; exit 1; fi
		else
			mv $FOLDER ${FOLDER}_$(date +%s)
			if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_jpeg. Aborting!" >&2; exit 1; fi
			mkdir $FOLDER
			if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_jpeg. Aborting!" >&2; exit 1; fi
		fi

		if [ "${tiffiles}" != "0" ] ; then
			echo "Converting TIFF files";
			for i in *.tif; do
				nice -n ${NICELEVEL} convert "${i}" -strip -quiet -define jpeg:extent=${3}kb "$FOLDER/${i%.*}.jpg";
				if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_jpeg. Aborting!" >&2; exit 1; fi
			done
		fi
	;;


	## Create JPEG files (write to $3) from TIFF files (read from $2, see prepare)
	tif2jpg)
		prepare
		set -e
		if [ "${VERBOSE}" == "1" ]; then echo "Creating JPEG from TIFF files"; fi
		if [ "$#" -ne "3" ]; then echo "Wrong number of arguments, expecting 3, got $#."; exit 1; fi

		FOLDER="$3"
		mkdir -p "$FOLDER";

		if [ "${tiffiles}" != "0" ] ; then
			for i in *.tif; do
				nice -n ${NICELEVEL} gm convert "${i}" -depth 8 +matte -colorspace RGB "$FOLDER/${i%.*}.jpg";
			done
		fi
		set +e
	;;

	## Apply -fx ($3) to TIFF files (mogrify)
	mogrify-fx)
		prepare
		set -e
		if [ "${VERBOSE}" == "1" ]; then echo "Mogrify TIFF files, applying fx: $3"; fi
		if [ "$#" -ne "3" ]; then echo "Wrong number of arguments, expecting 3, got $#."; exit 1; fi

		if [ "${tiffiles}" != "0" ] ; then
			for i in *.tif; do
				nice -n ${NICELEVEL} mogrify -quiet -depth 8 -alpha off -compress JPEG -quality 95 -fx "$3" "$i";
			done
		fi
		set +e
	;;


	## Converting TIF or JPEG files to TIFF/JPEG
	convert_jpeg)
		prepare
		if [ ${VERBOSE} == "1" ]; then echo "Converting all *.tif or *.jpg files to TIFF/JPEG"; fi

		## if files are tiff files
		if [ "${tiffiles}" != "0" ] ; then
			for i in *.tif; do
				## if picture is not bitonal compress
				if [ "$(tiffinfo ${i} 2>&1 | grep Bits | awk {'print $2'})" != "1" ]; then
					if [ "${NICEENABLED}" == "1" ]; then
						if [ "${USEGM}" == "1" ]; then
							tiffinfo "${i}" 2>/dev/null | grep 'Compression Scheme: Old-style JPEG' -q && { echo "WARNING: ${i} is compressed Old-style JPEG, skipping convert_jpeg."; continue; }
							nice -n ${NICELEVEL} gm mogrify -depth 8 +matte -colorspace RGB -compress JPEG "${i}"
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_jpeg. Aborting!" >&2; exit 1; fi
						else
							tiffinfo "${i}" 2>/dev/null | grep 'Compression Scheme: Old-style JPEG' -q && { echo "WARNING: ${i} is compressed Old-style JPEG, skipping convert_jpeg."; continue; }
							nice -n ${NICELEVEL} mogrify -quiet -depth 8 -alpha off -compress JPEG "${i}"
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_jpeg. Aborting!" >&2; exit 1; fi
						fi
					else
						if [ "${USEGM}" == "1" ]; then
							tiffinfo "${i}" 2>/dev/null | grep 'Compression Scheme: Old-style JPEG' -q && { echo "WARNING: ${i} is compressed Old-style JPEG, skipping convert_jpeg."; continue; }
							gm mogrify -depth 8 +matte -colorspace RGB -compress JPEG "${i}"
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_jpeg. Aborting!" >&2; exit 1; fi
						else
							tiffinfo "${i}" 2>/dev/null | grep 'Compression Scheme: Old-style JPEG' -q && { echo "WARNING: ${i} is compressed Old-style JPEG, skipping convert_jpeg."; continue; }
							mogrify -quiet -depth 8 -alpha off -compress JPEG "${i}"
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_jpeg. Aborting!" >&2; exit 1; fi
						fi
					fi
				fi
			done
		fi

		## if files are jpg files
		if [ "$jpgfiles" != "0" ] ; then
			for i in *.jpg; do
				if [ "${NICEENABLED}" == "1" ]; then
					if [ "${USEGM}" == "1" ]; then
						nice -n ${NICELEVEL} gm convert "${i}" -depth 8 +matte -compress JPEG "${i%.*}.tif"
						if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_jpeg. Aborting!" >&2; exit 1; fi
					else
						nice -n ${NICELEVEL} convert "${i}" -quiet -depth 8 -alpha off -compress JPEG "${i%.*}.tif"
						if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_jpeg. Aborting!" >&2; exit 1; fi
					fi
				else
					if [ "${USEGM}" == "1" ]; then
						gm convert "${i}" -depth 8 +matte -compress JPEG "${i%.*}.tif"
						if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_jpeg. Aborting!" >&2; exit 1; fi
					else
						convert "${i}" -quiet -depth 8 -alpha off -compress JPEG "${i%.*}.tif"
						if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_jpeg. Aborting!" >&2; exit 1; fi
					fi
				fi
			done
			rm *.jpg
		fi

		## send email after success or fail
		if [ "${SEND_EMAIL_AFTER_COMPRESS}" == "true" ]; then
			if [ "$(ls | wc -w)" == "$(ls ../orig_${WORKINGFOLDER} | wc -w)" ]; then
				echo "SUCCESS: conversion finished in folder: ${WORKINGFOLDER}" | mail -s "${SEND_EMAIL_HEADER_SUCCESS} ${WORKINGFOLDER}" ${SEND_EMAIL_ADDRESS_SUCCESS}
			fi
			if [ "$(ls | wc -w)" != "$(ls ../orig_${WORKINGFOLDER} | wc -w)" ]; then
				echo "FAILURE: conversion failed in foler: ${WORKINGFOLDER}" | mail -s "${SEND_EMAIL_HEADER_FAILURE} ${WORKINGFOLDER}" ${SEND_EMAIL_ADDRESS_FAILURE}
			fi
		fi
	;;

	## converting TIF or JPEG files in $2 to new TIFF/JPEG files in $3 (if bitonal TIFF just copy)
	create_tiffjpeg)
		prepare
		set -e
		mkdir -p "$3"
		if [ ${VERBOSE} == "1" ]; then echo "Converting all *.tif or *.jpg files to TIFF/JPEG"; fi
		## if files are tiff files
		if [ "${tiffiles}" != "0" ] ; then
			for i in *.tif; do
				## if picture is not bitonal compress
				if [ "$(tiffinfo ${i} 2>&1 | grep Bits | awk {'print $2'})" != "1" ]; then
					if [ "${USEGM}" == "1" ]; then
						nice -n ${NICELEVEL} gm convert "${i}" -depth 8 +matte -colorspace RGB -compress JPEG "$3/${i}"
					else
						nice -n ${NICELEVEL} convert "${i}" -quiet -depth 8 -alpha off -compress JPEG "$3/${i}"
					fi
				else
					cp "${i}" "$3/${i}"
				fi
			done
		fi
		## if files are jpg files
		if [ "$jpgfiles" != "0" ] ; then
			for i in *.jpg; do
				if [ "${USEGM}" == "1" ]; then
					nice -n ${NICELEVEL} gm convert "${i}" -depth 8 +matte -compress JPEG "$3/${i%.*}.tif"
				else
					nice -n ${NICELEVEL} convert "${i}" -quiet -depth 8 -alpha off -compress JPEG "$3/${i%.*}.tif"
				fi
			done
		fi
		set +e
	;;

	## Converting TIF or JPEG files to TIFF/JPEG, applying embedded ICC profiles to sRGB, remove profile
	convert_jpeg_rm_icc)
		prepare
		set -e
		cat /opt/digiverso/goobi/scripts/sRGB.icc >/dev/null
		SRGB="/opt/digiverso/goobi/scripts/sRGB.icc"
		## if files are tiff files
		if [ "${tiffiles}" != "0" ] ; then
			for i in *.tif; do
				## if picture is not bitonal compress
				if [ "$(tiffinfo ${i} 2>&1 | grep Bits | awk {'print $2'})" != "1" ]; then
					if [ $(exiftool -icc_profile:'*' "$i" | wc -l) -gt "0" ]; then
						nice -n ${NICELEVEL} gm mogrify -depth 8 +matte -colorspace RGB -profile "$SRGB" +profile '*' -compress JPEG "${i}"
					else
						nice -n ${NICELEVEL} gm mogrify -depth 8 +matte -colorspace RGB -compress JPEG "${i}"
					fi
				fi
			done
		fi

		## if files are jpg files
		if [ "$jpgfiles" != "0" ] ; then
			for i in *.jpg; do
				if [ $(exiftool -icc_profile:'*' "$i" | wc -l) -gt "0" ]; then
					nice -n ${NICELEVEL} gm convert "${i}" -depth 8 +matte -colorspace RGB -profile "$SRGB" +profile '*' -compress JPEG "${i%.*}.tif"
				else
					nice -n ${NICELEVEL} gm convert "${i}" -depth 8 +matte -colorspace RGB -compress JPEG "${i%.*}.tif"
				fi
			done
			rm *.jpg
		fi
		set +e
	;;


	## Strip all information from the TIFF header
	clean_tiffheader)
		prepare
		if [ ${VERBOSE} == "1" ]; then echo "Cleaning tiff headers"; fi

		if [ "${tiffiles}" != "0" ] ; then
			for i in *.tif; do
				if [ "${NICEENABLED}" == "1" ]; then
					if [ "${USEGM}" == "1" ]; then
						nice -n ${NICELEVEL} gm mogrify -strip "${i}"
						if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in clean_tiffheader. Aborting!" >&2; exit 1; fi
					else
						nice -n ${NICELEVEL} mogrify -quiet -strip "${i}"
						if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in clean_tiffheader. Aborting!" >&2; exit 1; fi
					fi
				else
					if [ "${USEGM}" == "1" ]; then
						gm mogrify -strip "${i}"
						if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in clean_tiffheader. Aborting!" >&2; exit 1; fi
					else
						mogrify -quiet -strip "${i}"
						if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in clean_tiffheader. Aborting!" >&2; exit 1; fi
					fi
				fi
			done
		fi
	;;



	## Convert images to TIFF/JPEG based on file extension (used at Greifswald)
	convert_images)
		prepare
		if [ ${VERBOSE} == "1" ]; then echo "Converting images to TIFF/JPEG based on file extension"; fi

		## get all available fileextensions in this folder
		FILEEXTENSIONS=$(for i in *; do echo ${i#*.}; done | sort | uniq)
		if [ ${VERBOSE} == "1" ]; then echo "Available fileextensions: ${FILEEXTENSIONS}"; fi

		for i in ${FILEEXTENSIONS}; do
			case "${i}" in
				tif)
					if [ ${VERBOSE} == "1" ]; then echo "tif, do nothing"; fi
				;;

				png)
					if [ ${VERBOSE} == "1" ]; then echo "png -> convert to tif"; fi
					mkdir -p ../orig_png;
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
					if [ "${NICEENABLED}" == "1" ]; then
						if [ "${USEGM}" == "1" ]; then
							for i in *.png
							do
								nice -n ${NICELEVEL} gm convert "${i}" -strip +matte -compress JPEG ${i/png/tif}
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
								mv "${i}" ../orig_png/
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
							done
						else
							for i in *.png
							do
								nice -n ${NICELEVEL} convert "${i}" -quiet -alpha off -compress JPEG -strip ${i/png/tif}
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
								mv "${i}" ../orig_png/
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
							done
						fi
					else
						if [ "${USEGM}" == "1" ]; then
							for i in *.png
							do
								gm convert "${i}" -strip +matte -compress JPEG ${i/png/tif}
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
								mv "${i}" ../orig_png/
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
							done
						else
							for i in *.png
							do
								convert "${i}" -quiet -alpha off -compress JPEG -strip ${i/png/tif}
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
								mv "${i}" ../orig_png/
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
							done
						fi
					fi
				;;

				jpg)
					if [ ${VERBOSE} == "1" ]; then echo "jpg -> convert to tif"; fi
					mkdir -p ../orig_jpg;
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
					if [ "${NICEENABLED}" == "1" ]; then
						if [ "${USEGM}" == "1" ]; then
							for i in *.jpg
							do
								nice -n ${NICELEVEL} gm convert "${i}" -strip +matte -compress JPEG ${i/jpg/tif}
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
								mv "${i}" ../orig_jpg/
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
							done
						else
							for i in *.jpg
							do
								nice -n ${NICELEVEL} convert "${i}" -quiet -alpha off -compress JPEG -strip ${i/jpg/tif}
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
								mv "${i}" ../orig_jpg/
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
							done
						fi
					else
						if [ "${USEGM}" == "1" ]; then
							for i in *.jpg
							do
								gm convert "${i}" -strip +matte -compress JPEG ${i/jpg/tif}
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
								mv "${i}" ../orig_jpg/
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
							done
						else
							for i in *.jpg
							do
								convert "${i}" -quiet -alpha off -compress JPEG -strip ${i/jpg/tif}
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
								mv "${i}" ../orig_jpg/
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
							done
						fi
					fi
				;;

				djvu)
					if [ ${VERBOSE} == "1" ]; then echo "djvu -> convert to tif"; fi
					type -P ddjvu &>/dev/null || { echo "ERROR: cant find ddjvu. For djvu conversion please install the djvulibre-bin package. Aborting." >&2; exit 1; }
					mkdir -p ../orig_djvu
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
					if [ "${NICEENABLED}" == "1" ]; then
						if [ "${USEGM}" == "1" ]; then
							#for i in *.djvu; do nice -n ${NICELEVEL} ddjvu -format=tiff ${i} ${i/djvu/tif}; mv ${i} ../orig_djvu/; done
							find *.djvu -print0 | xargs -0 -I {} -P ${MAXPROCS} nice -n ${NICELEVEL} ddjvu -format=tiff {} {}.tif
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
							mv *.djvu ../orig_djvu/
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
							for i in *.djvu.tif
							do
								mv "${i}" "${i/.djvu.tif/.tif}"
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
							done
							find *.tif -print0 | xargs -0 -I {} -P ${MAXPROCS} nice -n ${NICELEVEL} gm mogrify +matte -compress JPEG {}
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
						else
							#for i in *.djvu; do nice -n ${NICELEVEL} ddjvu -format=tiff ${i} ${i/djvu/tif}; mv ${i} ../orig_djvu/; done
							find *.djvu -print0 | xargs -0 -I {} -P ${MAXPROCS} nice -n ${NICELEVEL} ddjvu -format=tiff {} {}.tif
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
							mv *.djvu ../orig_djvu/
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
							for i in *.djvu.tif
							do
								mv "${i}" "${i/.djvu.tif/.tif}"
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
							done

							find *.tif -print0 | xargs -0 -I {} -P ${MAXPROCS} nice -n ${NICELEVEL} mogrify -quiet -alpha off -compress JPEG {}
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
						fi
					else
						if [ "${USEGM}" == "1" ]; then
							#for i in *.djvu; do ddjvu -format=tiff ${i} ${i/djvu/tif}; mv ${i} ../orig_djvu/; done
							find *.djvu -print0 | xargs -0 -I {} -P ${MAXPROCS} ddjvu -format=tiff {} {}.tif
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
							mv *.djvu ../orig_djvu/
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
							for i in *.djvu.tif
							do
								mv "${i}" "${i/.djvu.tif/.tif}"
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
							done

							find *.tif -print0 | xargs -0 -I {} -P ${MAXPROCS} gm mogrify +matte -compress JPEG {}
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
						else
							#for i in *.djvu; do ddjvu -format=tiff ${i} ${i/djvu/tif}; mv ${i} ../orig_djvu/; done
							find *.djvu -print0 | xargs -0 -I {} -P ${MAXPROCS} ddjvu -format=tiff {} {}.tif
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
							mv *.djvu ../orig_djvu/
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
							for i in *.djvu.tif
							do
								mv "${i}" "${i/.djvu.tif/.tif}"
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
							done

							find *.tif -print0 | xargs -0 -I {} -P ${MAXPROCS} mogrify -quiet -alpha off -compress JPEG {}
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
						fi
					fi
				;;

				sid)
					if [ ${VERBOSE} == "1" ]; then echo "mrsid -> convert to tif"; fi
					type -P ${SIDPATH}/bin/mrsidgeodecode &>/dev/null || { echo "ERROR: can't find mrsidgeodecode. Please download it from http://www.lizardtech.com/downloads/tools.php.Aborting." >&2; exit 1; }
					mkdir -p ../orig_sid
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
					if [ "${NICEENABLED}" == "1" ]; then
						if [ "${USEGM}" == "1" ]; then
							for i in *.sid
							do
								LD_LIBRARY_PATH="${SIDPATH}/bin/" nice -n ${NICELEVEL} ${SIDPATH}/bin/mrsidgeodecode -i "${i}" -o  ${i/sid/tif} -of tif -quiet; mv "${i}" ../orig_sid/
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
							done
							find *.tif -print0 | xargs -0 -I {} -P ${MAXPROCS} nice -n ${NICELEVEL} gm mogrify +matte -compress JPEG {}
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
						else
							for i in *.sid
							do
								LD_LIBRARY_PATH="${SIDPATH}/bin/" nice -n ${NICELEVEL} ${SIDPATH}/bin/mrsidgeodecode -i "${i}" -o  ${i/sid/tif} -of tif -quiet
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
								mv "${i}" ../orig_sid/
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
							done
							find *.tif -print0 | xargs -0 -I {} -P ${MAXPROCS} nice -n ${NICELEVEL} mogrify -quiet -alpha off -compress JPEG {}
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
						fi
					else
						if [ "${USEGM}" == "1" ]; then
							for i in *.sid
							do
								LD_LIBRARY_PATH="${SIDPATH}/bin/" ${SIDPATH}/bin/mrsidgeodecode -i "${i}" -o  ${i/sid/tif} -of tif -quiet
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
								mv "${i}" ../orig_sid/
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
							done
							find *.tif -print0 | xargs -0 -I {} -P ${MAXPROCS} gm mogrify +matte -compress JPEG {}
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
						else
							for i in *.sid
							do
								LD_LIBRARY_PATH="${SIDPATH}/bin/" ${SIDPATH}/bin/mrsidgeodecode -i "${i}" -o  ${i/sid/tif} -of tif -quiet
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
								mv "${i}" ../orig_sid/
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
							done
							find *.tif -print0 | xargs -0 -I {} -P ${MAXPROCS} mogrify -quiet -alpha off -compress JPEG {}
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_images. Aborting!" >&2; exit 1; fi
						fi
					fi
				;;

				*)
					if [ ${VERBOSE} == "1" ]; then echo "Nothing converted"; fi
				;;
			esac
		done
	;;


	## Convert all TIFF files to TIFF/JPEG using fixed quality value
	convert_jpeg_quality)
		prepare
		if [ ${VERBOSE} == "1" ]; then echo "Converting all *.tif files to TIFF/JPEG with compression quality ${JPEG_QUALITY}%"; fi
		if [ "${NICEENABLED}" == "1" ]; then
			if [ "${USEGM}" == "1" ]; then
				find *.tif -print0 | xargs -0 -I {} -P ${MAXPROCS} nice -n ${NICELEVEL} gm mogrify -quality ${JPEG_QUALITY} +matte -compress JPEG {}
				if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_jpeg_quality. Aborting!" >&2; exit 1; fi
			else
				find *.tif -print0 | xargs -0 -I {} -P ${MAXPROCS} nice -n ${NICELEVEL} mogrify -quiet -quality ${JPEG_QUALITY} -alpha off -compress JPEG {}
				if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_jpeg_quality. Aborting!" >&2; exit 1; fi
			fi
		else
			if [ "${USEGM}" == "1" ]; then
				find *.tif -print0 | xargs -0 -I {} -P ${MAXPROCS} gm mogrify -quality ${JPEG_QUALITY} +matte -compress JPEG {}
				if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_jpeg_quality. Aborting!" >&2; exit 1; fi
			else
				find *.tif -print0 | xargs -0 -I {} -P ${MAXPROCS} mogrify -quiet -quality ${JPEG_QUALITY} -alpha off -compress JPEG {}
				if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_jpeg_quality. Aborting!" >&2; exit 1; fi
			fi
		fi
	;;


	## Convert all TIFF files to grayscale
	convert_gray)
		prepare
		if [ ${VERBOSE} == "1" ]; then echo "Converting all *.tif files to grayscale"; fi
		if [ "${NICEENABLED}" == "1" ]; then
			if [ "${USEGM}" == "1" ]; then
				find *.tif -print0 | xargs -0 -I {} -P ${MAXPROCS} nice -n ${NICELEVEL} gm mogrify -type GRAYSCALE {}
				if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_gray. Aborting!" >&2; exit 1; fi
			else
				find *.tif -print0 | xargs -0 -I {} -P ${MAXPROCS} nice -n ${NICELEVEL} mogrify -quiet -type GRAYSCALE {}
				if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_gray. Aborting!" >&2; exit 1; fi
			fi
		else
			if [ "${USEGM}" == "1" ]; then
				find *.tif -print0 | xargs -0 -I {} -P ${MAXPROCS} gm mogrify -type GRAYSCALE {}
				if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_gray. Aborting!" >&2; exit 1; fi
			else
				find *.tif -print0 | xargs -0 -I {} -P ${MAXPROCS} mogrify -quiet -type GRAYSCALE {}
				if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_gray. Aborting!" >&2; exit 1; fi
			fi
		fi
	;;


	## Convert all TIFF files to monochrome
	convert_monochrome)
		prepare
		if [ ${VERBOSE} == "1" ]; then echo "Converting all *.tif files to monochrome"; fi
		if [ "${NICEENABLED}" == "1" ]; then
			if [ "${USEGM}" == "1" ]; then
				find *.tif -print0 | xargs -0 -I {} -P ${MAXPROCS} nice -n ${NICELEVEL} gm mogrify -compress FAX {}
				if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_monochrome. Aborting!" >&2; exit 1; fi
			else
				find *.tif -print0 | xargs -0 -I {} -P ${MAXPROCS} nice -n ${NICELEVEL} mogrify -quiet -compress FAX {}
				if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_monochrome. Aborting!" >&2; exit 1; fi
			fi
		else
			if [ "${USEGM}" == "1" ]; then
				find *.tif -print0 | xargs -0 -I {} -P ${MAXPROCS} gm mogrify -compress FAX {}
				if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_monochrome. Aborting!" >&2; exit 1; fi
			else
				find *.tif -print0 | xargs -0 -I {} -P ${MAXPROCS} mogrify -quiet -compress FAX {}
				if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_monochrome. Aborting!" >&2; exit 1; fi
			fi
		fi
	;;


	## Remove black border from all TIFF files
	remove_black_border)
		prepare
		if [ ${VERBOSE} == "1" ]; then echo "Removing black border from all *.tif files"; fi

		for i in *.tif; do
			## convert to pbm
			if [ "${NICEENABLED}" == "1" ]; then
				if [ "${USEGM}" == "1" ]; then
					nice -n ${NICELEVEL} gm convert "${i}" -strip "${i/.tif/_black.pbm}"
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_black_border. Aborting!" >&2; exit 1; fi
				else
					nice -n ${NICELEVEL} convert "${i}" -quiet -strip "${i/.tif/_black.pbm}"
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_black_border. Aborting!" >&2; exit 1; fi
				fi
			else
				if [ "${USEGM}" == "1" ]; then
					gm convert "${i}" -strip "${i/.tif/_black.pbm}"
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_black_border. Aborting!" >&2; exit 1; fi
				else
					convert "${i}" -quiet -strip "${i/.tif/_black.pbm}"
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_black_border. Aborting!" >&2; exit 1; fi
				fi
			fi

			## get crop information
			if [ "${NICEENABLED}" == "1" ]; then
				nice -n ${NICELEVEL} pnmcrop -black -verbose ${i/.tif/_black.pbm} 1> /dev/null 2> cropinfo
				if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_black_border. Aborting!" >&2; exit 1; fi
			else
				pnmcrop -black -verbose ${i/.tif/_black.pbm} 1> /dev/null 2> cropinfo
				if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_black_border. Aborting!" >&2; exit 1; fi
			fi

			## get lines for each side and turn to 0 if nothing
			if [ "$(grep top cropinfo | cut -d" " -f 3)" == "" ]; then
				TOP="0"
			else
				TOP=$(grep top cropinfo | cut -d" " -f 3)
			fi

			if [ "$(grep bottom cropinfo | cut -d" " -f 3)" == "" ]; then
				BOTTOM="0"
			else
				BOTTOM=$(grep bottom cropinfo | cut -d" " -f 3)
			fi

			if [ "$(grep left cropinfo | cut -d" " -f 3)" == "" ]; then
				LEFT="0"
			else
				LEFT=$(grep left cropinfo | cut -d" " -f 3)
			fi

			if [ "$(grep right cropinfo | cut -d" " -f 3)" == "" ]; then
				RIGHT="0"
			else
				RIGHT=$(grep right cropinfo | cut -d" " -f 3)
			fi

			if [ ${VERBOSE} == "1" ]; then echo "Cropping: - Top: $TOP; Bottom: $BOTTOM; Left: $LEFT; Right: $RIGHT"; fi

			## do the actial cropping
			if [ "${NICEENABLED}" == "1" ]; then
				if [ "${USEGM}" == "1" ]; then
					nice -n ${NICELEVEL} gm mogrify -shave "$TOP"x"$BOTTOM"x"$LEFT"x"$RIGHT" "${i}"
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_black_border. Aborting!" >&2; exit 1; fi
				else
					nice -n ${NICELEVEL} mogrify -quiet -shave "$TOP"x"$BOTTOM"x"$LEFT"x"$RIGHT" "${i}"
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_black_border. Aborting!" >&2; exit 1; fi
				fi
			else
				if [ "${USEGM}" == "1" ]; then
					gm mogrify -shave "$TOP"x"$BOTTOM"x"$LEFT"x"$RIGHT" "${i}"
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_black_border. Aborting!" >&2; exit 1; fi
				else
					mogrify -quiet -shave "$TOP"x"$BOTTOM"x"$LEFT"x"$RIGHT" "${i}"
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_black_border. Aborting!" >&2; exit 1; fi
				fi
			fi

			## clean temp files
			rm ${i/.tif/_black.pbm}    ## <- black and white pbm image
			if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_black_border. Aborting!" >&2; exit 1; fi
			rm cropinfo
			if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in convert_black_border. Aborting!" >&2; exit 1; fi
		done

		### OLD STUFF ###
		#    find *.tif -print0 | xargs -0 -I {} -P ${MAXPROCS} mogrify -quiet -fuzz 5% -trim {}
		### OLD STUFF ###
	;;


	## Deskew all TIFF files
	straighten)
		prepare
		if [ ${VERBOSE} == "1" ]; then echo "Straighten all *.tif files"; fi

		for i in *.tif; do
			## convert to pbm
			if [ "${NICEENABLED}" == "1" ]; then
				if [ "${USEGM}" == "1" ]; then
					nice -n ${NICELEVEL} gm convert "${i}" -strip "${i/.tif/_work.pbm}"
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in straighten. Aborting!" >&2; exit 1; fi
				else
					nice -n ${NICELEVEL} convert "${i}" -quiet -strip "${i/.tif/_work.pbm}"
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in straighten. Aborting!" >&2; exit 1; fi
				fi
			else
				if [ "${USEGM}" == "1" ]; then
					gm convert "${i}" -strip "${i/.tif/_work.pbm}"
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in straighten. Aborting!" >&2; exit 1; fi
				else
					convert "${i}" -quiet -strip "${i/.tif/_work.pbm}"
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in straighten. Aborting!" >&2; exit 1; fi
				fi
			fi

			## get skew
			if [ "${NICEENABLED}" == "1" ]; then
				nice -n ${NICELEVEL} $PBMFINDSKEW ${i/.tif/_work.pbm} > skew
				if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in straighten. Aborting!" >&2; exit 1; fi
			else
				${PBMFINDSKEW} ${i/.tif/_work.pbm} > skew
				if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in straighten. Aborting!" >&2; exit 1; fi
			fi

			## rotate
			if [ "${NICEENABLED}" == "1" ]; then
				if [ "${USEGM}" == "1" ]; then
					nice -n ${NICELEVEL} gm mogrify -background black -rotate "$(echo $(cat skew)*-1 | bc)" "${i}"
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in straighten. Aborting!" >&2; exit 1; fi
				else
					nice -n ${NICELEVEL} mogrify -quiet -background black -rotate "$(echo $(cat skew)*-1 | bc)" "${i}"
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in straighten. Aborting!" >&2; exit 1; fi
				fi
			else
				if [ "${USEGM}" == "1" ]; then
					gm mogrify -background black -rotate "$(echo $(cat skew)*-1 | bc)" "${i}"
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in straighten. Aborting!" >&2; exit 1; fi
				else
					mogrify -quiet -background black -rotate "$(echo $(cat skew)*-1 | bc)" "${i}"
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in straighten. Aborting!" >&2; exit 1; fi
				fi
			fi

			## clean temp files
			rm ${i/.tif/_work.pbm}	## <- black and white pbm image
			if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in straighten. Aborting!" >&2; exit 1; fi
			rm skew			## <- text file with rotation of image
			if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in straighten. Aborting!" >&2; exit 1; fi

		done

	;;


	## Set TIFF Header fiels 259 to uncompressed if not set (used in Kassel)
	correct259)
		prepare
		if [ ${VERBOSE} == "1" ]; then echo "Writing TIFF-Header field 259, setting compression to uncompressed"; fi

		if [ "${tiffiles}" != "0" ]; then
			if [ "${NICEENABLED}" == "1" ]; then
				for i in *.tif
				do
					nice -n ${NICELEVEL} tiffset -s 259 1 "${i}"
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in correct259. Aborting!" >&2; exit 1; fi
				done
			else
				for i in *.tif
				do
					tiffset -s 259 1 "${i}"
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in correct259. Aborting!" >&2; exit 1; fi
				done
			fi
		fi
	;;


	## Set the colorspace for all JPEG images to RGB
	set_jpeg_colorspace)
		prepare
		if [ ${VERBOSE} == "1" ]; then echo "Set the colorspace for all JPEG images to RGB"; fi

		if [ "${tiffiles}" != "0" ]; then
			if [ "${NICEENABLED}" == "1" ]; then
				for i in *.jpg
				do
					nice -n ${NICELEVEL} mogrify -strip -colorspace rgb "${i}"
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in set_jpeg_colorspace. Aborting!" >&2; exit 1; fi
				done
			else
				for i in *.jpg
				do
					mogrify -strip -colorspace rgb "${i}"
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in set_jpeg_colorspace. Aborting!" >&2; exit 1; fi
				done
			fi
		fi
	;;



	## Create tiled TIFF files and move them to a new folder _tiled
	create_tile_tiff)
		prepare
		if [ ${VERBOSE} == "1" ]; then echo "Create tiled TIFF files and move them to a new folder"; fi
		FOLDER=$(echo ${WORKINGPATH} | sed "s|orig_||" | sed "s|_tif|_ptif|")

		if [ "${FOLDER}" == "${WORKINGPATH}" ]; then
			FOLDER=TILES
		fi

		if [ "$tifffiles" != "0" ]; then
			if [ ${NICEENABLED} == "1" ]; then
				if [ "${USEGM}" == "1" ]; then
					if [ "${TILETIFFSINGLEIMAGE}" == "1" ]; then
						i=${TILETIFFSINGLEIMAGENAME}
						if [ "${ROTATEDEGREE}" != "0" ]; then
							nice -n ${NICELEVEL} gm convert "${i}" -rotate ${ROTATEDEGREE} "${i/.tif/_${ROTATEDEGREE}.tif}"
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
							nice -n ${NICELEVEL} gm convert "${i/.tif/_${ROTATEDEGREE}.tif}" -define tiff:tile-geometry=256x256 'ptif:out.ptif';
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
							mv out.ptif "${i/.tif/_${ROTATEDEGREE}degree.tif}"
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
							rm "${i/.tif/_${ROTATEDEGREE}.tif}"
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
						else
							nice -n ${NICELEVEL} gm convert "${i}" -define tiff:tile-geometry=256x256 'ptif:out.ptif';
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
							mv out.ptif "${i/.tif/_0degree.tif}"
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
						fi
					else
						if [ "${ROTATEDEGREE}" != "0" ]; then
							for i in *.tif
							do
								nice -n ${NICELEVEL} gm convert "${i}" -rotate ${ROTATEDEGREE} "${i/.tif/_${ROTATEDEGREE}.tif}"
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
								nice -n ${NICELEVEL} gm convert "${i/.tif/_${ROTATEDEGREE}.tif}" -define tiff:tile-geometry=256x256 'ptif:out.ptif';
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
								mv out.ptif "${i/.tif/_${ROTATEDEGREE}degree.tif}";
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
								rm "${i/.tif/_${ROTATEDEGREE}.tif}"
							done
						else
							for i in *.tif
							do
								nice -n ${NICELEVEL} gm convert "${i}" -define tiff:tile-geometry=256x256 'ptif:out.ptif';
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
								mv out.ptif "${i/.tif/_0degree.tif}";
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
							done
						fi
					fi
				else
					if [ "${TILETIFFSINGLEIMAGE}" == "1" ]; then
						i=${TILETIFFSINGLEIMAGENAME}
						if [ "${ROTATEDEGREE}" != "0" ]; then
							nice -n ${NICELEVEL} convert "${i}" -quiet -rotate ${ROTATEDEGREE} "${i/.tif/_${ROTATEDEGREE}.tif}"
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
							nice -n ${NICELEVEL} convert "${i/.tif/_${ROTATEDEGREE}.tif}" -quiet -define tiff:tile-geometry=256x256 'ptif:out.ptif';
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
							mv out.ptif "${i/.tif/_${ROTATEDEGREE}degree.tif}"
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
							rm "${i/.tif/_${ROTATEDEGREE}.tif}"
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
						else
							nice -n ${NICELEVEL} convert "${i}" -quiet -define tiff:tile-geometry=256x256 'ptif:out.ptif';
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
							mv out.ptif "${i/.tif/_0degree.tif}"
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
						fi
					else
						if [ "${ROTATEDEGREE}" != "0" ]; then
							for i in *.tif
							do
								nice -n ${NICELEVEL} convert "${i}" -quiet -rotate ${ROTATEDEGREE} "${i/.tif/_${ROTATEDEGREE}.tif}"
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
								nice -n ${NICELEVEL} convert "${i/.tif/_${ROTATEDEGREE}.tif}" -quiet -define tiff:tile-geometry=256x256 'ptif:out.ptif';
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
								mv out.ptif "${i/.tif/_${ROTATEDEGREE}degree.tif}";
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
								rm "${i/.tif/_${ROTATEDEGREE}.tif}"
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
							done
						else
							for i in *.tif
							do
								nice -n ${NICELEVEL} convert "${i}" -quiet -define tiff:tile-geometry=256x256 'ptif:out.ptif';
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
								mv out.ptif "${i/.tif/_0degree.tif}";
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
							done
						fi
					fi
				fi
			else
				if [ "${USEGM}" == "1" ]; then
					if [ "${TILETIFFSINGLEIMAGE}" == "1" ]; then
						i=${TILETIFFSINGLEIMAGENAME}
						if [ "${ROTATEDEGREE}" != "0" ]; then
							gm convert "${i}" -rotate ${ROTATEDEGREE} "${i/.tif/_${ROTATEDEGREE}.tif}"
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
							gm convert "${i/.tif/_${ROTATEDEGREE}.tif}" -define tiff:tile-geometry=256x256 'ptif:out.ptif';
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
							mv out.ptif "${i/.tif/_${ROTATEDEGREE}degree.tif}"
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
							rm "${i/.tif/_${ROTATEDEGREE}.tif}"
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
						else
							gm convert "${i}" -define tiff:tile-geometry=256x256 'ptif:out.ptif';
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
							mv out.ptif "${i/.tif/_0degree.tif}"
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi

						fi
					else
						if [ "${ROTATEDEGREE}" != "0" ]; then
							for i in *.tif; do
								gm convert "${i}" -rotate ${ROTATEDEGREE} "${i/.tif/_${ROTATEDEGREE}.tif}"
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
								gm convert "${i/.tif/_${ROTATEDEGREE}.tif}" -define tiff:tile-geometry=256x256 'ptif:out.ptif';
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
								mv out.ptif "${i/.tif/_${ROTATEDEGREE}degree.tif}";
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
								rm "${i/.tif/_${ROTATEDEGREE}.tif}"
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
							done
						else
							for i in *.tif; do
								gm convert "${i}" -define tiff:tile-geometry=256x256 'ptif:out.ptif';
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
								mv out.ptif "${i/.tif/_0degree.tif}";
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
							done
						fi
					fi
				else
					if [ "${TILETIFFSINGLEIMAGE}" == "1" ]; then
						i=${TILETIFFSINGLEIMAGENAME}
						if [ "${ROTATEDEGREE}" != "0" ]; then
							convert "${i}" -rotate ${ROTATEDEGREE} "${i/.tif/_${ROTATEDEGREE}.tif}"
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
							convert "${i/.tif/_${ROTATEDEGREE}.tif}" -quiet -define tiff:tile-geometry=256x256 'ptif:out.ptif';
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
							mv out.ptif "${i/.tif/_${ROTATEDEGREE}degree.tif}"
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
							rm "${i/.tif/_${ROTATEDEGREE}.tif}"
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
						else
							convert "${i}" -quiet -define tiff:tile-geometry=256x256 'ptif:out.ptif';
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
							mv out.ptif "${i/.tif/_0degree.tif}"
							if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
						fi

					else
						if [ "${ROTATEDEGREE}" != "0" ]; then
							for i in *.tif
							do
								convert "${i}" -rotate ${ROTATEDEGREE} "${i/.tif/_${ROTATEDEGREE}.tif}"
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
								convert "${i/.tif/_${ROTATEDEGREE}.tif}" -quiet -define tiff:tile-geometry=256x256 'ptif:out.ptif';
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
								mv out.ptif "${i/.tif/_${ROTATEDEGREE}degree.tif}";
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
								rm "${i/.tif/_${ROTATEDEGREE}.tif}"
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
							done
						else
							for i in *.tif
							do
								convert "${i}" -quiet -define tiff:tile-geometry=256x256 'ptif:out.ptif';
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
								mv out.ptif "${i/.tif/_0degree.tif}";
								if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
							done
						fi
					fi
				fi
			fi
		fi

		if [ ! -d ${FOLDER} ] ; then
			mkdir ${FOLDER};
			if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi
		fi

		mv *degree* "${FOLDER}/"
		if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_tile_tiff. Aborting!" >&2; exit 1; fi

	;;


	## Write TIFF Header to TIFF files
	write_tiffheader)
		prepare
		cat  ../tiffwriter.conf >/dev/null || { echo "ERROR: cannot read tiffwriter.conf. Aborting!"; exit 1; }
		if [ ${VERBOSE} == "1" ]; then echo "Writing tiff header information to all *.tif files"; fi
		lsb_release -r 2>/dev/null | grep 14.04 >/dev/null ; trustytahr=$?
		## convert from iso-8859-1 to utf8 if needed
		if [ "$(file ../tiffwriter.conf)" == "../tiffwriter.conf: ISO-8859 text, with CRLF line terminators" ]; then
			if [ ${VERBOSE} == "1" ]; then echo "File is ISO-8859-1"; fi
			mv ../tiffwriter.conf ../tiffwriter.conf-iso8859-1
			if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in write_tiffheader. Aborting!" >&2; exit 1; fi
			iconv --from-code=iso8859-1 --to-code=utf-8 ../tiffwriter.conf-iso8859-1 > ../tiffwriter.conf
			if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in write_tiffheader. Aborting!" >&2; exit 1; fi
		fi

		## write tiffheader if tiffwriter.conf is utf8 or ASCII
		if file ../tiffwriter.conf | grep -E "tiffwriter.conf.*(UTF-8|ASCII)" >/dev/null ; then
			## if file extension exists
			if [ "${tiffiles}" != "0" ] ; then
				for i in *.tif; do
					tiffinfo "${i}" &>/dev/null | grep 'Compression Scheme: Old-style JPEG' -q && { echo "WARNING: ${i} is compressed Old-style JPEG, skipping tiffwriter."; continue; }
					tiffset -s ImageDescription "$(grep ImageDescription ../tiffwriter.conf | sed 's/ImageDescription=//g')" "${i}" 2>&1 >> /dev/null;
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in write_tiffheader. Aborting!" >&2; exit 1; fi
					tiffset -s DocumentName "$(grep Documentname ../tiffwriter.conf | sed 's/Documentname=//g')" "${i}" 2>&1 >> /dev/null;
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in write_tiffheader. Aborting!" >&2; exit 1; fi
					tiffset -s Artist "$(grep Artist ../tiffwriter.conf | sed 's/Artist=//g')" "${i}" 2>&1 >> /dev/null;
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in write_tiffheader. Aborting!" >&2; exit 1; fi
					#tiffset -s Make "$TIFFSET_MAKE" ${i} 2>&1 >> /dev/null;
					tiffset -s Software "$TIFFSET_SOFTWARE" "${i}" 2>&1 >> /dev/null;
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in write_tiffheader. Aborting!" >&2; exit 1; fi
					tiffset -s PageName "|${i/.tif/}||" "${i}" 2>&1 >> /dev/null;
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in write_tiffheader. Aborting!" >&2; exit 1; fi
					if [ $trustytahr -eq 0 ]; then
						exiv2 -M "del Exif.Image.ExifTag" modify $i
						if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in write_tiffheader. Aborting!" >&2; exit 1; fi
					fi
				done
			fi
		fi
	;;

	generate_watermark)
		if [ "${USEGM}" == "1" ]; then
			echo -e "WARNING: This functionality works only with imagemagick. You need this package installed, even if you specified to use graphicsmagick at the top of this file"
		fi
		prepare
		if [ ${VERBOSE} == "1" ]; then echo "Insert Watermark into all TIFF images"; fi

		if [ "${tiffiles}" != "0" ] ; then
			for i in *.tif; do
				WIDTH=$(convert "${i}" -quiet -format "%[fx:.25*w]" info:)
				HEIGHT=$(convert "${i}" -quiet -format "%[fx:.25*h]" info:)


				if [ "${NICEENABLED}" == "1" ]; then
					nice -n ${NICELEVEL} composite -gravity northwest -geometry +30+30 -background transparent \( ${WATERMARKIMAGEFILE} -resize ${WIDTH}x${HEIGHT} \) "${i}" "${i}" 2>&1
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured during watermark generation. Aborting!" >&2; exit 1; fi
				else
					composite -gravity northwest -geometry +30+30 -background transparent \( ${WATERMARKIMAGEFILE} -resize ${WIDTH}x${HEIGHT} \) "${i}" "${i}" 2>&1
					if [ "$?" != "0" ]; then echo -e "ERROR: an error occured during watermark generation. Aborting!" >&2; exit 1; fi
				fi
			done
		fi
	;;


	cut_pixel_bottom)
		if [ "${CUTPIXELAMOUNT}" == "DISABLED" ]; then
			echo "INFO: You can not use cut_pixel_bottom because it has beed disabled in the script. Skipping this call.";
			SKIPSTEP=1
		fi

		if [ -f ${WORKINGPATH}/../cropdone.txt ]; then
			echo "INFO: You can not use cut_pixel_bottom because the script cutted already for this folder. Skipping this call."
			SKIPSTEP=1
		fi


		if [ "${SKIPSTEP}" == "0" ]; then
			prepare
			if [ ${VERBOSE} == "1" ]; then echo "Cutting ${CUTPIXELAMOUNT}px from the bottom of all TIFF files"; fi

			if [ "${tiffiles}" != "0" ]; then
				for i in *.tif; do
					echo "Cropping: ${i} - ${CUTPIXELAMOUNT}" >> ../cropdone.txt
					if [ "${NICEENABLED}" == "1" ]; then
						if [ "${USEGM}" == "1" ]; then
							nice -n ${NICELEVEL} gm convert "${i}" -gravity south -chop 0x${CUTPIXELAMOUNT} "${i}" 2>&1 >> ../cropdone.txt
							if [ "$?" != "0" ]; then echo "ERROR: An error occured during cropping ${i} with ${CUTPIXELAMOUNT} pixel" | tee ../cropdone.txt; fi
						else
							nice -n ${NICELEVEL} convert "${i}" -quiet -gravity south -chop 0x${CUTPIXELAMOUNT} "${i}" 2>&1 >> ../cropdone.txt
							if [ "$?" != "0" ]; then echo "ERROR: An error occured during cropping ${i} with ${CUTPIXELAMOUNT} pixel" | tee ../cropdone.txt; fi
						fi
					else
						if [ "${USEGM}" == "1" ]; then
							gm convert "${i}" -gravity south -chop 0x${CUTPIXELAMOUNT} "${i}" 2>&1 >> ../cropdone.txt
							if [ "$?" != "0" ]; then echo "ERROR: An error occured during cropping ${i} with ${CUTPIXELAMOUNT} pixel" | tee ../cropdone.txt; fi
						else
							convert "${i}" -quiet -gravity south -chop 0x${CUTPIXELAMOUNT} "${i}" 2>&1 >> ../cropdone.txt
							if [ "$?" != "0" ]; then echo "ERROR: An error occured during cropping ${i} with ${CUTPIXELAMOUNT} pixel" | tee ../cropdone.txt; fi
						fi
					fi
				done


			fi
		fi
	;;


	create_thumbnails)
		prepare
		if [ ${VERBOSE} == "1" ]; then echo "Create Thumbnails that fit in a box of ${THUMBNAILBOXSIZE} px"; fi

		if [ ! -d "../pimped_jpg" ]; then echo -e "ERROR: The pimped_jpg directory does not exist.  Aborting." >&2; exit 1; fi


		if [ "${tiffiles}" != "0" ] ; then

			for i in *.tif; do
				if [ "${NICEENABLED}" == "1" ]; then
					if [ "${USEGM}" == "1" ]; then
						nice -n ${NICELEVEL} gm convert "${i}[0]" -thumbnail ${THUMBNAILBOXSIZE} ../pimped_jpg/"${i/.tif/.jpg}"
						if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_thumbnails. Aborting!" >&2; exit 1; fi
					else
						nice -n ${NICELEVEL} convert "${i}[0]" -quiet -thumbnail ${THUMBNAILBOXSIZE} ../pimped_jpg/"${i/.tif/.jpg}"
						if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_thumbnails. Aborting!" >&2; exit 1; fi
					fi
				else
					if [ "${USEGM}" == "1" ]; then
						gm convert "${i}[0]" -thumbnail ${THUMBNAILBOXSIZE} ../pimped_jpg/"${i/.tif/.jpg}"
						if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_thumbnails. Aborting!" >&2; exit 1; fi
					else
						convert "${i}[0]" -quiet -thumbnail ${THUMBNAILBOXSIZE} ../pimped_jpg/"${i/.tif/.jpg}"
						if [ "$?" != "0" ]; then echo -e "ERROR: an error occured in create_thumbnails. Aborting!" >&2; exit 1; fi
					fi
				fi
			done
		fi
	;;


	prepare)
		prepare
	;;


        ## Revert tif images to uncompressed format
        decompress)
                prepare
                mogrify -compress NONE *.tif
        ;;

	compress_jpeg_if_uncompressed)
		if [ ${VERBOSE} == "1" ]; then echo "Creating TIFF/JPEG compressed images if image was uncompressed"; fi
		if [ "$#" -ne "2" ]; then echo "Wrong number of arguments, expecting 2, got $#."; exit 1; fi

		prepare
		## if files are tiff files
		if [ "${tiffiles}" != "0" ] ; then
			for i in *.tif; do
				## if picture is not bitonal compress
				if [ "$(tiffinfo ${i} 2>&1 | grep Bits | awk {'print $2'})" != "1" ]; then
				  	if identify -format "%[compression]" ${i} | grep -q 'None'; then
						if [ "${NICEENABLED}" == "1" ]; then
							if [ "${USEGM}" == "1" ]; then
								if ! nice -n ${NICELEVEL} gm mogrify -depth 8 +matte -colorspace RGB -compress JPEG "${i}"; then
									echo -e "ERROR: an error occured in convert_jpeg. Aborting!" >&2
									exit 1
								fi
							else
								if ! nice -n ${NICELEVEL} mogrify -quiet -depth 8 -alpha off -compress JPEG "${i}"; then
									echo -e "ERROR: an error occured in convert_jpeg. Aborting!" >&2
									exit 1
								fi
							fi
						else
							if [ "${USEGM}" == "1" ]; then
								if ! gm mogrify -depth 8 +matte -colorspace RGB -compress JPEG "${i}"; then
									echo -e "ERROR: an error occured in convert_jpeg. Aborting!" >&2
									exit 1
								fi
							else
								if ! mogrify -quiet -depth 8 -alpha off -compress JPEG "${i}"; then
									echo -e "ERROR: an error occured in convert_jpeg. Aborting!" >&2
									exit 1
								fi
							fi
						fi
					fi
				fi
			done
		fi
	;;

	*)
		echo
		echo "   USAGE:  $0 PARAMETER /path/to/files"
		echo

		echo "   Available parameters:
      * compress_images       - Resize and compress all TIFF files to a new DPI value
      * create_jpeg           - Create JPEG files from TIFF or JP2 files and stores them in a new folder
      * create_jpeg_size      - Create JPEG files with given size (KB) from TIFF files and stores them in a new _jpg folder
      * convert_jpeg          - Converting TIF or JPEG files to TIFF/JPEG
      * clean_tiffheader      - Strip all information from the TIFF header
      * convert_images        - Convert images to TIFF/JPEG based on file extension
      * convert_jpeg_quality  - Convert all TIFF files to TIFF/JPEG using fixed quality value
      * convert_gray          - Convert all TIFF files to gray
      * convert_monochrome    - Convert all TIFF files to monochrome
      * remove_black_border   - Remove black border from all TIFF files
      * straighten            - Deskew all TIFF files
      * correct259            - Set TIFF Header fiels 259 to uncompressed if not set
      * create_tile_tiff      - Create tiled TIFF files and move them to a new folder _tiled
      * write_tiffheader      - Write TIFF headers to TIFF files
      * generate_watermark    - Insert watermark into all TIFF images
      * set_jpeg_colorspace   - Set the colorspace for all JPEG images to RGB
      * cut_pixel_bottom      - Cuting specified amount of pixels from bottom of all TIFF files
      * create_thumbnails     - Create thumbnails that fit in a box of a defined size of px
      * prepare               - Only run the prepare function (rename to lower, set rights, ...)
      * tiffjpeg_resize	      - Mogrify to Tiff/Jpeg and resize to \$3 (e.g. 80%)
      * tif2jpg               - Convert Tiff to Jpeg, uses soure and destination folder (no mogrify)
      * mogrify-fx            - Mogrify Tiff to Tiff/Jpeg, apply fx
      * convert_jpeg_rm_icc   - Like convert_jpeg, but apply and remove ICC profile if existent.
      * decompress            - Convert all tiff images to uncompressed tif format.
      * create_tiffjpeg       - Convert all tiff and jpg files to tiff/jpeg w/ target folder
	  * compress_jpeg_if_uncompresed  - Convert TIFF files to TIFF/JPEG if they are uncompressed";
		echo
		exit 1
	;;

esac
