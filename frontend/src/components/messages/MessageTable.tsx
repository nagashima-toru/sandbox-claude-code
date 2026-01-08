'use client';

import { useMemo, useState, useEffect, useCallback } from 'react';
import { useGetAllMessages } from '@/lib/api/generated/message/message';
import { MessageResponse } from '@/lib/api/generated/models';
import { Loading } from '@/components/common/Loading';
import { ErrorMessage } from '@/components/common/ErrorMessage';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Edit, Trash2, ArrowUpDown, ArrowUp, ArrowDown } from 'lucide-react';
import { SearchBar } from './SearchBar';
import { Pagination } from './Pagination';
import { useDebounce } from '@/hooks/useDebounce';

interface MessageTableProps {
  onEdit?: (message: MessageResponse) => void;
  onDelete?: (message: MessageResponse) => void;
}

type SortField = 'id' | 'code' | 'content';
type SortDirection = 'asc' | 'desc';

const DEFAULT_PAGE_SIZE = 10;
const SEARCH_DEBOUNCE_MS = 300;

export default function MessageTable({ onEdit, onDelete }: MessageTableProps) {
  const { data: messages, isLoading, error } = useGetAllMessages();
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

  const getSortIcon = useCallback(
    (field: SortField) => {
      if (sortField !== field) {
        return <ArrowUpDown className="h-4 w-4 ml-1 inline" />;
      }
      return sortDirection === 'asc' ? (
        <ArrowUp className="h-4 w-4 ml-1 inline" />
      ) : (
        <ArrowDown className="h-4 w-4 ml-1 inline" />
      );
    },
    [sortField, sortDirection]
  );

  const filteredAndSortedMessages = useMemo(() => {
    if (!messages) return [];

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
      <div className="flex justify-center items-center py-12">
        <Loading size="lg" text="Loading messages..." />
      </div>
    );
  }

  if (error) {
    return (
      <ErrorMessage message="Failed to load messages. Please try again later." variant="card" />
    );
  }

  if (!messages || messages.length === 0) {
    return (
      <div className="text-center py-12">
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
      />

      {filteredAndSortedMessages.length === 0 ? (
        <div className="text-center py-12 border rounded-lg">
          <p className="text-muted-foreground text-lg">No messages match your search.</p>
          <p className="text-muted-foreground text-sm mt-2">Try a different search term.</p>
        </div>
      ) : (
        <>
          <div className="border rounded-lg overflow-hidden">
            <div className="hidden md:block">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead className="w-[100px]">
                      <button
                        onClick={() => handleSort('id')}
                        className="flex items-center hover:text-foreground"
                      >
                        ID
                        {getSortIcon('id')}
                      </button>
                    </TableHead>
                    <TableHead className="w-[200px]">
                      <button
                        onClick={() => handleSort('code')}
                        className="flex items-center hover:text-foreground"
                      >
                        Code
                        {getSortIcon('code')}
                      </button>
                    </TableHead>
                    <TableHead>
                      <button
                        onClick={() => handleSort('content')}
                        className="flex items-center hover:text-foreground"
                      >
                        Content
                        {getSortIcon('content')}
                      </button>
                    </TableHead>
                    <TableHead className="w-[150px] text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {paginatedMessages.map((message: MessageResponse) => (
                    <TableRow key={message.id}>
                      <TableCell className="font-medium">{message.id}</TableCell>
                      <TableCell>
                        <code className="bg-muted px-2 py-1 rounded text-sm">{message.code}</code>
                      </TableCell>
                      <TableCell className="max-w-md truncate">{message.content}</TableCell>
                      <TableCell className="text-right">
                        <div className="flex justify-end gap-2">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => onEdit?.(message)}
                            title="Edit message"
                          >
                            <Edit className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => onDelete?.(message)}
                            title="Delete message"
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>

            <div className="md:hidden divide-y">
              {paginatedMessages.map((message: MessageResponse) => (
                <div key={message.id} className="p-4 space-y-3">
                  <div className="flex items-start justify-between">
                    <div className="space-y-1 flex-1">
                      <div className="flex items-center gap-2">
                        <span className="text-xs text-muted-foreground">ID:</span>
                        <span className="font-medium">{message.id}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <span className="text-xs text-muted-foreground">Code:</span>
                        <code className="bg-muted px-2 py-1 rounded text-sm">{message.code}</code>
                      </div>
                    </div>
                    <div className="flex gap-2">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => onEdit?.(message)}
                        title="Edit message"
                      >
                        <Edit className="h-4 w-4" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => onDelete?.(message)}
                        title="Delete message"
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>
                  <div>
                    <span className="text-xs text-muted-foreground">Content:</span>
                    <p className="mt-1 text-sm">{message.content}</p>
                  </div>
                </div>
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
