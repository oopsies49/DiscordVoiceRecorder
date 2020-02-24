# DiscordVoiceRecorder
A discord bot that records a voice channel and has the ability to upload the clip to an AWS S3 bucket

# Dependencies
install java 8+

install ffmpeg https://www.ffmpeg.org/

# Building
`gradlew build`

# Configuring
Modify application.properties with your bot token and a path to save clips. If using an AWS S3 bucket, it must have public access allowing reads and writes (If any s3 experts would like to point out how I can enhance this to disallow public writes, I am interested in learning). 
```
# your discord bot token from https://discordapp.com/developers/
botToken=

# character that tells the bot to listen to a command
commandCharacter=!

# local filesystem path that the bot should save clips to
recordingPath=

# the format that the raw output should be converted to when ending a recording
recordingFormat=flac

# Should the bot attempt to use the AWS feature
# if false the upload command will attempt to upload the file to the discord channel directly
#    discord file size limits aren't very large, so long recordings will likely fail to upload
useAWS=false

# The AWS bucket name that the file should be uploaded to
awsBucket=
```

# Running 
`gradlew run`

# Usage
The bot listens for chat commands in your server. When a command to start recording is received the bot will join the voice channel that you are in and start recording. 

## Commands
`!start` starts recording

`!end` ends recording and endcodes the raw data into the configured output format, bot will remain in the voice channel until encoding is finished.

`!upload` uploads the most recent encoded file to discord, or the configured AWS S3 bucket. 

# FAQ
Q: Why is does the bot appear as if it is talking when it is recording?

A: The audio recording API is undocumented and unsupported as far as I can tell. To be able to receive audio you must also be sending audio at the same time. To workaround this, the bot sends silence while recording.



