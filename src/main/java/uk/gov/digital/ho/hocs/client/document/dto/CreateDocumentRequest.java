package uk.gov.digital.ho.hocs.client.document.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class CreateDocumentRequest {
    @JsonProperty("name")
    private String name;

    public CreateDocumentRequest(String name, String type, String fileLink, UUID externalReferenceUUID) {
        this.name = name;
        this.type = type;
        this.fileLink = fileLink;
        this.externalReferenceUUID = externalReferenceUUID;
    }

    @JsonProperty("type")
    private String type;

    @JsonProperty("fileLink")
    private String fileLink;

    @JsonProperty("externalReferenceUUID")
    private UUID externalReferenceUUID;

    @JsonProperty("actionDataItemUuid")
    private UUID actionDataItemUuid;
}
