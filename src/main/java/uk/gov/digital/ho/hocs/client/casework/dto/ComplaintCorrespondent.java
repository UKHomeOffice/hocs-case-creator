package uk.gov.digital.ho.hocs.client.casework.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import uk.gov.digital.ho.hocs.domain.queue.complaints.CorrespondentType;

import javax.validation.constraints.NotEmpty;


@Getter
@Setter
@EqualsAndHashCode
public class ComplaintCorrespondent {

    @NonNull
    @JsonProperty(value = "type", required = true)
    CorrespondentType type;

    @NonNull
    @NotEmpty
    @JsonProperty(value = "fullname", required = true)
    String fullname;

    @JsonProperty("telephone")
    String telephone;

    @JsonProperty("email")
    String email;

    public ComplaintCorrespondent(@NonNull @NotEmpty String fullname, @NonNull @NotEmpty CorrespondentType type) {
        this.fullname = fullname;
        this.type = type;
    }

}
