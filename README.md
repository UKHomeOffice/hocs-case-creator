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

There is also a small java client ``src/test/java/uk/gov/digital/ho/hocs/clientutil/SQSSender.java``
This can be used for sending messages to the local queue.
