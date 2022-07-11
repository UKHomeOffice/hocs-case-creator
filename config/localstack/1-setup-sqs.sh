#!/bin/bash
set -euxo pipefail

export AWS_ACCESS_KEY_ID=UNSET
export AWS_SECRET_ACCESS_KEY=UNSET
export AWS_DEFAULT_REGION=eu-west-2

## make sure that localstack is running in the pipeline
until curl http://localstack:4566/health --silent | grep -q "running"; do
   sleep 5
   echo "Waiting for LocalStack to be ready..."
done

aws sqs --endpoint-url=http://localstack:4566 create-queue --queue-name case-creator-queue-dlq
aws sqs --endpoint-url=http://localstack:4566 create-queue --queue-name case-creator-queue --attributes '{"RedrivePolicy": "{\"deadLetterTargetArn\":\"arn:aws:sqs:eu-west-2:000000000000:case-creator-queue-dlq\",\"maxReceiveCount\":1}"}'

aws sqs --endpoint-url=http://localstack:4566 create-queue --queue-name migration-queue-dlq
aws sqs --endpoint-url=http://localstack:4566 create-queue --queue-name migration-queue --attributes '{"RedrivePolicy": "{\"deadLetterTargetArn\":\"arn:aws:sqs:eu-west-2:000000000000:case-creator-queue-dlq\",\"maxReceiveCount\":1}"}'
