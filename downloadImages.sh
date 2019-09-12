#!/bin/bash

curl https://www.flickr.com/search/?text=$1 2> /dev/null | egrep 'live\.staticflickr[^"]*[a-zA-Z0-9]{2}\.jpg' -o | head -$2  | sed "s/^/http:\/\//g" | xargs wget -P ./$3
