import React from 'react';

// 1. Define the TypeScript interface for the component's props
interface PaginationProps {
    currentPage: number;
    totalPage: number;
    onPageChange: (page: number) => void; // Defines a function that takes a number and returns nothing (void)
}

// 2. Attach the interface to the component function
const Pagination: React.FC<PaginationProps> = ({totalPage, onPageChange }) => {
    // The rest of the logic is correct JSX
    const pages = Array.from({ length: totalPage }, (_, i) => i + 1);

    return (
        <div>
            {pages.map((page) => (
                // Key prop is correct, and the onClick handler is correct
                <button
                    key={page}
                    onClick={ () => onPageChange(page)}
                    // You might add a className here to style the current page
                >
                    {page}
                </button>
            ))}
        </div>
    );
};

export default Pagination;