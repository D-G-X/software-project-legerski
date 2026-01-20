import React from "react";
import { useTranslation } from "react-i18next";

// 1. Define the TypeScript interface for the component's props
interface PaginationProps {
  currentPage: number;
  totalPage: number;
  onPageChange: (page: number) => void;
  start: number;
  end: number;
  numberOfItems: number;
}

// Helper function to calculate visible pages
const getVisiblePages = (
  currentPage: number,
  totalPage: number,
): (number | "dots")[] => {
  if (totalPage <= 10) {
    return Array.from({ length: totalPage }, (_, i) => i + 1);
  }

  const pages = new Set<number>();

  // First 2 pages
  pages.add(1);
  pages.add(2);

  // Last 2 pages
  pages.add(totalPage - 1);
  pages.add(totalPage);

  // Middle 4 pages (around current page)
  for (let i = currentPage - 1; i <= currentPage + 2; i++) {
    if (i > 2 && i < totalPage - 1) {
      pages.add(i);
    }
  }

  const sortedPages = Array.from(pages).sort((a, b) => a - b);

  // Insert dots where gaps exist
  const result: (number | "dots")[] = [];
  for (let i = 0; i < sortedPages.length; i++) {
    if (
      i > 0 &&
      typeof sortedPages[i] === "number" &&
      typeof sortedPages[i - 1] === "number" &&
      (sortedPages[i] as number) - (sortedPages[i - 1] as number) > 1
    ) {
      result.push("dots");
    }
    if (typeof sortedPages[i] === "number" && sortedPages[i] !== undefined) {
      result.push(sortedPages[i] as number);
    }
  }

  return result;
};

// 2. Attach the interface to the component function
const Pagination: React.FC<PaginationProps> = ({
  currentPage,
  totalPage,
  onPageChange,
  start,
  end,
  numberOfItems,
}) => {
  const { t } = useTranslation();
  const pages = getVisiblePages(currentPage, totalPage);

  return (
    <div className="flex flex-col md:flex-row justify-between mt-4 items-center">
      <div className="text-xs text-gray-500 my-3">
        {t("dashboard.pagination.showingFrom")} {start}{" "}
        {t("dashboard.pagination.to")}{" "}
        {end > numberOfItems ? numberOfItems : end}{" "}
        {t("dashboard.pagination.of")} {numberOfItems}{" "}
        {t("dashboard.pagination.entries")}
      </div>

      <div className="flex justify-end items-center">
        {/* Previous */}
        <button
          id="previous-page-button"
          className="mx-1 px-3 py-1 bg-gray-200 text-mallorca-purple rounded-sm disabled:opacity-50"
          onClick={() => onPageChange(currentPage - 1)}
          disabled={currentPage === 1}
        >
          {t("dashboard.pagination.previous")}
        </button>

        {/* Page numbers */}
        {pages.map((page, index) =>
          page === "dots" ? (
            <span
              key={`dots-${index}`}
              className="mx-2 text-gray-400 select-none"
            >
              …
            </span>
          ) : (
            <button
              id={`page-button-${page}`}
              key={page}
              onClick={() => onPageChange(page)}
              className={`mx-1 px-3 py-1 rounded-sm disabled:opacity-50
                ${
                  currentPage === page
                    ? "bg-mallorca-purple text-white"
                    : "bg-gray-200 text-mallorca-purple"
                }`}
            >
              {page}
            </button>
          ),
        )}

        {/* Next */}
        <button
          id="next-page-button"
          className="mx-1 px-3 py-1 bg-gray-200 text-mallorca-purple rounded-sm disabled:opacity-50"
          onClick={() => onPageChange(currentPage + 1)}
          disabled={currentPage === totalPage}
        >
          {t("dashboard.pagination.next")}
        </button>
      </div>
    </div>
  );
};

export default Pagination;
