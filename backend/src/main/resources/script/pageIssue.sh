#!/bin/sh

cd issue

start=${2}
end=${3}

for i in $(seq $start $end); do

  url="${1}/issues?page=${i}&per_page=100&state=all"
  curl -i -H "Authorization: token 9f013b28fd3cb66455cb5f94498fff3978b94609" $url >issue${i}.txt

  while true; do
    if [ -s issue${i}.txt ]; then
      break
    else
      curl -i -H "Authorization: token 9f013b28fd3cb66455cb5f94498fff3978b94609" $url >issue${i}.txt
    fi
  done

done
