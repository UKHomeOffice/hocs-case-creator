package uk.gov.digital.ho.hocs.application.properties.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class Account {

    @NotBlank
    private String accessKey;

    @NotBlank
    private String secretKey;

}
