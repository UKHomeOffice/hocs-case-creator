#!/bin/bash
set -euo pipefail

export KUBE_NAMESPACE=${ENVIRONMENT}
export KUBE_SERVER=${KUBE_SERVER}
export KUBE_TOKEN=${KUBE_TOKEN}
export VERSION=${VERSION}

if [[ ${KUBE_NAMESPACE} == *prod ]]
then
    export MIN_REPLICAS="2"
    export MAX_REPLICAS="6"
    export CLUSTER_NAME="acp-prod"
else
    export MIN_REPLICAS="1"
    export MAX_REPLICAS="3"
    export CLUSTER_NAME="acp-notprod"
fi

export KUBE_CERTIFICATE_AUTHORITY=/tmp/acp.crt
if ! curl --silent --fail --retry 5 \
	https://raw.githubusercontent.com/UKHomeOffice/acp-ca/master/$CLUSTER_NAME.crt -o $KUBE_CERTIFICATE_AUTHORITY; then
	echo "failed to download ca for kube api"
	exit 1
fi

cd kd || exit 1

kd --timeout 15m \
    -f deployment.yaml \
    -f autoscale.yaml
