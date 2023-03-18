package com.vanoma.api.order.utils;

import com.vanoma.api.utils.web.ILanguageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class LanguageUtils implements ILanguageUtils {

    @Autowired
    private MessageSource messageSource;

    private Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }

    public String getLocalizedMessage(String messageKey) {
        return messageSource.getMessage(messageKey, null, getLocale());
    }
}
