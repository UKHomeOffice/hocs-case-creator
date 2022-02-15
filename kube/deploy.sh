#!/bin/bash
set -euo pipefail

export KUBE_NAMESPACE=${ENVIRONMENT}
export KUBE_SERVER=${KUBE_SERVER}
export KUBE_TOKEN=${KUBE_TOKEN}
export VERSION=${VERSION}

echo
echo "Deploying hocs-case-creator to ${ENVIRONMENT}"
echo "Service version: ${VERSION}"
echo

if [[ ${KUBE_NAMESPACE} == *prod ]]
then
    export UPTIME_PERIOD="Mon-Sun 05:10-22:50 Europe/London"
else
    export UPTIME_PERIOD="Mon-Fri 08:10-17:50 Europe/London"
    export MESSAGE_IGNORED_TYPES=''
fi

export MIN_REPLICAS="1"
export MAX_REPLICAS="2"

export KUBE_CERTIFICATE_AUTHORITY="https://raw.githubusercontent.com/UKHomeOffice/acp-ca/master/${CLUSTER_NAME}.crt"

cd kd

kd --timeout 10m \
    -f deployment.yaml \
    -f service.yaml \
    -f autoscale.yaml
