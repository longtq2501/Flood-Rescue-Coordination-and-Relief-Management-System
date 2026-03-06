// Layout public — không cần RoleGuard
export default function LandingLayout({
    children,
}: {
    children: React.ReactNode;
}) {
    return (
        <div className="min-h-screen bg-white">
            {/* TODO Nhất: thêm Navbar public (Logo + Login + Register button) */}
            <main>{children}</main>
            {/* TODO Nhất: thêm Footer */}
        </div>
    );
}