#!/usr/bin/env bash

# This script is a template for creating the complaint identity secret
# After creating a "system user" as the identity for UKVI complaint creation, 
# the userId and group should be extracted from keycloak and added to this secret

kubectl create secret generic hocs-delta-ukvi-complaint-identity \
    --from-literal=user=<userId> \
    --from-literal=group=<groupId>
    