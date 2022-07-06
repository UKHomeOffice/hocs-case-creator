#!/bin/bash
set -euo pipefail

export KUBE_NAMESPACE=${ENVIRONMENT}
export KUBE_SERVER=${KUBE_SERVER}
export KUBE_TOKEN=${KUBE_TOKEN}
export VERSION=${VERSION}
export CLUSTER_NAME=${CLUSTER_NAME}
export DEPLOYMENT_TYPE="creator"
export SQS_SECRET_NAME="case-creator-sqs"
export PORT=10443

echo
echo "Deploying hocs-case-creator to ${ENVIRONMENT}"
echo "Service version: ${VERSION}"
echo

if [[ ${KUBE_NAMESPACE} == *prod ]]
then
    export UPTIME_PERIOD="Mon-Sun 05:10-22:50 Europe/London"
    export MESSAGE_IGNORED_TYPES=UKVI_COMPLAINTS
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

if [[ ${KUBE_NAMESPACE} == *dev* ]]
then
  echo "Deploying hocs-case-migrator to ${ENVIRONMENT}"
  echo "Service version: ${VERSION}"
  export DEPLOYMENT_TYPE="migrator"
  export SQS_SECRET_NAME="case-migrator-sqs"
  export PORT=10943
  kd --timeout 10m \
      -f deployment.yaml \
      -f service.yaml \
      -f autoscale.yaml
fi
