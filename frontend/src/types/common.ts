export interface PageRequest<T> {
    content: T[];
    pageable: Pageable;
    totalElements: number;
}

export interface Pageable {
    pageNumber: number;
    pageSize: number;
}