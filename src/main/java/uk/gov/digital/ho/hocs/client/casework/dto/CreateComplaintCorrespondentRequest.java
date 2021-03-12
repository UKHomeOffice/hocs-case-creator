package uk.gov.digital.ho.hocs.client.casework.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotEmpty;


@Getter
@Setter
@EqualsAndHashCode
public class CreateComplaintCorrespondentRequest {

    @NonNull
    @JsonProperty(value = "type", required = true)
    String type;

    @NonNull
    @NotEmpty
    @JsonProperty(value = "fullname", required = true)
    String fullname;

    @JsonProperty("telephone")
    String telephone;

    @JsonProperty("email")
    String email;

    public CreateComplaintCorrespondentRequest(@NonNull @NotEmpty String fullname) {
        this.fullname = fullname;
        this.type = "COMPLAINT";
    }

}
