import {
    Alert,
    //  Link,
} from '@mui/material';
import moment from 'moment';
import React from 'react';
import { useTranslation } from 'react-i18next';
// import { useSelector } from 'react-redux';
// import { selectCustomerId } from '../redux/slices/authenticationSlice';

const translatedAnnouncement = {
    en: 'Due to umuganda, today we will open at',
    fr: "En raison de l'umuganda, nous ouvrirons à",
    rw: 'Kubera umuganda, turatangira gukora',
};

// const translatedAnnouncement = {
//     en: 'On September 5th, we will switch to 2-Hour delivery. ',
//     fr: 'Le 5 septembre, nous passerons à la livraison en 2 heures. ',
//     rw: "Ku ya 5 z'ukwacyenda, delivery izajya imara amasaha abiri.",
// };

// const link = {
//     en: 'Read more here.',
//     fr: 'En savoir plus ici.',
//     rw: 'Soma hano.',
// };

const Announcement: React.FC = () => {
    const { i18n } = useTranslation();
    // const customerId = useSelector(selectCustomerId);
    // Show this new working hours until July 30th 11:29 AM
    const isShowing = moment.utc().isBefore(moment('2022-10-29T09:29:59.000Z'));

    const language = i18n.language as 'en' | 'fr' | 'rw';

    // const exclusives = [
    //     'ebfb2198f9f0479cba67c630705fdba6',
    //     'eec3f1a97676471dba98a119aaeae63d',
    // ];

    return isShowing ? (
        //  && customerId && !exclusives.includes(customerId)
        <Alert variant="filled" severity="info">
            {`${translatedAnnouncement[language]} `}
            <span style={{ fontWeight: 'bold' }}>11:30 AM.</span>
            {/* <Link
                href="https://vanoma.com/blog/announcement-switching-to-two-hour-delivery/"
                target="_blank"
                rel="noreferrer"
            >
                {link[language]}
            </Link> */}
        </Alert>
    ) : (
        <></>
    );
};

export default Announcement;
