'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { messageSchema, MessageFormData } from '@/lib/validations/message';
import { Button } from '@/components/ui/button';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { MessageResponse } from '@/lib/api/generated/models';

interface MessageFormProps {
  onSubmit: (data: MessageFormData) => void;
  initialData?: MessageResponse;
  isSubmitting?: boolean;
  onCancel: () => void;
  error?: any;
}

export default function MessageForm({
  onSubmit,
  initialData,
  isSubmitting = false,
  onCancel,
  error,
}: MessageFormProps) {
  const form = useForm<MessageFormData>({
    resolver: zodResolver(messageSchema),
    defaultValues: {
      code: initialData?.code || '',
      content: initialData?.content || '',
    },
  });

  const handleSubmit = (data: MessageFormData) => {
    onSubmit(data);
  };

  const getErrorMessage = () => {
    if (!error) return null;

    const status = error?.response?.status;
    const message = error?.response?.data?.message || error?.message;

    if (status === 409) {
      return 'A message with this code already exists. Please use a different code.';
    }
    if (status === 404) {
      return 'Message not found. It may have been deleted.';
    }
    if (status === 400) {
      return message || 'Invalid input. Please check your data.';
    }
    if (status === 500) {
      return 'Server error. Please try again later.';
    }
    if (error?.code === 'ECONNABORTED' || error?.code === 'ERR_NETWORK') {
      return 'Network error. Please check your connection and try again.';
    }

    return message || 'An unexpected error occurred. Please try again.';
  };

  const errorMessage = getErrorMessage();

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-6">
        {errorMessage && (
          <div className="rounded-md bg-destructive/10 p-3 text-sm text-destructive border border-destructive/20">
            {errorMessage}
          </div>
        )}
        <FormField
          control={form.control}
          name="code"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Code</FormLabel>
              <FormControl>
                <Input placeholder="Enter message code" {...field} disabled={isSubmitting} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="content"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Content</FormLabel>
              <FormControl>
                <textarea
                  className="flex min-h-[120px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                  placeholder="Enter message content"
                  {...field}
                  disabled={isSubmitting}
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <div className="flex justify-end gap-3">
          <Button type="button" variant="outline" onClick={onCancel} disabled={isSubmitting}>
            Cancel
          </Button>
          <Button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Saving...' : 'Save'}
          </Button>
        </div>
      </form>
    </Form>
  );
}
