import React from 'react';
import { useI18next, useTranslation } from 'gatsby-plugin-react-i18next';
import { PageProps } from 'gatsby';
import { LanguagePopoverBase } from '@vanoma/ui-components';
import LanguageMenuItem from './LanguageMenuItem';

const LanguagePopover: React.FC<{ pageProps: PageProps }> = ({ pageProps }) => {
    const { t } = useTranslation();
    const { language, originalPath } = useI18next();
    const { location } = pageProps;

    return (
        <LanguagePopoverBase
            renderItem={({ value, icon, label }) => (
                <LanguageMenuItem
                    key={value}
                    value={value}
                    label={label}
                    icon={icon}
                    to={`${originalPath}${location.search}`}
                    language={language}
                />
            )}
            language={language}
            tooltipText={t('languages')}
        />
    );
};

export default LanguagePopover;
