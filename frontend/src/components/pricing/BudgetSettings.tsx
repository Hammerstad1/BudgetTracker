import { useState } from 'react';
import './BugdetSettings.css';

export function BudgetSettings() {
    const [budget, setBudget] = useState<number | ''>('');
    const [threshold, setThreshold] = useState<number>(80);
    const [enabled, setEnabled] = useState<boolean>(true);
    const [saved, setSaved] = useState<boolean>(false);

    const saveBudget = async() => {
        try {
            await fetch ('/api/budget/set',{
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    userId: 'u1',
                    monthlyBudget: Number(budget),
                    warningThreshold: threshold / 100,
                    notificationEnabled: enabled,
                })
            });
            setSaved(true);
            setTimeout(() => setSaved(false), 3000);
        } catch (error) {
            console.error('Failed to save budget', error);
            alert('Failed to save budget settings');
        }
    };

    return (
        <div className="budget-settings">
            <h2> Budget Settings</h2>

            <div className="setting-group">
                <label>Your Budget (NOK)</label>
                <input
                    type="number"
                    value={budget}
                    onChange={(e) => setBudget(Number(e.target.value))}
                    placeholder="Enter your budget"
                    min="0"
                    step="100"
                />
            </div>

            <div className="setting-group">
                <label>WarningThreshold: {threshold}%</label>
                <input
                    type="range"
                    min="50"
                    max="100"
                    value={threshold}
                    onChange={(e) => setThreshold(Number(e.target.value))}
                />
                <small> You will get notified when spending reaches {threshold}% of budget</small>
            </div>
            <div className="setting-group">
                <label>
                    <input
                        type="checkbox"
                        checked={enabled}
                        onChange={(e) => setEnabled(e.target.checked)}
                    />
                    Enable notifications
                </label>
            </div>

            <button onClick={saveBudget} className="save-btn">
                Save Budget Settings
            </button>

            {saved && <div className="success-msg">Settings saved!</div>}
        </div>
    )
}