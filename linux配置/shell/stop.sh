#!/bin/sh
APP_NAME=audio_transf-1.0-SNAPSHOT.jar

PID=`ps -ef|grep $APP_NAME|grep -v grep|awk '{print $2}'`
if [ -z "$PID" ]
then
    echo $APP_NAME is already stopped
else
    echo killing $PID
    kill -9 $PID
fi