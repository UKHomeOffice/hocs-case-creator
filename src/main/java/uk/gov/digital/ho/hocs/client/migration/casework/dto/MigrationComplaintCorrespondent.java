package uk.gov.digital.ho.hocs.client.migration.casework.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import uk.gov.digital.ho.hocs.queue.complaints.CorrespondentType;

import javax.validation.constraints.NotEmpty;


@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class MigrationComplaintCorrespondent {

    @NonNull
    @NotEmpty
    @JsonProperty(value = "fullName", required = true)
    String fullname;

    @NonNull
    @NotEmpty
    @JsonProperty(value = "correspondentType", required = true)
    CorrespondentType type;

    @JsonProperty("telephone")
    String telephone;

    @JsonProperty("email")
    String email;

    @JsonProperty("organisation")
    String organisation;

    @JsonProperty("address1")
    String address1;

    @JsonProperty("address2")
    String address2;

    @JsonProperty("address3")
    String address3;

    @JsonProperty("postcode")
    String postcode;

    @JsonProperty("country")
    String country;

    @JsonProperty("reference")
    String reference;

    public MigrationComplaintCorrespondent(@NonNull @NotEmpty  String fullname, @NonNull @NotEmpty CorrespondentType type) {
        this.type = type;
        this.fullname = fullname;
    }
}
