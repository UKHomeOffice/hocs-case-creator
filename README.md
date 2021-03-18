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

## Local development
This service has a dependency on the workflow and casework services. 
These should be started using the docker-compose file in the frontend project. 

## Local testing

### Localstack and queues
There is a docker-compose file that will start localstack and add the required queues.
````console
$ cd localdev
$ docker-compose up
````

There is also a small java client `src/test/java/uk/gov/digital/ho/hocs/clientutil/SQSSender.java`.
This can be used for sending messages to the local queue.

## JSON Schema
The service uses the JSON schema defined in [hocs-ukvi-complaint-schema](https://github.com/UKHomeOffice/hocs-ukvi-complaint-schema).
The schema is pulled into the build by a task which the gradle assemble jobs depends on. 
See the `build.gradle` file for more information.
