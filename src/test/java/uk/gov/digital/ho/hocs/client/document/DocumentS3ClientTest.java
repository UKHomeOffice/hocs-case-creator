package uk.gov.digital.ho.hocs.client.document;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.UnsupportedEncodingException;

import static junit.framework.TestCase.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("local")
public class DocumentS3ClientTest {

    @Autowired
    private AmazonS3 s3Client;

    @Value("${aws.s3.untrusted.bucket-name}")
    private String bucketName;

    private DocumentS3Client documentS3Client;
    private String payload;
    private String fileName;

    @Before
    public void setUp() {
        payload = "{json}";
        fileName = "text.txt";
        documentS3Client = new DocumentS3Client(s3Client, bucketName, "");
    }

    @Test
    public void shouldStoreADocument() {
        var resultFileUuid = documentS3Client.storeUntrustedDocument(fileName, payload);

        var file = s3Client.getObject(bucketName, resultFileUuid);

        assertEquals(file.getObjectMetadata().getContentLength(), payload.length());
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
        assertEquals(bucketName, putObjectRequest.getBucketName());
        assertEquals(tempName, putObjectRequest.getKey());
        assertNull(putObjectRequest.getSSEAwsKeyManagementParams());
    }

    @Test
    public void shouldBuildAPutRequestWithKMS() throws UnsupportedEncodingException {
        String tempName = "1234";
        documentS3Client = new DocumentS3Client(s3Client, bucketName, "TEST");
        ObjectMetadata objectMetadata = documentS3Client.buildObjectMetadata(fileName, payload);
        PutObjectRequest putObjectRequest = documentS3Client.buildPutObjectRequest(payload, objectMetadata, tempName);
        assertNotNull(putObjectRequest.getSSEAwsKeyManagementParams());
    }
}
