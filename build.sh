#!/usr/bin/env bash

VERSION=$1

set -eou pipefail

lein clean
lein uberjar

docker build -t config-server-example:$1 .
docker tag config-server-example:$1 formicarium/config-server-example:$1
docker push formicarium/config-server-example:$1
