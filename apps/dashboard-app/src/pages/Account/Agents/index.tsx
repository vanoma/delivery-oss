import { Box, Card, Skeleton, Typography } from '@mui/material';
import React from 'react';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import { useGetAgentsQuery } from '../../../api';
import { selectCustomerId } from '../../../redux/slices/authenticationSlice';
import AddAgentButton from './AddAgentButton';
import AgentView from './Agent';

const Agents: React.FC = () => {
    const { t } = useTranslation();

    const customerId = useSelector(selectCustomerId);
    const { data, isFetching, isLoading } = useGetAgentsQuery(customerId!);

    return (
        <>
            <Box
                display="flex"
                justifyContent="space-between"
                alignItems="center"
            >
                <Typography variant="h5">
                    {t('account.agents.agents')}
                </Typography>
                <AddAgentButton />
            </Box>
            <Box>
                {isLoading &&
                    [...new Array(10)].map((e, index) => (
                        // eslint-disable-next-line react/no-array-index-key
                        <Card sx={{ mb: 2 }} key={index}>
                            <Skeleton
                                variant="rectangular"
                                animation="wave"
                                height={74}
                                sx={{ borderRadius: 0.5 }}
                            />
                        </Card>
                    ))}
                {data &&
                    data.results.map((agent) => (
                        <AgentView agent={agent} key={agent.agentId} />
                    ))}
            </Box>
            {!isFetching && data && data.results.length === 0 && (
                <Typography sx={{ pb: 4, pt: 2 }} align="center">
                    {t('account.Agents.agentsNotFound')}
                </Typography>
            )}
        </>
    );
};

export default Agents;
