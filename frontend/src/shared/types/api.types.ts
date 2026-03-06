export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
    code?: string;
    timestamp: string;
}

export interface PageResponse<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
}

export interface PageParams {
    page?: number;
    size?: number;
    sort?: string;
}