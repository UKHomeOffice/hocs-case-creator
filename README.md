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

### Start localstack (sqs, s3)
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

To build the dockerfile locally it needs access to GitHub packages, this is done through your Personal Access Token 
run:
```
docker build . -t whatever --build-arg=PACKAGE_TOKEN=$MY_PAT
```

## Running in an IDE

> To be able to run the service in the environment you will need to ensure that both schemas have been published to 
> either Maven Local or Artifactory with the versions specified within the [build.gradle](./build.gradle).
> 
> See [JSON Schemas](#json-schemas) for more information.

If you are using an IDE, such as IntelliJ, this service can be started by running the `CaseCreatorApplication` main class.
The service can then be accessed at `http://localhost:8092`.

### Profiles

There are a number of Spring profiles that can be used to configure the service.

#### Core

| Profile     | Description                                     | Required                  |
|-------------|-------------------------------------------------|---------------------------|
| local       | Used to run the service locally                 | Yes for local development |
| development | Used to enable further logging                  | Yes for local development |

#### Message Profiles

| Profile     | Description                                     |
|-------------|-------------------------------------------------|
| ukvi        | Used to listen and handle UKVI messages         |
| migration   | Used to listen and enable migration messages    |

The service is designed that you should only run one message profile at a time, this ensures the service works just like in production.

## Configuration

The following table contains the mandatory and optional properties that need to be set for deployment.

| Property                                    | Description                                | Example                                        | Mandatory |
|---------------------------------------------|--------------------------------------------|------------------------------------------------|-----------|
| AWS_SQS_CONFIG_REGION                       | The region for the AWS SQS queue           | eu-west-2                                      | Yes       |
| AWS_SQS_CASE_CREATOR_URL                    | The full AWS SQS queue URL                 | http://localhost:4566/queue/case-creator-queue | Yes       |
| AWS_SQS_CASE_CREATOR_ACCOUNT_ACCESS_KEY     | The SQS access key                         | 12345                                          | Yes       |
| AWS_SQS_CASE_CREATOR_ACCOUNT_SECRET_KEY     | The SQS secret key                         | 12345                                          | Yes       |
| AWS_SQS_CASE_CREATOR_ATTRIBUTE_MAX_MESSAGES | The amount of messages to read at one time | 10                                             | No        |
| AWS_SQS_CASE_CREATOR_ATTRIBUTE_WAIT_TIME    | The poll time for the queue listener       | 5                                              | No        |
| AWS_SQS_IGNORE_MESSAGES                     | The messages to ignore                     | false/true                                     | No        |
| AWS_S3_CONFIG_REGION                        | The region for the AWS S3 queue            | eu-west-2                                      | Yes       |
| AWS_S3_UNTRUSTED_ACCOUNT_ACCESS_KEY         | The S3 access key                          | 12345                                          | Yes       |
| AWS_S3_UNTRUSTED_ACCOUNT_SECRET_KEY         | The S3 secret key                          | 12345                                          | Yes       |
| AWS_S3_UNTRUSTED_ACCOUNT_BUCKET_KMS_KEY     | The S3 bucket kms key                      | [UNSET]                                        | Yes       |
| AWS_S3_UNTRUSTED_BUCKET_NAME                | The S3 bucket name to send to              | untrusted-bucket                               | Yes       |
| CASE_CREATOR_WORKFLOW_SERVICE               | The URL of the workflow service            | http://localhost:8091                          | Yes       |
| CASE_CREATOR_CASE_SERVICE                   | The URL of the casework service            | http://localhost:8082                          | Yes       |
| CASE_CREATOR_IDENTITY_USER                  | The User ID                                | UUID                                           | Yes       |
| CASE_CREATOR_IDENTITY_GROUP                 | The Group ID                               | /TEAM_UUID_GROUP                               | Yes       |
| CASE_CREATOR_IDENTITY_TEAM                  | The Team ID                                | UUID                                           | Yes       |
| SERVER_PORT                                 | The port the server listens on             | 8092                                           | Yes       |

## JSON Schemas

The service uses the associated JSON schema to validate the incoming message.

- [hocs-ukvi-complaint-schema](https://github.com/UKHomeOffice/hocs-ukvi-complaint-schema)
- [hocs-migration-schema](https://github.com/UKHomeOffice/hocs-migration-schema)

These are loaded in at built time through either Maven local or GitHub Packages.

For GitHub Packages, you will need to generate a Personal Access Token and either set it as an environment variable 
or pass it in as a build argument. See [build.gradle](./build.gradle) for more information.

For local development, build the schema locally and publish to a Maven local repository. View the README in the schema 
for more information.

## Versioning

For versioning this project uses [SemVer](https://semver.org/).

## Authors

This project is authored by the Home Office.

## License

This project is licensed under the MIT license. For details please see [License](LICENSE) 
