#!/bin/sh

# shellcheck disable=SC2164
cd ../projects/$1
chmod -R -f 777 .
echo "this started"
git log --full-history --topo-order --name-status --pretty=format:"##_._##%ncommit %H" > log.txt
git log --full-history --topo-order --shortstat --pretty=format:"##_._##%ncommit %H" > stats.txt
echo "this ended"

mkdir issue

cd issue
touch issue1.txt
chmod -R -f 777 .

issueUrl="${2}/issues?page=1&per_page=100&state=all"

curl -i -H "Authorization: token 9f013b28fd3cb66455cb5f94498fff3978b94609" $issueUrl >issue1.txt

while true; do
  if [ -s issue1.txt ]; then
    break
  else
    curl -i -H "Authorization: token 9f013b28fd3cb66455cb5f94498fff3978b94609" $issueUrl >issue1.txt
  fi
done

cd ..
mkdir pullRequest

cd pullRequest
touch pullRequest1.txt

pullRequestUrl="${2}/pulls?page=1&per_page=100&state=all"

curl -i -H "Authorization: token 9f013b28fd3cb66455cb5f94498fff3978b94609" $pullRequestUrl >pullRequest1.txt

while true; do
  if [ -s pullRequest1.txt ]; then
    break
  else
    curl -i -H "Authorization: token 9f013b28fd3cb66455cb5f94498fff3978b94609" $pullRequestUrl >pullRequest1.txt
  fi
done
