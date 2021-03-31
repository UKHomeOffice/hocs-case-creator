package uk.gov.digital.ho.hocs.client.document;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.UnsupportedEncodingException;

import static junit.framework.TestCase.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class DocumentS3ClientTest {

    @Mock
    private AmazonS3 s3Client;

    private DocumentS3Client documentS3Client;
    private String payload;
    private String fileName;
    private String bucketName;

    @Before
    public void setUp() {
        payload = "{json}";
        fileName = "text.txt";
        bucketName = "bucket";
        documentS3Client = new DocumentS3Client(s3Client, bucketName, "");
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
        assertEquals(bucketName, putObjectRequest.getBucketName());
        assertEquals(tempName, putObjectRequest.getKey());
        assertNull(putObjectRequest.getSSEAwsKeyManagementParams());
    }

    @Test
    public void shouldBuildAPutRequestWithKMS() throws UnsupportedEncodingException {
        String kmsKey = "kmsKey";
        String tempName = "1234";
        documentS3Client = new DocumentS3Client(s3Client, bucketName, kmsKey);
        ObjectMetadata objectMetadata = documentS3Client.buildObjectMetadata(fileName, payload);
        PutObjectRequest putObjectRequest = documentS3Client.buildPutObjectRequest(payload, objectMetadata, tempName);
        assertNotNull(putObjectRequest.getSSEAwsKeyManagementParams());
    }
}