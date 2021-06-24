#!/bin/sh

cd ../projects/$1

git checkout -f $2
git blame -w $3
