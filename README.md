# hocs-case-creator

[![CodeQL](https://github.com/UKHomeOffice/hocs-case-creator/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/UKHomeOffice/hocs-case-creator/actions/workflows/codeql-analysis.yml)

This is the Home Office Correspondence Service (HOCS) case creator service.

- Reads a message containing a JSON payload from an AWS SQS Queue
- Validates the payload against a pre-described schema
- Creates a case with associated data (i.e. correspondents, complaint type)

## Getting Started

### Prerequisites

* ```Java 17```
* ```Docker```
* ```LocalStack```

### Submodules

This project contains a 'ci' submodule with a docker-compose and infrastructure scripts in it.
Most modern IDEs will handle pulling this automatically for you, but if not

```console
$ git submodule update --init --recursive
```

## Docker Compose

This repository contains a [Docker Compose](https://docs.docker.com/compose/)
file.

### Start localstack (sqs, sns, s3)
From the project root run:
```console
$ docker-compose -f ./ci/docker-compose.yml up -d localstack
```

> With Docker using 4 GB of memory, this takes approximately 2 minutes to startup.

### Stop the services
From the project root run:
```console
$ docker-compose -f ./ci/docker-compose.yml stop
```
> This will retain data in the local database and other volumes.

## Dockerfile

To build the dockerfile locally it needs access to github packages, this is done through your Personal Access Token 
run:
```
docker build . -t whatever --build-arg=PACKAGE_TOKEN=$MY_PAT
```

## Running in an IDE

> To be able to run the service in the environment you will need to ensure that both the [`hocs-ukvi-complaint-schema`](https://github.com/UKHomeOffice/hocs-ukvi-complaint-schema#publishing)
> and [`hocs-migration-schema`](https://github.com/UKHomeOffice/hocs-migration-schema#publishing) have been published to Maven Local with the versions specified within the [build.gradle](./build.gradle).

If you are using an IDE, such as IntelliJ, this service can be started by running the ```CaseCreatorApplication``` main class.
The service can then be accessed at ```http://localhost:8092```.

You need to specify appropriate Spring profiles.
Paste `development,local` into the "Active profiles" box of your run configuration.

## Configuration

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
| SERVER_PORT | The port the server listens on | 8092 | Yes

## JSON Schema

The service uses the JSON schema defined
in [hocs-ukvi-complaint-schema](https://github.com/UKHomeOffice/hocs-ukvi-complaint-schema). The schema is pulled into
the build as a dependency in the gradle file.

For local development, build the schema locally and publish
to a Maven local repository. View the README in `hocs-ukvi-complaint-schema` for more information.

## Versioning

For versioning this project uses [SemVer](https://semver.org/).

## Authors

This project is authored by the Home Office.

## License

This project is licensed under the MIT license. For details please see [License](LICENSE) 
