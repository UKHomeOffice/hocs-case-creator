package uk.gov.digital.ho.hocs.client.document;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.application.properties.AwsS3Properties;

import java.io.UnsupportedEncodingException;

import static junit.framework.TestCase.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("local")
public class DocumentS3ClientTest {

    @MockBean
    private AmazonS3 s3Client;

    @Autowired
    private AwsS3Properties awsS3Properties;

    private DocumentS3Client documentS3Client;
    private String payload;
    private String fileName;

    @Before
    public void setUp() {
        payload = "{json}";
        fileName = "text.txt";
        documentS3Client = new DocumentS3Client(s3Client, awsS3Properties);
    }

    @Test
    public void shouldStoreADocument() {
        documentS3Client.storeUntrustedDocument(fileName, payload);

        verify(s3Client).putObject(any(PutObjectRequest.class));
    }

    @Test
    public void shouldBuildMetaData() {
        ObjectMetadata objectMetadata = documentS3Client.buildObjectMetadata(fileName, payload);
        assertEquals(6, objectMetadata.getContentLength());
        assertEquals("application/octet-stream", objectMetadata.getContentType());
        assertEquals(fileName, objectMetadata.getUserMetadata().get("originalName"));
    }

    @Test
    public void shouldBuildAPutRequestWithoutKMS() throws UnsupportedEncodingException {
        String tempName = "1234";
        ObjectMetadata objectMetadata = documentS3Client.buildObjectMetadata(fileName, payload);
        PutObjectRequest putObjectRequest = documentS3Client.buildPutObjectRequest(payload, objectMetadata, tempName);
        assertEquals(awsS3Properties.getUntrusted().getBucketName(), putObjectRequest.getBucketName());
        assertEquals(tempName, putObjectRequest.getKey());
        assertNull(putObjectRequest.getSSEAwsKeyManagementParams());
    }

    @Test
    public void shouldBuildAPutRequestWithKMS() throws UnsupportedEncodingException {
        var oldValue = awsS3Properties.getUntrusted().getAccount().getBucketKmsKey();
        awsS3Properties.getUntrusted().getAccount().setBucketKmsKey("kmsKey");

        String tempName = "1234";
        documentS3Client = new DocumentS3Client(s3Client, awsS3Properties);
        ObjectMetadata objectMetadata = documentS3Client.buildObjectMetadata(fileName, payload);
        PutObjectRequest putObjectRequest = documentS3Client.buildPutObjectRequest(payload, objectMetadata, tempName);
        assertNotNull(putObjectRequest.getSSEAwsKeyManagementParams());

        awsS3Properties.getUntrusted().getAccount().setBucketKmsKey(oldValue);
    }
}
