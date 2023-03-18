import React from 'react';

export default function LogoColor(): JSX.Element {
    return (
        <svg
            width="40"
            height="40"
            viewBox="0 0 40 40"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
            style={{
                filter: 'drop-shadow(0px 8px 4px rgba(0, 0, 0, 0.6))',
            }}
        >
            <path
                d="M35.5375 7.40405C33.6619 5.09137 31.2936 3.22654 28.6056 1.94562C25.9175 0.664704 22.9774 0 19.9998 0C17.0221 0 14.082 0.664704 11.3939 1.94562C8.70587 3.22654 6.33765 5.09137 4.46204 7.40405L7.80932 11.3419L20.0366 25.9058L32.2638 11.3419L32.2462 11.3187L35.5375 7.40405ZM20.039 23.6163L9.70343 11.3075C10.829 9.97309 12.1996 8.86649 13.7413 8.04742C15.6819 7.02364 17.8429 6.48856 20.037 6.48856C22.2311 6.48856 24.392 7.02364 26.3326 8.04742C27.8743 8.86649 29.2449 9.97309 30.3705 11.3075L20.039 23.6163Z"
                fill="url(#paint0_linear)"
            />
            <path
                d="M5.20379 22.3729L0.299285 16.5313C0.0992113 17.6754 -0.000925323 18.8348 6.44268e-06 19.9962C6.44268e-06 31.0432 8.95438 39.9967 19.9997 39.9967L15.0976 34.1552L5.20379 22.3729Z"
                fill="url(#paint1_linear)"
            />
            <path
                d="M39.6999 16.5322L34.7962 22.3737C34.7962 22.3929 34.789 22.4113 34.7858 22.4297L24.9552 34.1384L24.9016 34.1584L19.9995 40C31.0424 40 40 31.0464 40 19.9995C40.0008 18.8372 39.9004 17.677 39.6999 16.5322Z"
                fill="url(#paint2_linear)"
            />
            <defs>
                <linearGradient
                    id="paint0_linear"
                    x1="19.9998"
                    y1="0"
                    x2="19.9998"
                    y2="25.9058"
                    gradientUnits="userSpaceOnUse"
                >
                    <stop stopColor="#D61B53" stopOpacity="0.49" />
                    <stop offset="0.317708" stopColor="#D61B53" />
                </linearGradient>
                <linearGradient
                    id="paint1_linear"
                    x1="9.99985"
                    y1="16.5313"
                    x2="9.99985"
                    y2="39.9967"
                    gradientUnits="userSpaceOnUse"
                >
                    <stop stopColor="#D61B53" stopOpacity="0.49" />
                    <stop offset="0.317708" stopColor="#D61B53" />
                </linearGradient>
                <linearGradient
                    id="paint2_linear"
                    x1="29.9998"
                    y1="16.5322"
                    x2="29.9998"
                    y2="40"
                    gradientUnits="userSpaceOnUse"
                >
                    <stop stopColor="#D61B53" stopOpacity="0.49" />
                    <stop offset="0.317708" stopColor="#D61B53" />
                </linearGradient>
            </defs>
        </svg>
    );
}
