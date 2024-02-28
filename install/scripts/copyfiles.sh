#!/bin/bash
set -u
set -e

# first parameter src, second parameter dst

if [ "$#" -ne "2" ]
then
    echo "ERROR: Two parameters are needed, source and destination directory"
    exit 1
fi


# test if source directory exists
[ -d "$1" ] || { echo "ERROR: Source directory does not exist" >&2; exit 1; }

echo "Starting copy process..."

nice -n 18 /usr/bin/rsync -O -a --delete "$1"/ "$2"/ || { echo "ERROR: Error while copying files" >&2; exit 1; }

echo "Copy process finished!"
