import RoleGuard from '@/shared/components/guards/RoleGuard';

export default function CitizenLayout({
    children,
}: {
    children: React.ReactNode;
}) {
    return (
        <RoleGuard allowedRoles={['CITIZEN']}>
            <div className="min-h-screen bg-gray-50">
                {/* TODO Nhất: thêm Navbar + Sidebar cho Citizen */}
                <main className="container mx-auto px-4 py-6">{children}</main>
            </div>
        </RoleGuard>
    );
}