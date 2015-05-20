#!/bin/bash 

/usr/lib/jvm/java-7-openjdk-amd64/jre/bin/java \
  -Dlog4j.configurationFile=file:./log4j2.yaml \
  -Dlog4j2.disable.jmx=true \
  -Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager \
  -jar ./inertia-lch.jar ./eve.yaml \
  &> start.out \
  &
