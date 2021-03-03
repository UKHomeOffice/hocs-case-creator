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
                        "  \"creationDate\": \"1949-04-07\",\n" +
                        "  \"complaint\": {\n" +
                        "    \"complaintType\": \"MAKING_APPOINTMENT\",\n" +
                        "    \"reference\": {\n" +
                        "      \"referenceType\": \"GWF_REF\",\n" +
                        "      \"reference\": \"nostrud labore do\"\n" +
                        "    },\n" +
                        "    \"reporterDetails\": {\n" +
                        "      \"applicantType\": \"APPLICANT\",\n" +
                        "      \"applicantName\": \"dolore\",\n" +
                        "      \"applicantNationality\": \"Botswana\",\n" +
                        "      \"applicantDob\": \"1974-03-05\",\n" +
                        "      \"applicantEmail\": \"sss@uevptde.com\",\n" +
                        "      \"applicantPhone\": \"01772 700806\"\n" +
                        "    },\n" +
                        "    \"complaintDetails\": {\n" +
                        "      \"complaintText\": \"tempor aute pariatur aliquip esse\",\n" +
                        "      \"problemExperienced\": \"TECHNICAL_APPOINTMENTS\",\n" +
                        "      \"applicationLocation\": \"INSIDE_UK\"\n" +
                        "    }\n" +
                        "  }\n" +
                        "}\n")
                .withDelaySeconds(5);

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
