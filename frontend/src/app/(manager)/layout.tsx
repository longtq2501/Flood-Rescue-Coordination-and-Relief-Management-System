import RoleGuard from '@/shared/components/guards/RoleGuard';

export default function ManagerLayout({
    children,
}: {
    children: React.ReactNode;
}) {
    return (
        <RoleGuard allowedRoles={['MANAGER', 'ADMIN']}>
            <div className="min-h-screen bg-gray-50">
                {/* TODO Nhất: thêm Navbar + Sidebar cho Manager */}
                <main className="container mx-auto px-4 py-6">{children}</main>
            </div>
        </RoleGuard>
    );
}