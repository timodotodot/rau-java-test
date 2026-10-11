package com.example.raujavatest.support;

import com.example.raujavatest.domain.documents.Subscription;
import com.example.raujavatest.repositories.TariffRepository;
import org.jdbi.v3.core.Jdbi;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import su.onno.metadata.DocumentDescriptor;
import su.onno.metadata.MetadataRegistry;
import su.onno.numbering.NumberGenerator;
import su.onno.posting.PostingService;
import su.onno.security.SecretCipher;
import su.onno.ui.DocumentCommandService;
import su.onno.ui.DocumentQueryService;
import su.onno.ui.UiAccessService;
import su.onno.ui.UiProperties;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Primary
public class SubscriptionDocumentCommandService extends DocumentCommandService {
    private final TariffRepository tariffs;

    public SubscriptionDocumentCommandService(
            MetadataRegistry registry,
            Jdbi jdbi,
            UiProperties properties,
            NumberGenerator numberGenerator,
            PostingService postingService,
            DocumentQueryService documentQuery,
            UiAccessService access,
            ApplicationEventPublisher events,
            SecretCipher secretCipher,
            TariffRepository tariffs
    ) {
        super(registry, jdbi, properties, numberGenerator, postingService,
                documentQuery, access, events, secretCipher);
        this.tariffs = tariffs;
    }

    @Override
    public Map<String, Object> create(
            DocumentDescriptor descriptor,
            Map<String, Object> requestBody,
            Principal principal
    ) {
        return super.create(descriptor, normalize(descriptor, requestBody), principal);
    }

    @Override
    public Map<String, Object> update(
            DocumentDescriptor descriptor,
            UUID id,
            Map<String, Object> requestBody,
            Principal principal
    ) {
        return super.update(descriptor, id, normalize(descriptor, requestBody), principal);
    }

    @Override
    public Map<String, Object> validate(
            DocumentDescriptor descriptor,
            UUID id,
            Map<String, Object> requestBody,
            Principal principal
    ) {
        return super.validate(descriptor, id, normalize(descriptor, requestBody), principal);
    }

    Map<String, Object> normalize(DocumentDescriptor descriptor, Map<String, Object> requestBody) {
        if (!descriptor.javaClass().equals(Subscription.class)
                || !(requestBody.get("lines") instanceof List<?> submittedLines)) {
            return requestBody;
        }

        Map<String, Object> normalizedBody = new LinkedHashMap<>(requestBody);
        List<Map<String, Object>> normalizedLines = new ArrayList<>();

        for (Object submittedLine : submittedLines) {
            if (!(submittedLine instanceof Map<?, ?> values)) {
                continue;
            }

            Map<String, Object> line = copy(values);
            tariffId(line.get("tariff"))
                    .flatMap(tariffs::findById)
                    .ifPresent(tariff -> {
                        BigDecimal price = tariff.getPricePerPeriod() == null
                                ? BigDecimal.ZERO
                                : tariff.getPricePerPeriod();
                        int periods = positiveInt(line.get("periods"));
                        line.put("price", price);
                        line.put("amount", price.multiply(BigDecimal.valueOf(periods)));
                    });
            normalizedLines.add(line);
        }

        normalizedBody.put("lines", normalizedLines);
        return normalizedBody;
    }

    private static Map<String, Object> copy(Map<?, ?> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        source.forEach((key, value) -> result.put(String.valueOf(key), value));
        return result;
    }

    private static java.util.Optional<UUID> tariffId(Object value) {
        try {
            return value == null
                    ? java.util.Optional.empty()
                    : java.util.Optional.of(value instanceof UUID id
                    ? id
                    : UUID.fromString(value.toString()));
        } catch (IllegalArgumentException ignored) {
            return java.util.Optional.empty();
        }
    }

    private static int positiveInt(Object value) {
        try {
            int parsed = value instanceof Number number
                    ? number.intValue()
                    : Integer.parseInt(String.valueOf(value));
            return Math.max(parsed, 0);
        } catch (RuntimeException ignored) {
            return 0;
        }
    }
}
