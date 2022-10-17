{{- define "deployment.envs" }}
- name: JAVA_OPTS
  value: '{{ tpl .Values.app.env.javaOpts . }}'
- name: SERVER_PORT
  value: '{{ include "hocs-app.port" . }}'
- name: SPRING_PROFILES_ACTIVE
  value: '{{ tpl .Values.app.env.springProfiles . }}'
- name: CASE_CREATOR_CASE_SERVICE
  value: '{{ tpl .Values.app.env.caseworkService . }}'
- name: CASE_CREATOR_WORKFLOW_SERVICE
  value: '{{ tpl .Values.app.env.workflowService . }}'
- name: MESSAGE_IGNORED_TYPES
  value: '{{ tpl .Values.app.env.ignoredTypes . }}'
- name: CASE_CREATOR_BASICAUTH
  valueFrom:
    secretKeyRef:
      name: ui-casework-creds
      key: plaintext
- name: AWS_SQS_CASE_CREATOR_ACCOUNT_ACCESS_KEY
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-case-creator-sqs
      key: access_key_id
- name: AWS_SQS_CASE_CREATOR_ACCOUNT_SECRET_KEY
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-case-creator-sqs
      key: secret_access_key
- name: AWS_SQS_CASE_CREATOR_URL
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-case-creator-sqs
      key: sqs_url
- name: AWS_S3_UNTRUSTED_ACCOUNT_ACCESS_KEY
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-untrusted-s3
      key: access_key_id
- name: AWS_S3_UNTRUSTED_ACCOUNT_SECRET_KEY
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-untrusted-s3
      key: secret_access_key
- name: AWS_S3_UNTRUSTED_BUCKET_NAME
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-untrusted-s3
      key: bucket_name
- name: AWS_S3_UNTRUSTED_ACCOUNT_BUCKET_KMS_KEY
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-untrusted-s3
      key: kms_key_id
- name: CASE_CREATOR_IDENTITIES_COMPLAINTS_UKVI_GROUP
  valueFrom:
    secretKeyRef:
      name: hocs-case-creator-identities
      key: complaint_ukvi_group
- name: CASE_CREATOR_IDENTITIES_COMPLAINTS_UKVI_USER
  valueFrom:
    secretKeyRef:
      name: hocs-case-creator-identities
      key: complaint_ukvi_user
- name: CASE_CREATOR_IDENTITIES_COMPLAINTS_UKVI_TEAM
  valueFrom:
    secretKeyRef:
      name: hocs-case-creator-identities
      key: complaint_ukvi_team
{{- end -}}
