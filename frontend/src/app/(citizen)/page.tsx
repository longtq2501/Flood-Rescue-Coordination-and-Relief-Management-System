import { ROUTES } from '@/constants/routes';
import { redirect } from 'next/navigation';

export default function CitizenPage() {
  redirect(ROUTES.CITIZEN.REQUESTS);
}
