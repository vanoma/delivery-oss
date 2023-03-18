package com.vanoma.api.order.utils;

import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Borrowed idea from <a href="https://www.baeldung.com/rest-api-search-language-spring-data-specifications">baeldung</a>.
 * @param <T> {@link javax.persistence.Entity}
 */
public class SpecificationBuilder<T> {
    private final List<Specification<T>> specs;

    public SpecificationBuilder() {
        specs = new ArrayList<>();
    }

    public SpecificationBuilder<T> add(Specification<T> spec) {
        specs.add(spec);
        return this;
    }

    public Specification<T> build() {
        if (specs.size() == 0) {
            return null;
        }

        Specification<T> result = specs.get(0);

        for (int i = 1; i < specs.size(); i++) {
            result = Specification.where(result).and(specs.get(i));
        }

        return result;
    }

    public static <T> SpecificationBuilder<T> builder() {
        return new SpecificationBuilder<>();
    }
}
