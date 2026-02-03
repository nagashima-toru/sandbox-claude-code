'use client';

import { useMemo, useState, useEffect } from 'react';
import { useGetAllMessages } from '@/lib/api/generated/message/message';
import { MessageResponse } from '@/lib/api/generated/models';
import { Loading } from '@/components/common/Loading';
import { ErrorMessage } from '@/components/common/ErrorMessage';
import { Table, TableBody } from '@/components/ui/table';
import { SearchBar } from './SearchBar';
import { Pagination } from './Pagination';
import { MessageTableHeader, SortField, SortDirection } from './MessageTableHeader';
import { MessageTableRow, MessageCard } from './MessageTableRow';
import { useDebounce } from '@/hooks/useDebounce';

interface MessageTableProps {
  onEdit?: (message: MessageResponse) => void;
  onDelete?: (message: MessageResponse) => void;
}

const DEFAULT_PAGE_SIZE = 10;
const SEARCH_DEBOUNCE_MS = 300;

/**
 * MessageTable component displays a list of messages with search, sort, and pagination features.
 * Includes responsive design with table view for desktop and card view for mobile.
 *
 * @param onEdit - Callback function triggered when edit button is clicked
 * @param onDelete - Callback function triggered when delete button is clicked
 */
export default function MessageTable({ onEdit, onDelete }: MessageTableProps) {
  const { data, isLoading, error } = useGetAllMessages();
  const messages = useMemo(() => data?.content ?? [], [data?.content]);
  const [searchQuery, setSearchQuery] = useState('');
  const debouncedSearch = useDebounce(searchQuery, SEARCH_DEBOUNCE_MS);
  const [sortField, setSortField] = useState<SortField>('id');
  const [sortDirection, setSortDirection] = useState<SortDirection>('asc');
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);

  // Reset to page 1 when search query changes
  useEffect(() => {
    setCurrentPage(1);
  }, [debouncedSearch]);

  const handleSort = (field: SortField) => {
    if (sortField === field) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortDirection('asc');
    }
  };

  const filteredAndSortedMessages = useMemo(() => {
    let result = [...messages];

    if (debouncedSearch) {
      const query = debouncedSearch.toLowerCase();
      result = result.filter(
        (msg) =>
          msg.code?.toLowerCase().includes(query) || msg.content?.toLowerCase().includes(query)
      );
    }

    result.sort((a, b) => {
      let aValue: string | number = a[sortField] ?? '';
      let bValue: string | number = b[sortField] ?? '';

      if (sortField === 'id') {
        aValue = Number(aValue ?? 0);
        bValue = Number(bValue ?? 0);
      }

      if (aValue < bValue) return sortDirection === 'asc' ? -1 : 1;
      if (aValue > bValue) return sortDirection === 'asc' ? 1 : -1;
      return 0;
    });

    return result;
  }, [messages, debouncedSearch, sortField, sortDirection]);

  const paginatedMessages = useMemo(() => {
    const startIndex = (currentPage - 1) * pageSize;
    const endIndex = startIndex + pageSize;
    return filteredAndSortedMessages.slice(startIndex, endIndex);
  }, [filteredAndSortedMessages, currentPage, pageSize]);

  const totalPages = Math.ceil(filteredAndSortedMessages.length / pageSize);

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const handlePageSizeChange = (size: number) => {
    setPageSize(size);
    setCurrentPage(1);
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center py-12" role="status" aria-live="polite">
        <Loading size="lg" text="Loading messages..." />
      </div>
    );
  }

  if (error) {
    return (
      <div role="alert">
        <ErrorMessage message="Failed to load messages. Please try again later." variant="card" />
      </div>
    );
  }

  if (messages.length === 0) {
    return (
      <div className="text-center py-12" role="status">
        <p className="text-muted-foreground text-lg">No messages found.</p>
        <p className="text-muted-foreground text-sm mt-2">
          Create your first message to get started.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <SearchBar
        value={searchQuery}
        onChange={setSearchQuery}
        placeholder="Search by code or content..."
        aria-label="Search messages"
      />

      {filteredAndSortedMessages.length === 0 ? (
        <div className="text-center py-12 border rounded-lg" role="status" aria-live="polite">
          <p className="text-muted-foreground text-lg">No messages match your search.</p>
          <p className="text-muted-foreground text-sm mt-2">Try a different search term.</p>
        </div>
      ) : (
        <>
          <div className="border rounded-lg overflow-hidden">
            {/* Desktop Table View */}
            <div className="hidden md:block">
              <Table aria-label="Messages table">
                <MessageTableHeader
                  sortField={sortField}
                  sortDirection={sortDirection}
                  onSort={handleSort}
                />
                <TableBody>
                  {paginatedMessages.map((message: MessageResponse) => (
                    <MessageTableRow
                      key={message.id}
                      message={message}
                      onEdit={onEdit}
                      onDelete={onDelete}
                    />
                  ))}
                </TableBody>
              </Table>
            </div>

            {/* Mobile Card View */}
            <div className="md:hidden divide-y" role="list" aria-label="Messages list">
              {paginatedMessages.map((message: MessageResponse) => (
                <MessageCard
                  key={message.id}
                  message={message}
                  onEdit={onEdit}
                  onDelete={onDelete}
                />
              ))}
            </div>
          </div>

          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            pageSize={pageSize}
            totalItems={filteredAndSortedMessages.length}
            onPageChange={handlePageChange}
            onPageSizeChange={handlePageSizeChange}
          />
        </>
      )}
    </div>
  );
}
