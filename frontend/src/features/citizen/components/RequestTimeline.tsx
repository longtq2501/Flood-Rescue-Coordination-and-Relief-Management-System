import { TIMELINE_STEPS } from '../constants';
import { CitizenRequest } from '../types';

interface RequestTimelineProps {
  request: CitizenRequest;
}

export function RequestTimeline({ request }: RequestTimelineProps) {
  const currentIndex = TIMELINE_STEPS.findIndex(step => step.key === request.status);

  return (
    <div className="mt-6">
      <h3 className="font-medium mb-4">Tiến trình</h3>
      <div className="space-y-4">
        {TIMELINE_STEPS.map((step, idx) => {
          const isActive = idx <= currentIndex;
          const isCurrent = step.key === request.status;

          return (
            <div key={step.key} className="flex items-start gap-3">
              <div className={`w-6 h-6 rounded-full flex items-center justify-center text-sm ${
                isActive ? 'bg-green-500 text-white' : 'bg-gray-200 text-gray-500'
              }`}>
                {isActive ? '✓' : idx + 1}
              </div>
              <div className={`flex-1 ${isCurrent ? 'font-semibold text-red-500' : ''}`}>
                <div>{step.label}</div>
                {isCurrent && request.updatedAt && (
                  <div className="text-xs text-gray-500">
                    {new Date(request.updatedAt).toLocaleString()}
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}