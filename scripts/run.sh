#!/usr/bin/env bash

NAME=${NAME:-case-creator}

JAR=$(find . -name ${NAME}*.jar|head -1)

handleSigTerm() {
  echo "Caught SIGTERM"
  kill -TERM "$child"
}

trap handleSigTerm SIGTERM

java ${JAVA_OPTS} -Dcom.sun.management.jmxremote.local.only=false -Djava.security.egd=file:/dev/./urandom -jar "${JAR}" &

child=$!
wait "$child"

