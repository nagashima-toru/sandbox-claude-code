'use client';

import { useCallback } from 'react';
import { TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { ArrowUpDown, ArrowUp, ArrowDown } from 'lucide-react';

export type SortField = 'id' | 'code' | 'content';
export type SortDirection = 'asc' | 'desc';

interface MessageTableHeaderProps {
  sortField: SortField;
  sortDirection: SortDirection;
  onSort: (field: SortField) => void;
}

export function MessageTableHeader({ sortField, sortDirection, onSort }: MessageTableHeaderProps) {
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

  const getSortLabel = useCallback(
    (field: SortField, fieldName: string) => {
      if (sortField !== field) {
        return `Sort by ${fieldName}`;
      }
      return sortDirection === 'asc'
        ? `Sort by ${fieldName}, currently ascending`
        : `Sort by ${fieldName}, currently descending`;
    },
    [sortField, sortDirection]
  );

  return (
    <TableHeader data-testid="message-table-header">
      <TableRow>
        <TableHead className="w-[100px]">
          <button
            onClick={() => onSort('id')}
            className="flex items-center hover:text-foreground"
            aria-label={getSortLabel('id', 'ID')}
            aria-pressed={sortField === 'id'}
          >
            ID
            {getSortIcon('id')}
          </button>
        </TableHead>
        <TableHead className="w-[200px]">
          <button
            onClick={() => onSort('code')}
            className="flex items-center hover:text-foreground"
            aria-label={getSortLabel('code', 'Code')}
            aria-pressed={sortField === 'code'}
          >
            Code
            {getSortIcon('code')}
          </button>
        </TableHead>
        <TableHead>
          <button
            onClick={() => onSort('content')}
            className="flex items-center hover:text-foreground"
            aria-label={getSortLabel('content', 'Content')}
            aria-pressed={sortField === 'content'}
          >
            Content
            {getSortIcon('content')}
          </button>
        </TableHead>
        <TableHead className="w-[150px] text-right">Actions</TableHead>
      </TableRow>
    </TableHeader>
  );
}
