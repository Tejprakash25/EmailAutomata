package com.emailautomata.core.web;

import com.emailautomata.core.config.AppProperties;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

/**
 * Service identity endpoint.
 *
 * <p>Distinct from {@code /actuator/health}: actuator answers "is the process
 * alive", this answers "which build of which product am I talking to". The
 * client uses it to confirm end-to-end wiring on boot.</p>
 */
@RestController
@RequestMapping("/api/v1/meta")
public class MetaController {

    private static final String PRODUCT_NAME = "EmailAutomata";
    private static final String TAGLINE = "Compose. Schedule. Account for every send.";

    private final AppProperties properties;
    private final Environment environment;

    public MetaController(AppProperties properties, Environment environment) {
        this.properties = properties;
        this.environment = environment;
    }

    @GetMapping
    public ApiResponse<MetaResponse> meta() {
        String activeProfile = environment.getActiveProfiles().length > 0
                ? environment.getActiveProfiles()[0]
                : "default";

        return ApiResponse.ok(new MetaResponse(
                PRODUCT_NAME,
                TAGLINE,
                properties.apiVersion(),
                properties.buildVersion(),
                activeProfile,
                OffsetDateTime.now()
        ));
    }
}
