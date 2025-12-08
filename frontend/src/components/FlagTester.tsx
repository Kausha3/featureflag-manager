import { useState } from 'react';
import { TestTube, Play, Loader2, CheckCircle, XCircle, Info } from 'lucide-react';
import type { EvaluateRequest, EvaluationResponse, EvaluationReason } from '../types';
import { evaluateFlags } from '../api/flagApi';

const REASON_LABELS: Record<EvaluationReason, string> = {
  FLAG_DISABLED: 'Flag is disabled',
  RULE_MATCH: 'Matched targeting rule',
  ROLLOUT_INCLUDED: 'Included in rollout percentage',
  ROLLOUT_EXCLUDED: 'Excluded from rollout percentage',
  NO_RULES_DEFAULT: 'No rules matched, using default',
};

export default function FlagTester() {
  const [userId, setUserId] = useState('');
  const [userEmail, setUserEmail] = useState('');
  const [country, setCountry] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<EvaluationResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleEvaluate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!userId.trim()) return;

    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const request: EvaluateRequest = {
        userId: userId.trim(),
        userEmail: userEmail.trim() || undefined,
        country: country.trim() || undefined,
      };
      const response = await evaluateFlags(request);
      setResult(response);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to evaluate flags');
    } finally {
      setLoading(false);
    }
  };

  const flagEntries = result ? Object.entries(result.flags) : [];

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
        <TestTube className="w-7 h-7" />
        Flag Tester
      </h1>

      <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Test User Context</h2>
        <p className="text-gray-600 text-sm mb-4">
          Enter user details to see which flags would be enabled for them.
        </p>

        <form onSubmit={handleEvaluate} className="space-y-4">
          <div className="grid grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                User ID <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                value={userId}
                onChange={(e) => setUserId(e.target.value)}
                placeholder="user123"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 font-mono text-sm"
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Email
              </label>
              <input
                type="email"
                value={userEmail}
                onChange={(e) => setUserEmail(e.target.value)}
                placeholder="user@company.com"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Country
              </label>
              <input
                type="text"
                value={country}
                onChange={(e) => setCountry(e.target.value)}
                placeholder="US"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm uppercase"
                maxLength={2}
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading || !userId.trim()}
            className="flex items-center gap-2 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition disabled:opacity-50"
          >
            {loading ? (
              <Loader2 className="w-5 h-5 animate-spin" />
            ) : (
              <Play className="w-5 h-5" />
            )}
            Evaluate Flags
          </button>
        </form>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
          {error}
        </div>
      )}

      {result && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">
            Results for <span className="font-mono text-blue-600">{userId}</span>
          </h2>

          {flagEntries.length === 0 ? (
            <p className="text-gray-500 text-center py-8">
              No enabled flags found. Create some flags first!
            </p>
          ) : (
            <div className="space-y-3">
              {flagEntries.map(([flagName, enabled]) => {
                const detail = result.details[flagName];
                return (
                  <div
                    key={flagName}
                    className={`p-4 rounded-lg border ${
                      enabled
                        ? 'bg-green-50 border-green-200'
                        : 'bg-red-50 border-red-200'
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        {enabled ? (
                          <CheckCircle className="w-5 h-5 text-green-600" />
                        ) : (
                          <XCircle className="w-5 h-5 text-red-600" />
                        )}
                        <span className="font-mono font-medium text-gray-900">
                          {flagName}
                        </span>
                      </div>
                      <span
                        className={`px-3 py-1 rounded-full text-sm font-medium ${
                          enabled
                            ? 'bg-green-100 text-green-700'
                            : 'bg-red-100 text-red-700'
                        }`}
                      >
                        {enabled ? 'ENABLED' : 'DISABLED'}
                      </span>
                    </div>

                    {detail && (
                      <div className="mt-2 flex items-start gap-2 text-sm text-gray-600">
                        <Info className="w-4 h-4 mt-0.5 flex-shrink-0" />
                        <div>
                          <div className="font-medium">{REASON_LABELS[detail.reason]}</div>
                          <div className="text-gray-500">{detail.explanation}</div>
                        </div>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
