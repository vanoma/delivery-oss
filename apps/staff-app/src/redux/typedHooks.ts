import { useDispatch } from 'react-redux';
import type { AppDispatch } from './store';

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
export const useTypedDispatch = () => useDispatch<AppDispatch>();
