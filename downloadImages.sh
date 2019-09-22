#!/bin/bash


# set multi word searches to correct format
word=$(echo "$1" | sed "s/ /%20/g")



curl "https://www.flickr.com/search/?text=$word" 2> /dev/null | grep -oh '//live.staticflickr.com/.*jpg' | head -$2  | sed "s/^/http:/g" | xargs wget -P ./$3

