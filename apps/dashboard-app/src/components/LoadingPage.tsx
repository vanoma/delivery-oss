import { CircularProgress, Stack, Box } from '@mui/material';
import React from 'react';

function LoadingPage(): JSX.Element {
    return (
        <Stack
            sx={{
                height: '100vh',
                width: '100vw',
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
            }}
            spacing={2}
        >
            <Box sx={{ position: 'relative' }}>
                <CircularProgress size={84} thickness={16} />
                <Box sx={{ position: 'absolute', top: 7, left: 7 }}>
                    <svg
                        width="70"
                        height="70"
                        viewBox="0 0 70 70"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                    >
                        <circle cx="35" cy="35" r="35" fill="#D61B53" />
                        <path
                            d="M62.1906 12.9571C58.9083 8.90987 54.7639 5.64644 50.0598 3.40483C45.3557 1.16323 40.2106 0 34.9997 0C29.7888 0 24.6436 1.16323 19.9395 3.40483C15.2354 5.64644 11.091 8.90987 7.80872 12.9571L13.6665 19.8483L35.0641 45.335L56.4617 19.8483L56.4309 19.8077L62.1906 12.9571ZM35.0683 41.3285L16.9811 19.7881C18.9509 17.4529 21.3495 15.5163 24.0474 14.083C27.4434 12.2913 31.2251 11.355 35.0648 11.355C38.9044 11.355 42.6862 12.2913 46.0822 14.083C48.7801 15.5163 51.1787 17.4529 53.1484 19.7881L35.0683 41.3285Z"
                            fill="white"
                        />
                        <path
                            d="M9.10661 39.1523L0.523748 28.9296C0.173619 30.9317 -0.00161931 32.9606 1.12747e-05 34.9932C1.12747e-05 54.3253 15.6701 69.994 34.9994 69.994L26.4208 59.7713L9.10661 39.1523Z"
                            fill="white"
                        />
                        <path
                            d="M69.4748 28.931L60.8934 39.1537C60.8934 39.1873 60.8808 39.2195 60.8752 39.2517L43.6716 59.7419L43.5778 59.7769L34.9991 69.9996C54.3242 69.9996 70 54.3309 70 34.9988C70.0014 32.9648 69.8257 30.9345 69.4748 28.931Z"
                            fill="white"
                        />
                    </svg>
                </Box>
            </Box>
        </Stack>
    );
}

export default LoadingPage;
