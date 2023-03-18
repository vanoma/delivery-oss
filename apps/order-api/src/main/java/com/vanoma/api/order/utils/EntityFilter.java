package com.vanoma.api.order.utils;

import org.springframework.data.jpa.domain.Specification;

public interface EntityFilter<T> {
    boolean isEmpty();
    Specification<T> getSpec();
}
