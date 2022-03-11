# hocs-case-creator

## Behaviour

- Reads a message containing a JSON payload from an AWS SQS Queue
- Validates the payload against a pre-described schema
- Creates a case with associated data (i.e. correspondents, complaint type)

## Getting Started

### Prerequisites

* ```Java 11```
* ```Docker```
* ```LocalStack```

### Local Development

This service has a dependency on the workflow and casework services. 

There is also a small java client `src/test/java/uk/gov/digital/ho/hocs/clientutil/SQSSender.java`. This can be used for
sending messages to the local queue.

In order to run the service locally, LocalStack is required. We have provided an [docker-compose.yml](docker-compose.yml) file to support this.

To start LocalStack through Docker, run the following command from the root of the project:

```shell
docker-compose up
```

This brings up the LocalStack docker image and creates the necessary AWS resources to run the project. This is done through mounting the [localstack configuration folder](config/localstack) into the docker image.

This configuration folder contains 3 shell scripts that each handle a separate part of the AWS creation.

1. [1-setup-sqs.sh](config/localstack/1-setup-sqs.sh)  
2. [2-setup-sns.sh](config/localstack/2-setup-sns.sh)  
3. [3-setup-s3.sh](config/localstack/3-setup-s3.sh)  

To stop the running containers, run the following:

```shell
docker-compose down
```

### Configuration

The following table contains the mandatory and optional properties that need to be set for deployment.

| Property | Description | Example | Mandatory |
| -------- | -------- |-------- |-------- |
| AWS_SQS_CONFIG_REGION | The region for the AWS SQS queue | eu-west-2 | Yes |
| AWS_SQS_CASE_CREATOR_URL | The full AWS SQS queue URL | http://localhost:4566/queue/case-creator-queue | Yes |
| AWS_SQS_CASE_CREATOR_ACCOUNT_ACCESS_KEY | The SQS access key | 12345 | Yes
| AWS_SQS_CASE_CREATOR_ACCOUNT_SECRET_KEY | The SQS secret key | 12345 | Yes
| AWS_SQS_CASE_CREATOR_ATTRIBUTE_MAX_MESSAGES | The amount of messages to read at one time | 10 | No |
| AWS_SQS_CASE_CREATOR_ATTRIBUTE_WAIT_TIME | The poll time for the queue listener | 5 | No |
| AWS_SNS_CONFIG_REGION | The region for the AWS SNS queue | eu-west-2 | Yes |
| AWS_SNS_AUDIT_ACCOUNT_ACCESS_KEY | The SNS access key | 12345 | Yes
| AWS_SNS_AUDIT_ACCOUNT_SECRET_KEY | The SNS secret key | 12345 | Yes
| AWS_SNS_AUDIT_ACCOUNT_ID | The SNS account id | 000000000000 | Yes
| AWS_SNS_AUDIT_TOPIC_NAME | The SNS topic name to send to | hocs-audit-topic | Yes
| AWS_SNS_AUDIT_ARN | The SNS ARN for the topic | arn:aws:sns:eu-west-2:000000000000:hocs-audit-topic | Yes
| AWS_S3_CONFIG_REGION | The region for the AWS S3 queue | eu-west-2 | Yes |
| AWS_S3_UNTRUSTED_ACCOUNT_ACCESS_KEY | The S3 access key | 12345 | Yes
| AWS_S3_UNTRUSTED_ACCOUNT_SECRET_KEY | The S3 secret key | 12345 | Yes
| AWS_S3_UNTRUSTED_ACCOUNT_BUCKET_KMS_KEY | The S3 bucket kms key | [UNSET] | Yes
| AWS_S3_UNTRUSTED_BUCKET_NAME | The S3 bucket name to send to | untrusted-bucket | Yes
| CASE_CREATOR_WORKFLOW_SERVICE | The URL of the workflow service  | http://localhost:8091   | Yes  |
| CASE_CREATOR_CASE_SERVICE | The URL of the casework service  | http://localhost:8082   | Yes  |
| CASE_CREATOR_BASICAUTH | The basic auth credentials  | [UNSET]   | Yes  |
| CASE_CREATOR_IDENTITIES_COMPLAINTS_UKVI_USER | The UKVI System User ID  | UUID   | Yes  |
| CASE_CREATOR_IDENTITIES_COMPLAINTS_UKVI_GROUP | The UKVI System Group ID  | /CMSMNIAZQXMZQ6IGEKTRWA   | Yes  |
| CASE_CREATOR_IDENTITIES_COMPLAINTS_UKVI_TEAM | The UKVI System Team ID  | UUID   | Yes  |
| INFO_NAMESPACE | The kubernetes namespace | local | Yes |
| SERVER_PORT | The port the server listens on | 8092 | Yes

## JSON Schema

The service uses the JSON schema defined
in [hocs-ukvi-complaint-schema](https://github.com/UKHomeOffice/hocs-ukvi-complaint-schema). The schema is pulled into
the build as a dependency in the gradle file.

For local development, build the schema locally and publish
to a Maven local repository. View the README in `hocs-ukvi-complaint-schema` for more information.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
