// src/app/layout.tsx
'use client';

import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useState } from 'react';
import { Toaster } from '@/shared/components/ui/sonner';
import './globals.css';

import type { Metadata } from 'next';

export const metadata: Metadata = {
    title: {
        default: 'Flood Rescue System — Hệ thống cứu hộ lũ lụt',
        template: '%s | Flood Rescue System',
    },
    description: 'Gửi yêu cầu cứu hộ khẩn cấp khi gặp lũ lụt. Hệ thống điều phối cứu hộ thời gian thực.',
    keywords: ['cứu hộ lũ lụt', 'khẩn cấp', 'cứu trợ', 'thiên tai', 'flood rescue'],
    authors: [{ name: 'Flood Rescue Team' }],
    robots: {
        index: true,   // public → cho Google index
        follow: true,
    },
    openGraph: {
        title: 'Flood Rescue System — Hệ thống cứu hộ lũ lụt',
        description: 'Gửi yêu cầu cứu hộ khẩn cấp khi gặp lũ lụt.',
        locale: 'vi_VN',
        type: 'website',
    },
};

export default function RootLayout({
    children,
}: {
    children: React.ReactNode;
}) {
    const [queryClient] = useState(() => new QueryClient({
        defaultOptions: {
            queries: {
                retry: 1,
                staleTime: 30 * 1000,
            },
        },
    }));

    return (
        <html lang="vi">
            <body>
                <QueryClientProvider client={queryClient}>
                    {children}
                    <Toaster richColors position="top-right" />
                </QueryClientProvider>
            </body>
        </html>
    );
}