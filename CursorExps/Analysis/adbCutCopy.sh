#!/bin/bash

#Get all of a directory(sdcard), and filter them
for file in `~/Sdk/platform-tools/adb shell ls /mnt/sdcard/ | grep $1`
do
	echo $file
    file=`echo -e $file | tr -d "\r"`; # osx fix! ghhrrr :@ :(
    # pull the command
    ~/Sdk/platform-tools/adb pull /mnt/sdcard/$file $2/$file;
    ~/Sdk/platform-tools/adb shell rm /mnt/sdcard/$file
done
