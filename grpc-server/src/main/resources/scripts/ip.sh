#!/bin/sh

set -e
if [ -z $LOCAL_IP ]; then
  export LOCAL_IP=$(/sbin/ifconfig eth0 | grep 'inet addr:' | cut -d: -f2 | awk '{ print $1}')
fi

if [ "$1" = "java" ]; then
  java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar
fi

exec "$@"
