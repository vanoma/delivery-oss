import React, { FC, Suspense, useEffect } from 'react';
import { useNavigate, useRoutes } from 'react-router-dom';
import { ThemeConfig } from '@vanoma/ui-theme';
import { useSelector } from 'react-redux';
import LoadingPage from './components/LoadingPage';
import {
    getStaff,
    selectIsAuthenticated,
} from './redux/slices/authenticationSlice';
import { useTypedDispatch } from './redux/typedHooks';
import routes from './routes';
import { initializeOnesignal } from './services/oneSignal';

const App: FC = () => {
    const isAuthenticated = useSelector(selectIsAuthenticated);
    const dispatch = useTypedDispatch();
    const navigate = useNavigate();

    useEffect(() => {
        initializeOnesignal(navigate);
        dispatch(getStaff());
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return (
        <ThemeConfig>
            <Suspense fallback={<LoadingPage />}>
                {useRoutes(routes(isAuthenticated))}
            </Suspense>
        </ThemeConfig>
    );
};

export default App;
