import { Toaster } from 'sonner';

export default function CitizenLayout({ children }: { children: React.ReactNode }) {
  return (
    //<RoleGuard allowedRoles={['CITIZEN']}>
      <div className="min-h-screen bg-gray-50">
        <main className="container mx-auto px-4 py-6">{children}</main>
        <Toaster position="top-right" />
      </div>
    //</RoleGuard>
  );
}