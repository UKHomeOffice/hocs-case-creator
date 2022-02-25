#!/usr/bin/env sh

aws --endpoint-url=http://localhost:4566 sqs send-message --queue-url http://localhost:4566/queue/case-creator-queue --message-body "test" --delay-seconds 10
