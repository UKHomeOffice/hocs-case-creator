package uk.gov.digital.ho.hocs.clientutil;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

/**
 * Utility to add messages to a queue, change as required.
 */
public class SQSSender {

    public static void main(String[] args) {
        AmazonSQS sqs = sqsClient();
        String QUEUE_NAME = "ukvi-complaint-queue";
        String queueUrl = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();
        System.out.println(queueUrl);

        SendMessageRequest send_msg_request = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody("{\n" +
                        "  \"creationDate\": \"2021-03-03\",\n" +
                        "  \"complaint\": {\n" +
                        "    \"complaintType\": \"POOR_STAFF_BEHAVIOUR\",\n" +
                        "    \"reference\": {\n" +
                        "      \"referenceType\": \"IHS_REF\",\n" +
                        "      \"reference\": \"amet\"\n" +
                        "    },\n" +
                        "    \"reporterDetails\": {\n" +
                        "      \"applicantType\": \"AGENT\",\n" +
                        "      \"applicantDetails\": {\n" +
                        "        \"applicantName\": \"Ken Dodd\",\n" +
                        "        \"applicantNationality\": \"Lorem\",\n" +
                        "        \"applicantDob\": \"1989-08-23\"\n" +
                        "      },\n" +
                        "      \"agentDetails\": {\n" +
                        "        \"agentName\": \"id\",\n" +
                        "        \"agentType\": \"RELATIVE\",\n" +
                        "        \"agentEmail\": \"cupidatat in\"\n" +
                        "      }\n" +
                        "    },\n" +
                        "    \"complaintDetails\": {\n" +
                        "      \"complaintText\": \"officia laborum dolore\",\n" +
                        "      \"experience\": {\n" +
                        "        \"experienceType\": \"FACE_TO_FACE\",\n" +
                        "        \"location\": {\n" +
                        "          \"country\": \"dolore consequat\",\n" +
                        "          \"city\": \"sit aute incididunt mollit\",\n" +
                        "          \"centreType\": \"VAC\"\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }\n" +
                        "}\n")
                .withDelaySeconds(2);

        SendMessageResult sendMessageResult = sqs.sendMessage(send_msg_request);

        System.out.println(sendMessageResult);

    }

    static AmazonSQS sqsClient() {

        String host = String.format("http://%s:4576/", "localhost");

        AWSCredentialsProvider awsCredentialsProvider = new AWSCredentialsProvider() {
            @Override
            public AWSCredentials getCredentials() {
                return new BasicAWSCredentials("test", "test");
            }

            @Override
            public void refresh() {
            }
        };

        AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration(host, "eu-west-2");

        return AmazonSQSClientBuilder.standard()
                .withClientConfiguration(new ClientConfiguration().withProtocol(Protocol.HTTP))
                .withCredentials(awsCredentialsProvider)
                .withEndpointConfiguration(endpoint)
                .build();
    }


}
