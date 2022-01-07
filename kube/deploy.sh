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
    export CASE_CREATOR_UKVI_COMPLAINT_USER="TBD"
    export CASE_CREATOR_UKVI_COMPLAINT_TEAM="TBD"
    export CASE_CREATOR_UKVI_COMPLAINT_GROUP="TBD"

    export MIN_REPLICAS="1"
    export MAX_REPLICAS="2"

    export UPTIME_PERIOD="Mon-Sun 05:10-22:50 Europe/London"
else
    export CASE_CREATOR_UKVI_COMPLAINT_USER="96193359-228a-4d22-908a-eb44e9877163"
    export CASE_CREATOR_UKVI_COMPLAINT_TEAM="08e30ffc-2087-ff3a-b19b-343a88491347"
    export CASE_CREATOR_UKVI_COMPLAINT_GROUP="/COMP_CCH_zqxmzQ6iEkTRw"

    export MIN_REPLICAS="1"
    export MAX_REPLICAS="2"

    export UPTIME_PERIOD="Mon-Fri 08:10-17:50 Europe/London"
fi

export KUBE_CERTIFICATE_AUTHORITY="https://raw.githubusercontent.com/UKHomeOffice/acp-ca/master/${CLUSTER_NAME}.crt"

cd kd

kd --timeout 10m \
    -f identityConfigMap.yaml \
    -f deployment.yaml \
    -f service.yaml \
    -f autoscale.yaml
