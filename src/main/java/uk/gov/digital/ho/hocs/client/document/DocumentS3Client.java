package uk.gov.digital.ho.hocs.client.document;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.SSEAwsKeyManagementParams;
import com.amazonaws.util.StringInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.application.properties.AwsS3Properties;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

@Service
@Slf4j
public class DocumentS3Client {

    private static final String META_DATA_LABEL = "originalName";

    private final AmazonS3 s3Client;
    private final AwsS3Properties awsS3Properties;

    public DocumentS3Client(AmazonS3 s3Client,
                            AwsS3Properties awsS3Properties) {
        this.s3Client = s3Client;
        this.awsS3Properties = awsS3Properties;
    }

    public String storeUntrustedDocument(String originalFilename, String formattedDocument) {
        ObjectMetadata metaData = buildObjectMetadata(originalFilename, formattedDocument);
        String tempObjectName = getTempObjectName();
        try {
            PutObjectRequest uploadRequest = buildPutObjectRequest(formattedDocument, metaData, tempObjectName);
            s3Client.putObject(uploadRequest);
        } catch (UnsupportedEncodingException e) {
            // Unless this code changes, this should never happen
            log.error(e.getMessage());
        }
        return tempObjectName;
    }

    PutObjectRequest buildPutObjectRequest(String formattedDocument, ObjectMetadata metaData, String tempObjectName) throws UnsupportedEncodingException {
        PutObjectRequest uploadRequest = new PutObjectRequest(awsS3Properties.getUntrusted().getBucketName(), tempObjectName,
                new StringInputStream(formattedDocument), metaData);

        String untrustedKmsKey = awsS3Properties.getUntrusted().getAccount().getBucketKmsKey();

        if (StringUtils.hasText(untrustedKmsKey)) { // Will be empty when running local. Workaround because localstack doesn't use HTTPS
            uploadRequest = uploadRequest.withSSEAwsKeyManagementParams(new SSEAwsKeyManagementParams(untrustedKmsKey));
        }

        return uploadRequest;
    }

    String getTempObjectName() {
        return UUID.randomUUID().toString();
    }

    ObjectMetadata buildObjectMetadata(String originalFilename, String formattedDocument) {
        ObjectMetadata metaData = new ObjectMetadata();
        metaData.setContentType("application/octet-stream");
        metaData.addUserMetadata(META_DATA_LABEL, originalFilename);
        metaData.setContentLength(formattedDocument.length());
        return metaData;
    }
}
