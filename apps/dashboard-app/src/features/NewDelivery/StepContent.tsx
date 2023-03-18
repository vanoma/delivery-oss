import { Collapse, styled } from '@mui/material';

const StepContent = styled(Collapse)(({ theme }) => ({
    marginTop: theme.spacing(2),
    marginBottom: theme.spacing(1),
    paddingLeft: theme.spacing(5.75),
    [theme.breakpoints.down('sm')]: {
        paddingLeft: theme.spacing(4.25),
    },
}));

export default StepContent;
