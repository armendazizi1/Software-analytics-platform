#!/bin/sh

cd  ../../../../libs
java -jar ck-0.6.3-SNAPSHOT-jar-with-dependencies.jar ../src/main/resources/projects/$1 true 0 false
