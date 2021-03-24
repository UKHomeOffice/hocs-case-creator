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
| CASE_CREATOR_WORKFLOW_SERVICE | The URL of the workflow service  | http://localhost:8091   | Yes  |
| CASE_CREATOR_CASE_SERVICE | The URL of the casework service  | http://localhost:8082   | Yes  |
| CASE_CREATOR_BASICAUTH | The basic auth credentials  | UNSET   | Yes  |
| CASE_CREATOR_SQS_REGION | The AWS SQS region  | eu-west-2   | Yes  |
| CASE_CREATOR_SQS_ACCOUNT_ID | The SQS account Id  | 1234   | Yes  |
| CASE_CREATOR_SQS_ACCESS_KEY | The SQS access key  | 1234   | Yes  |
| CASE_CREATOR_SQS_SECRET_KEY | The SQS secret key  | 1234   | Yes  |
| CASE_CREATOR_UKVI_COMPLAINT_USER | The UKVI System User ID  | UUID   | Yes  |
| CASE_CREATOR_UKVI_COMPLAINT_GROUP | The UKVI System Group ID  | /CMSMNIAZQXMZQ6IGEKTRWA   | Yes  |
| CASE_CREATOR_UKVI_COMPLAINT_QUEUE_NAME | The UKVI queue name | ukvi-complaint-queue | Yes |
| CASE_CREATOR_UKVI_COMPLAINT_DL_QUEUE_NAME | The UKVI dead letter queue name | ukvi-complaint-queue-dlq | Yes |
| CASE_CREATOR_UKVI_COMPLAINT_QUEUE_MAXIMUM_REDELIVERIES | Sets the maximum number of times a message exchange will be redelivered | 10 | No |
| CASE_CREATOR_UKVI_COMPLAINT_QUEUE_REDELIVERY_DELAY | Sets the initial redelivery delay in milliseconds | 1000 | No |
| CASE_CREATOR_UKVI_COMPLAINT_QUEUE_BACKOFF_MULTIPLIER | Enables exponential backoff and sets the multiplier used to increase the delay between redeliveries | 5 | No |
| CASE_CREATOR_UKVI_COMPLAINT_QUEUE_BACKOFF_IDLE_THRESHOLD | The number of subsequent idle polls that should happen before the backoffMultipler should kick-in. | 1 | No |
| CASE_CREATOR_UKVI_COMPLAINT_QUEUE_WAIT_TIME_SECONDS | The duration (in seconds) for which the call waits for a message to arrive in the queue before returning. | 20 | No |
| CASE_CREATOR_UKVI_COMPLAINT_QUEUE_INITIAL_DELAY | Milliseconds before the first poll starts. | 5000 | No |
| CASE_CREATOR_UKVI_COMPLAINT_QUEUE_POLL_DELAY | Milliseconds before the next poll. | 100 | No |

## Local development

This service has a dependency on the workflow and casework services. These should be started using the docker-compose
file in the frontend project.

## Local testing

### Localstack and queues

There is a docker-compose file that will start localstack and add the required queues.

````console
$ cd localdev
$ docker-compose up
````

There is also a small java client `src/test/java/uk/gov/digital/ho/hocs/clientutil/SQSSender.java`. This can be used for
sending messages to the local queue.

## JSON Schema

The service uses the JSON schema defined
in [hocs-ukvi-complaint-schema](https://github.com/UKHomeOffice/hocs-ukvi-complaint-schema). The schema is pulled into
the build by a task which the gradle assemble jobs depends on. See the `build.gradle` file for more information.
