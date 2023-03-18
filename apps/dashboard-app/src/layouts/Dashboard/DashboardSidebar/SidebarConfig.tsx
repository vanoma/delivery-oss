import InsightsIcon from '@mui/icons-material/Insights';
import { SvgIconTypeMap } from '@mui/material';
import { OverridableComponent } from '@mui/material/OverridableComponent';
import PersonOutlineOutlinedIcon from '@mui/icons-material/PersonOutlineOutlined';
import PaymentIcon from '@mui/icons-material/Payment';
import TwoWheelerIcon from '@mui/icons-material/TwoWheeler';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import i18n from '../../../locales/i18n';
import {
    ACCOUNT,
    BILLING,
    CUSTOMERS,
    DELIVERIES,
    OVERVIEW,
} from '../../../routeNames';

export interface SidebarConfigType {
    title: string;
    path: string;
    Icon: OverridableComponent<SvgIconTypeMap<{}, 'svg'>>;
}

const sidebarConfig = (): SidebarConfigType[] => [
    {
        title: i18n.t('dashboard.sidebar.overview'),
        path: OVERVIEW,
        Icon: InsightsIcon,
    },
    {
        title: i18n.t('dashboard.sidebar.deliveries'),
        path: `${DELIVERIES}/:tab`,
        Icon: TwoWheelerIcon,
    },
    {
        title: i18n.t('dashboard.sidebar.customers'),
        path: CUSTOMERS,
        Icon: PersonOutlineOutlinedIcon,
    },
    {
        title: i18n.t('dashboard.sidebar.billing'),
        path: `${BILLING}/:tab`,
        Icon: PaymentIcon,
    },
    {
        title: i18n.t('dashboard.sidebar.account'),
        path: `${ACCOUNT}/:tab`,
        Icon: AccountCircleIcon,
    },
];

export default sidebarConfig;
