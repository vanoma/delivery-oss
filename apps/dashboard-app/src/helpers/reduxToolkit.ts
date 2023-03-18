/* eslint-disable no-param-reassign */
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit';
import { useDispatch } from 'react-redux';
import { AppDispatch } from '../redux/store';

// eslint-disable-next-line import/prefer-default-export
export const useTypedDispatch = (): ThunkDispatch<any, {}, AnyAction> =>
    useDispatch<AppDispatch>();
