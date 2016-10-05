#!/usr/bin/env bash
set -e
[ ! -f run.jar ] &&  mvn package
./node_modules/.bin/rollup -c config/client.js
./node_modules/.bin/rollup -c config/server.js
java -jar run.jar --redeploy="src/**" --on-redeploy="./devmode.sh"
