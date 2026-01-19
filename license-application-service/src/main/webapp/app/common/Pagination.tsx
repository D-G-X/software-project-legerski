import React from 'react';
import {useTranslation} from "react-i18next";

// 1. Define the TypeScript interface for the component's props
interface PaginationProps {
  currentPage: number;
  totalPage: number;
  onPageChange: (page: number) => void; // Defines a function that takes a number and returns nothing (void)
  start: number;
  end: number;
  numberOfItems: number
}

// 2. Attach the interface to the component function
const Pagination: React.FC<PaginationProps> = ({
                                                 currentPage,
                                                 totalPage,
                                                 onPageChange,
                                                 start,
                                                 end,
                                                 numberOfItems
                                               }) => {

  const {t} = useTranslation();
  const pages = Array.from({length: totalPage}, (_, i) => i + 1);

  return (
      <div className={"flex flex-col md:flex-row justify-between mt-4 items-center"}>
        <div className={"text-xs text-gray-500 my-3"}>
          {t("dashboard.pagination.showingFrom")} {start} {t("dashboard.pagination.to")} {end > numberOfItems ? numberOfItems : end} {t("dashboard.pagination.of")} {numberOfItems} {t("dashboard.pagination.entries")}
        </div>
        <div className={"flex justify-end"}>
          <button
              id="previous-page-button"
              className="mx-1 px-3 py-1 bg-gray-200 text-mallorca-purple rounded-sm disabled:opacity-50"
              onClick={() => onPageChange(currentPage - 1)} disabled={currentPage === 1}>
            {t("dashboard.pagination.previous")}
          </button>
          {pages.map((page) => (
              // Key prop is correct, and the onClick handler is correct
              <button
                  id={`page-button-${page}`}
                  className={`mx-1 px-3 py-1 ${currentPage === page ? 'bg-mallorca-purple' : 'bg-gray-200'} ${currentPage === page ? 'text-white' : 'text-mallorca-purple'} rounded-sm disabled:opacity-50`}
                  key={page}
                  onClick={() => onPageChange(page)}
                  // You might add a className here to style the current page
              >
                {page}
              </button>
          ))}
          <button
              id="next-page-button"
              className="mx-1 px-3 py-1 bg-gray-200 text-mallorca-purple rounded-sm disabled:opacity-50"
              onClick={() => onPageChange(currentPage + 1)} disabled={currentPage === totalPage}>
            {t("dashboard.pagination.next")}
          </button>
        </div>
      </div>
  );
};

export default Pagination;