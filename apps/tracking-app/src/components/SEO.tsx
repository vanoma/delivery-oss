import React from 'react';
import { PageProps, useStaticQuery, graphql } from 'gatsby';
import { Helmet } from 'react-helmet';

const SEO: React.FC<{
    title: string;
    pageProps: PageProps;
    description?: string;
    image?: string;
    article?: boolean;
}> = ({ title, pageProps, description, image, article }) => {
    // eslint-disable-next-line no-undef
    const { site } = useStaticQuery<GatsbyTypes.SEOQuery>(graphql`
        query {
            site {
                siteMetadata {
                    titleTemplate
                    defaultDescription: description
                    siteUrl: url
                    defaultImage: image
                    twitterUsername
                }
            }
        }
    `);

    const {
        titleTemplate,
        defaultDescription,
        siteUrl,
        defaultImage,
        twitterUsername,
    } = site!.siteMetadata;

    const seo = {
        title,
        description: description ?? defaultDescription,
        image: `${siteUrl}${image ?? defaultImage}`,
        url: `${siteUrl}${pageProps.location.pathname}`,
    };

    return (
        <Helmet title={seo.title} titleTemplate={titleTemplate}>
            <meta name="description" content={seo.description} />
            <meta name="image" content={seo.image} />

            {seo.url && <meta property="og:url" content={seo.url} />}

            {(article ? true : null) && (
                <meta property="og:type" content="article" />
            )}

            {seo.title && <meta property="og:title" content={seo.title} />}

            {seo.description && (
                <meta property="og:description" content={seo.description} />
            )}

            {seo.image && <meta property="og:image" content={seo.image} />}

            <meta name="twitter:card" content="summary_large_image" />

            {twitterUsername && (
                <meta name="twitter:creator" content={twitterUsername} />
            )}

            {seo.title && <meta name="twitter:title" content={seo.title} />}

            {seo.description && (
                <meta name="twitter:description" content={seo.description} />
            )}

            {seo.image && <meta name="twitter:image" content={seo.image} />}
        </Helmet>
    );
};

export default SEO;
