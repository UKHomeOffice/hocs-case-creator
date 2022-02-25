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
        String QUEUE_NAME = "case-creator-queue";
        String queueUrl = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();
        System.out.println(queueUrl);

        SendMessageRequest send_msg_request = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody("{\n" +
                        "  \"creationDate\": \"2021-05-10\",\n" +
                        "  \"complaint\": {\n" +
                        "    \"complaintType\": \"POOR_STAFF_BEHAVIOUR\",\n" +
                        "    \"reference\": {\n" +
                        "      \"referenceType\": \"IHS_REF\",\n" +
                        "      \"reference\": \"ABC12345\"\n" +
                        "    },\n" +
                        "    \"reporterDetails\": {\n" +
                        "      \"applicantType\": \"AGENT\",\n" +
                        "      \"applicantDetails\": {\n" +
                        "        \"applicantName\": \"Jack White\",\n" +
                        "        \"applicantNationality\": \"Lorem\",\n" +
                        "        \"applicantDob\": \"1989-08-23\"\n" +
                        "      },\n" +
                        "      \"agentDetails\": {\n" +
                        "        \"agentName\": \"Peter Jones\",\n" +
                        "        \"agentType\": \"RELATIVE\",\n" +
                        "        \"agentEmail\": \"peterJ@gmailcom\"\n" +
                        "      }\n" +
                        "    },\n" +
                        "    \"complaintDetails\": {\n" +
                        "      \"complaintText\": \"Lorem ipsum dolor sit amet, \\\"consectetur adipiscing elit, sed do eiusmod tempor incididunt ut \\\"labore et dolore magna aliqua. Facilisis magna etiam tempor orci eu lobortis elementum nibh tellus. Diam maecenas ultricies mi eget mauris pharetra et ultrices. Cras fermentum odio eu feugiat pretium nibh. Ut ornare lectus sit amet est. Gravida neque convallis a cras semper. In hac habitasse platea dictumst vestibulum rhoncus est pellentesque elit. Ipsum a arcu cursus vitae congue mauris rhoncus aenean. Feugiat in ante metus dictum at tempor. Ipsum dolor sit amet consectetur adipiscing elit pellentesque habitant. Vel risus commodo viverra maecenas. Sed vulputate mi sit amet mauris commodo quis imperdiet. Magna sit amet purus gravida.\",\n" +
                        "      \"experience\": {\n" +
                        "        \"experienceType\": \"FACE_TO_FACE\",\n" +
                        "        \"location\": {\n" +
                        "          \"country\": \"United Kingdom\",\n" +
                        "          \"city\": \"Dover\",\n" +
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
