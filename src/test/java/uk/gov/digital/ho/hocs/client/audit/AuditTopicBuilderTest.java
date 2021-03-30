package uk.gov.digital.ho.hocs.client.audit;

import org.junit.Test;
import uk.gov.digital.ho.hocs.aws.LocalStackConfiguration;
import uk.gov.digital.ho.hocs.aws.SNSConfiguration;

import static junit.framework.TestCase.assertEquals;

public class AuditTopicBuilderTest {

    @Test
    public void shouldReturnCorrectTopicForLocalConfig() {

        LocalStackConfiguration localStackConfiguration = new LocalStackConfiguration();
        String topicName = "audit-topic";

        AuditTopicBuilder auditTopicBuilder = new AuditTopicBuilder(localStackConfiguration.topicPrefix(), topicName);

        assertEquals(String.format("aws-sns://%s?amazonSNSClient=#auditSnsClient", topicName), auditTopicBuilder.getTopic());
    }

    @Test
    public void shouldReturnCorrectTopicForSNSConfig() {

        SNSConfiguration localStackConfiguration = new SNSConfiguration();
        String region = "eu-west-2";
        String accountId = "12345";
        String topicName = "audit-topic";

        AuditTopicBuilder auditTopicBuilder = new AuditTopicBuilder(localStackConfiguration.topicPrefix(region, accountId), topicName);

        assertEquals(String.format("aws-sns://arn:aws:sns:%s:%s:%s?amazonSNSClient=#auditSnsClient", region, accountId, topicName), auditTopicBuilder.getTopic());
    }
}