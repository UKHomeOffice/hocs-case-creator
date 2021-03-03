#!/usr/bin/env sh

aws --endpoint-url=http://localhost:4561 sqs send-message --queue-url http://localhost:4576/queue/ukvi-complaint-queue --message-body "test" --delay-seconds 10 
