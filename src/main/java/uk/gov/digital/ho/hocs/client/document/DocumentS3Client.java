package uk.gov.digital.ho.hocs.client.document;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.SSEAwsKeyManagementParams;
import com.amazonaws.util.StringInputStream;
import com.amazonaws.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

@Service
@Slf4j
public class DocumentS3Client {

    private final String untrustedS3BucketName;
    private final AmazonS3 s3Client;
    private static final String META_DATA_LABEL = "originalName";
    private final String untrustedBucketKMSKey;

    public DocumentS3Client(AmazonS3 s3Client,
                            @Value("${document.s3.untrusted-bucket-name}") String untrustedBucketName,
                            @Value("${document.s3.untrusted-bucket-kms-key}") String untrustedBucketKMSKey) {
        this.s3Client = s3Client;
        this.untrustedS3BucketName = untrustedBucketName;
        this.untrustedBucketKMSKey = untrustedBucketKMSKey;
    }

    public String storeUntrustedDocument(String originalFilename, String formattedDocument) {
        String tempObjectName = UUID.randomUUID().toString();
        ObjectMetadata metaData = new ObjectMetadata();
        metaData.setContentType("application/octet-stream");
        metaData.addUserMetadata(META_DATA_LABEL, originalFilename);
        metaData.setContentLength(formattedDocument.length());

        try {
            PutObjectRequest uploadRequest = new PutObjectRequest(untrustedS3BucketName, tempObjectName, new StringInputStream(formattedDocument), metaData);

            if (StringUtils.hasValue(untrustedBucketKMSKey)) { // Will be empty when running local. Workaround because localstack doesn't use HTTPS
                uploadRequest = uploadRequest.withSSEAwsKeyManagementParams(new SSEAwsKeyManagementParams(untrustedBucketKMSKey));
            }

            s3Client.putObject(uploadRequest);

        } catch (UnsupportedEncodingException e) {
            // Unless this code changes, this should never happen
            log.error(e.getMessage());
        }
        return tempObjectName;
    }
}
