/// <reference lib="dom" />

/* eslint-disable no-console */

// eslint-disable-next-line import/prefer-default-export
export const updateSWRegistrations = async (): Promise<void> => {
    if ('serviceWorker' in navigator) {
        const registrations = await navigator.serviceWorker.getRegistrations();
        await registrations.map((registration) => registration.update());
    } else {
        console.log('serviceWorker not supported');
    }
};
