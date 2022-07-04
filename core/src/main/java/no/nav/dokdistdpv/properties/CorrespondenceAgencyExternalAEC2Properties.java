package no.nav.dokdistdpv.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;


@Data
@ConfigurationProperties("altinn.correspondenceagencyexternalaec2")
@Validated
public class CorrespondenceAgencyExternalAEC2Properties {

    @NotEmpty
    private String endpointurl;
    @Min(1)
    private int readtimeoutms;
    @Min(1)
    private int connecttimeoutms;
}
