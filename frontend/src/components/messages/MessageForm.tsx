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
import { getApiErrorMessage } from '@/lib/utils/errorHandling';

interface MessageFormProps {
  onSubmit: (data: MessageFormData) => void;
  initialData?: MessageResponse;
  isSubmitting?: boolean;
  onCancel: () => void;
  error?: unknown;
  disabled?: boolean;
}

/**
 * MessageForm component handles creating and editing messages.
 * Uses React Hook Form with Zod validation for form management.
 *
 * @param onSubmit - Callback function called when form is successfully validated and submitted
 * @param initialData - Initial data for editing mode. If provided, form fields will be pre-populated
 * @param isSubmitting - Loading state during form submission
 * @param onCancel - Callback function called when cancel button is clicked
 * @param error - API error object to display error messages from server
 * @param disabled - If true, all form fields will be disabled (read-only mode)
 */
export default function MessageForm({
  onSubmit,
  initialData,
  isSubmitting = false,
  onCancel,
  error,
  disabled = false,
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

  const errorMessage = getApiErrorMessage(error);

  return (
    <Form {...form}>
      <form
        data-testid="message-form"
        onSubmit={form.handleSubmit(handleSubmit)}
        className="space-y-6"
      >
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
                <Input
                  placeholder="Enter message code"
                  {...field}
                  disabled={isSubmitting || disabled}
                  aria-readonly={disabled}
                  data-testid="message-code-input"
                />
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
                  disabled={isSubmitting || disabled}
                  aria-readonly={disabled}
                  data-testid="message-content-input"
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <div className="flex justify-end gap-3">
          <Button
            type="button"
            variant="outline"
            onClick={onCancel}
            disabled={isSubmitting}
            data-testid="message-form-cancel"
          >
            {disabled ? 'Close' : 'Cancel'}
          </Button>
          {!disabled && (
            <Button type="submit" disabled={isSubmitting} data-testid="message-form-submit">
              {isSubmitting ? 'Saving...' : 'Save'}
            </Button>
          )}
        </div>
      </form>
    </Form>
  );
}
