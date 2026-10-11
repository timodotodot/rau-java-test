package com.example.raujavatest.support;

import org.springframework.stereotype.Component;
import su.onno.types.Ref;
import su.onno.types.RefResolver;

import java.util.Optional;

@Component
public final class DomainReferenceResolver {
    private static volatile RefResolver resolver;

    public DomainReferenceResolver(RefResolver resolver) {
        DomainReferenceResolver.resolver = resolver;
    }

    public static <T> Optional<T> resolve(Ref<T> reference) {
        if (reference == null || resolver == null) {
            return Optional.empty();
        }
        return resolver.resolve(reference);
    }

    public static void useForTests(RefResolver testResolver) {
        resolver = testResolver;
    }
}
