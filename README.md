# hocs-case-creator

## Behaviour

- Reads a JSON payload from an AWS SQS queue
- Using Apache Camel
  - Validates the payload
  - Manages the Dead letter queue
  - Passes the JSON to the spring boot service
- creates a case
- adds a correspondent
- adds a complaint type

## Configuration

The following table contains the mandatory and optional properties that need to be set for deployment.

| Property | Description |Example |Mandatory |
| -------- | -------- |-------- |-------- |
| AWS_SQS_REGION | The DECS AWS region (used by all AWS services, comes from hocs-queue-config map) | eu-west-2   | Yes  |
| AWS_ACCOUNT_ID | The DECS AWS account_Id (used by all AWS services, comes from hocs-queue-config map)  | 1234567   | Yes  |
| CASE_CREATOR_WORKFLOW_SERVICE | The URL of the workflow service  | http://localhost:8091   | Yes  |
| CASE_CREATOR_CASE_SERVICE | The URL of the casework service  | http://localhost:8082   | Yes  |
| CASE_CREATOR_BASICAUTH | The basic auth credentials  | UNSET   | Yes  |
| CASE_CREATOR_UKVI_COMPLAINT_USER | The UKVI System User ID  | UUID   | Yes  |
| CASE_CREATOR_UKVI_COMPLAINT_GROUP | The UKVI System Group ID  | /CMSMNIAZQXMZQ6IGEKTRWA   | Yes  |
| CASE_CREATOR_SQS_ACCESS_KEY | The SQS access key  | 1234   | Yes  |
| CASE_CREATOR_SQS_SECRET_KEY | The SQS secret key  | 1234   | Yes  |
| CASE_CREATOR_UKVI_COMPLAINT_QUEUE_NAME | The UKVI queue name | ukvi-complaint-queue | Yes |
| CASE_CREATOR_UKVI_COMPLAINT_DL_QUEUE_NAME | The UKVI dead letter queue name | ukvi-complaint-queue-dlq | Yes |
| CASE_CREATOR_UKVI_COMPLAINT_QUEUE_MAXIMUM_REDELIVERIES | Sets the maximum number of times a message exchange will be redelivered | 10 | No |
| CASE_CREATOR_UKVI_COMPLAINT_QUEUE_REDELIVERY_DELAY | Sets the initial redelivery delay in milliseconds | 1000 | No |
| CASE_CREATOR_UKVI_COMPLAINT_QUEUE_BACKOFF_MULTIPLIER | Enables exponential backoff and sets the multiplier used to increase the delay between redeliveries | 5 | No |
| CASE_CREATOR_UKVI_COMPLAINT_QUEUE_BACKOFF_IDLE_THRESHOLD | The number of subsequent idle polls that should happen before the backoffMultipler should kick-in. | 1 | No |
| CASE_CREATOR_UKVI_COMPLAINT_QUEUE_WAIT_TIME_SECONDS | The duration (in seconds) for which the call waits for a message to arrive in the queue before returning. | 20 | No |
| CASE_CREATOR_UKVI_COMPLAINT_QUEUE_MAX_MESSAGES_PER_POLL | The maximum number of messages at each polling | 1 | No |
| CASE_CREATOR_UKVI_COMPLAINT_QUEUE_INITIAL_DELAY | Milliseconds before the first poll starts. | 5000 | No |
| CASE_CREATOR_UKVI_COMPLAINT_QUEUE_POLL_DELAY | Milliseconds before the next poll. | 100 | No |
| CASE_CREATOR_REST_CLIENT_RETRIES |The maximum number of retry attempts | 10 | No |
| CASE_CREATOR_REST_CLIENT_DELAY | The minimum delay between retries. | 1000 | No |
| AUDIT_APP_NAME | The kubernetes namespace | local | Yes |
| AUDIT_NAMESPACE | The kubernetes namespace | local | Yes |
| AUDIT_SNS_ACCESS_KEY | The SNS access key | 12345 | Yes |
| AUDIT_SNS_SECRET_KEY | The SNS secret key | 12345 | Yes |
| AUDIT_SNS_TOPIC_NAME | The SNS Topic Name | hocs-audit-topic | Yes |
| AUDIT_SNS_RETRIES | The maximum number of retry attempts | 10 | No |
| AUDIT_SNS_DELAY | The minimum delay between retries.  | 2000 | No |
| DOCUMENT_S3_ACCESS_KEY | The S3 access key | 12345 | Yes |
| DOCUMENT_S3_SECRET_KEY | The S3 secret key | 12345 | Yes |
| DOCUMENT_S3_UNTRUSTED_BUCKET_NAME | The untrusted bucket name | untrusted-bucket | Yes |
| DOCUMENT_S3_UNTRUSTED_BUCKET_KMS_KEY | The kms key | left blank locally | Yes | 

> Note : The rest retry properties must not be longer than the default visibility timeout of the SQS queue, which defaults to 30 seconds

## Local development

This service has a dependency on the workflow and casework services. These should be started using the docker-compose
file in the frontend project.

There is also a small java client `src/test/java/uk/gov/digital/ho/hocs/clientutil/SQSSender.java`. This can be used for
sending messages to the local queue.

## JSON Schema

The service uses the JSON schema defined
in [hocs-ukvi-complaint-schema](https://github.com/UKHomeOffice/hocs-ukvi-complaint-schema). The schema is pulled into
the build as a dependency in the gradle file.

For local development, build the schema locally and publish
to a Maven local repository. View the README in `hocs-ukvi-complaint-schema` for more information.
