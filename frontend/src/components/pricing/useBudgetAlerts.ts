import { useEffect, useState } from 'react';

interface BudgetAlert{
    type: string;
    userId: string;
    currentTotal: number;
    budget: number;
    thresholdPercentage: number;
    currency?: string;
    message?: string;
    timestamp: number;
}

export interface BudgetAlertComputed extends BudgetAlert {
    currentPercentage: number;
    hasReachedThreshold: boolean;
}

export function useBudgetAlerts(
    basketTotal: number
): BudgetAlertComputed | null  {
    const [serverAlert, setServerAlert] = useState<BudgetAlert | null>(null);

    useEffect(() => {
        const eventSource = new EventSource('/api/budget/alerts/stream');

        eventSource.addEventListener('budgetAlert', (event) => {
            console.log('Budget alert event received: ', event);
            const data = JSON.parse(event.data) as BudgetAlert;
            console.log('Budget alert received:', data);
            setServerAlert(data);
        });

        eventSource.onerror = (error) => {
            console.error('SSE error:', error);
            eventSource.close();
        }

        eventSource.onerror = (error) => {
            console.error('SSE error:', error);
            eventSource.close();
        };

        return () => {
            eventSource.close();
        };
    }, []);

    if (serverAlert == null) {
        return null;
    }

    const { budget, thresholdPercentage } = serverAlert;

    if (!budget || budget <= 0) {
        return null;
    }

    const currentPercentage = (basketTotal / budget) * 100;

    const hasReachedThreshold = currentPercentage >= thresholdPercentage;

    console.log(
        "[useBudgetAlerts] basketTotal:",
        basketTotal,
        "budget:",
        budget,
        "threshold%:",
        thresholdPercentage,
        "current%:",
        currentPercentage,
        "hasReachedThreshold:",
        hasReachedThreshold
    );



    if (!hasReachedThreshold) {
        return null;
    }

    return {
        ...serverAlert,
        currentTotal: basketTotal,
        currentPercentage,
        hasReachedThreshold,
    };

}