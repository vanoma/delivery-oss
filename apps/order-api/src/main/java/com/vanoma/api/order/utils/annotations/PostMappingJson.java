package com.vanoma.api.order.utils.annotations;

import org.springframework.core.annotation.AliasFor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping(method = RequestMethod.POST, consumes= MediaType.APPLICATION_JSON_VALUE)
public @interface PostMappingJson {
    @AliasFor(annotation = RequestMapping.class)
    String[] value() default {};

    @AliasFor(annotation = RequestMapping.class)
    String[] path() default {};
}
