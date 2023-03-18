import { styled } from '@mui/material/styles';
import { ListItem } from '@mui/material';

const SelectedInfo = styled(ListItem)(({ theme }) => ({
    backgroundColor:
        theme.palette.mode === 'dark'
            ? theme.palette.primary.light
            : theme.palette.grey[200],
}));

export default SelectedInfo;
