# hocs-case-creator

## Behaviour
- Reads a JSON payload from an AWS SQS queue
- Using Apache Camel
  - Validates the payload
  - Manages the Dead letter queue
  - Passes the JSON to the spring boot service
    


## Local development
There is a docker-compose file that will start localstack and add the required queues.
````
docker-compose up
````

There is also a small java client ``src/test/java/uk/gov/digital/ho/hocs/clientutil/SQSSender.java``
This can be used for sending messages to the local queue.
