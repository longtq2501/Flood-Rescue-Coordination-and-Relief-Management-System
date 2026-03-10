// src/app/layout.tsx
import './globals.css';
import type { Metadata } from 'next';
import { Providers } from '@/shared/components/Providers';

export const metadata: Metadata = {
    title: {
        default: 'Flood Rescue System — Hệ thống cứu hộ lũ lụt',
        template: '%s | Flood Rescue System',
    },
    description: 'Gửi yêu cầu cứu hộ khẩn cấp khi gặp lũ lụt. Hệ thống điều phối cứu hộ thời gian thực.',
    keywords: ['cứu hộ lũ lụt', 'khẩn cấp', 'cứu trợ', 'thiên tai', 'flood rescue'],
    authors: [{ name: 'Flood Rescue Team' }],
    robots: {
        index: true,
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
    return (
        <html lang="vi">
            <body>
                <Providers>
                    {children}
                </Providers>
            </body>
        </html>
    );
}