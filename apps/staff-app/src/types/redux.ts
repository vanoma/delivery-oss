/**
 *  Types that are used in redux state and related logic.
 */

export interface RootState {}

export interface SliceState {
    isLoading: boolean;
    error: string | null;
    success: string | null;
}
