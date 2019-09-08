		


# $1 = name
# $2 = tempDir
# $3 = searchTerm
# $4 = vidTime


# creates a video with blue background and resolution 480x360
		# video contains name of creation
		ffmpeg -loglevel panic -f lavfi -i color=c=blue:s=480x360:d=$4 -vf "drawtext=fontfile=myfont.ttf:fontsize=30: fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text='$3'" $2/video.mp4
		ffmpeg -loglevel panic -i $2/video.mp4 -i $2/audio.mp3 -strict experimental "creations/$1.mp4"
		rm -fr $2


