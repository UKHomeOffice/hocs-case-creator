# kube-hocs-case-creator
Kubernetes configuration of the case creator project

## Config Map Values

### CASE_CREATOR_UKVI_COMPLAINT_USER
The user UUID after creating a "system user" as the identity for UKVI complaint creation.

### CASE_CREATOR_UKVI_COMPLAINT_TEAM
The team UUID is taken from the `team` table in the info schema. It is the team associated with the display_name: "CCH Webform Cases"

### CASE_CREATOR_UKVI_COMPLAINT_GROUP
The group after creating a "system user" as the identity for UKVI complaint creation.
