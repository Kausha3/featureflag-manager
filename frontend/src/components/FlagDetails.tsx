import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  ArrowLeft,
  Plus,
  Trash2,
  ToggleLeft,
  ToggleRight,
  Loader2,
  User,
  Mail,
  Globe,
  Percent,
} from 'lucide-react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from 'recharts';
import type { Flag, Rule, Analytics, RuleType, CreateRuleRequest } from '../types';
import { getFlag, toggleFlag, addRule, toggleRule, deleteRule, getAnalytics } from '../api/flagApi';

const RULE_TYPE_ICONS: Record<RuleType, typeof User> = {
  USER_ID: User,
  EMAIL_DOMAIN: Mail,
  EMAIL_EXACT: Mail,
  COUNTRY: Globe,
  PERCENTAGE_GROUP: Percent,
};

const RULE_TYPE_LABELS: Record<RuleType, string> = {
  USER_ID: 'User ID',
  EMAIL_DOMAIN: 'Email Domain',
  EMAIL_EXACT: 'Email (Exact)',
  COUNTRY: 'Country',
  PERCENTAGE_GROUP: 'Percentage Group',
};

const PIE_COLORS = ['#22c55e', '#ef4444'];

export default function FlagDetails() {
  const { id } = useParams<{ id: string }>();
  const [flag, setFlag] = useState<Flag | null>(null);
  const [analytics, setAnalytics] = useState<Analytics | null>(null);
  const [loading, setLoading] = useState(true);
  const [showRuleForm, setShowRuleForm] = useState(false);
  const [newRule, setNewRule] = useState<CreateRuleRequest>({
    ruleType: 'USER_ID',
    ruleValue: '',
    enabled: true,
    priority: 0,
  });

  useEffect(() => {
    if (id) {
      loadData();
    }
  }, [id]);

  const loadData = async () => {
    try {
      setLoading(true);
      const [flagData, analyticsData] = await Promise.all([
        getFlag(id!),
        getAnalytics(id!, 24),
      ]);
      setFlag(flagData);
      setAnalytics(analyticsData);
    } catch (err) {
      console.error('Failed to load flag details:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleToggleFlag = async () => {
    if (!flag) return;
    try {
      await toggleFlag(flag.id, !flag.enabled);
      setFlag({ ...flag, enabled: !flag.enabled });
    } catch (err) {
      console.error('Failed to toggle flag:', err);
    }
  };

  const handleAddRule = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!flag) return;

    try {
      await addRule(flag.id, newRule);
      setNewRule({ ruleType: 'USER_ID', ruleValue: '', enabled: true, priority: 0 });
      setShowRuleForm(false);
      loadData();
    } catch (err) {
      console.error('Failed to add rule:', err);
    }
  };

  const handleToggleRule = async (rule: Rule) => {
    try {
      await toggleRule(rule.id, !rule.enabled);
      loadData();
    } catch (err) {
      console.error('Failed to toggle rule:', err);
    }
  };

  const handleDeleteRule = async (ruleId: string) => {
    if (!confirm('Delete this rule?')) return;
    try {
      await deleteRule(ruleId);
      loadData();
    } catch (err) {
      console.error('Failed to delete rule:', err);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
      </div>
    );
  }

  if (!flag) {
    return <div className="text-center text-gray-500">Flag not found</div>;
  }

  const pieData = analytics
    ? [
        { name: 'Enabled', value: analytics.enabledCount },
        { name: 'Disabled', value: analytics.disabledCount },
      ]
    : [];

  const timeSeriesData = analytics?.evaluationsOverTime.map((point) => ({
    time: new Date(point.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    enabled: point.enabledCount,
    disabled: point.disabledCount,
    total: point.totalCount,
  })) || [];

  return (
    <div className="space-y-6">
      <Link
        to="/"
        className="inline-flex items-center gap-2 text-gray-600 hover:text-gray-900 transition"
      >
        <ArrowLeft className="w-4 h-4" />
        Back to Flags
      </Link>

      {/* Header */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <div className="flex items-start justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 font-mono">{flag.name}</h1>
            <p className="text-gray-600 mt-1">{flag.description || 'No description'}</p>
          </div>
          <button
            onClick={handleToggleFlag}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg font-medium transition ${
              flag.enabled
                ? 'bg-green-100 text-green-700 hover:bg-green-200'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            {flag.enabled ? (
              <>
                <ToggleRight className="w-5 h-5" />
                Enabled
              </>
            ) : (
              <>
                <ToggleLeft className="w-5 h-5" />
                Disabled
              </>
            )}
          </button>
        </div>

        <div className="flex gap-6 mt-4 text-sm">
          <div>
            <span className="text-gray-500">Rollout:</span>
            <span className="ml-2 font-semibold text-blue-600">{flag.rolloutPercentage}%</span>
          </div>
          <div>
            <span className="text-gray-500">Created:</span>
            <span className="ml-2">{new Date(flag.createdAt).toLocaleDateString()}</span>
          </div>
          <div>
            <span className="text-gray-500">Updated:</span>
            <span className="ml-2">{new Date(flag.updatedAt).toLocaleDateString()}</span>
          </div>
        </div>
      </div>

      {/* Targeting Rules */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold text-gray-900">Targeting Rules</h2>
          <button
            onClick={() => setShowRuleForm(!showRuleForm)}
            className="flex items-center gap-2 px-3 py-1.5 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
          >
            <Plus className="w-4 h-4" />
            Add Rule
          </button>
        </div>

        {showRuleForm && (
          <form onSubmit={handleAddRule} className="mb-4 p-4 bg-gray-50 rounded-lg space-y-3">
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Rule Type</label>
                <select
                  value={newRule.ruleType}
                  onChange={(e) => setNewRule({ ...newRule, ruleType: e.target.value as RuleType })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                >
                  {Object.entries(RULE_TYPE_LABELS).map(([value, label]) => (
                    <option key={value} value={value}>{label}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Value</label>
                <input
                  type="text"
                  value={newRule.ruleValue}
                  onChange={(e) => setNewRule({ ...newRule, ruleValue: e.target.value })}
                  placeholder={newRule.ruleType === 'EMAIL_DOMAIN' ? '@company.com' : 'value'}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm font-mono"
                  required
                />
              </div>
            </div>
            <div className="flex gap-2">
              <button
                type="submit"
                className="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700"
              >
                Add Rule
              </button>
              <button
                type="button"
                onClick={() => setShowRuleForm(false)}
                className="px-4 py-2 border border-gray-300 rounded-lg text-sm hover:bg-gray-50"
              >
                Cancel
              </button>
            </div>
          </form>
        )}

        <div className="space-y-2">
          {flag.rules?.length === 0 ? (
            <p className="text-gray-500 text-sm py-4 text-center">
              No targeting rules. Flag uses rollout percentage only.
            </p>
          ) : (
            flag.rules?.map((rule) => {
              const Icon = RULE_TYPE_ICONS[rule.ruleType];
              return (
                <div
                  key={rule.id}
                  className={`flex items-center justify-between p-3 rounded-lg border ${
                    rule.enabled ? 'bg-white border-gray-200' : 'bg-gray-50 border-gray-100'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <Icon className="w-4 h-4 text-gray-400" />
                    <span className="text-sm text-gray-600">{RULE_TYPE_LABELS[rule.ruleType]}</span>
                    <span className="font-mono text-sm bg-gray-100 px-2 py-0.5 rounded">
                      {rule.ruleValue}
                    </span>
                  </div>
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => handleToggleRule(rule)}
                      className={`p-1.5 rounded ${
                        rule.enabled
                          ? 'text-green-600 hover:bg-green-50'
                          : 'text-gray-400 hover:bg-gray-100'
                      }`}
                    >
                      {rule.enabled ? (
                        <ToggleRight className="w-5 h-5" />
                      ) : (
                        <ToggleLeft className="w-5 h-5" />
                      )}
                    </button>
                    <button
                      onClick={() => handleDeleteRule(rule.id)}
                      className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              );
            })
          )}
        </div>
      </div>

      {/* Analytics */}
      {analytics && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Analytics (Last 24 Hours)</h2>

          <div className="grid grid-cols-4 gap-4 mb-6">
            <div className="bg-gray-50 rounded-lg p-4">
              <div className="text-2xl font-bold text-gray-900">{analytics.totalEvaluations.toLocaleString()}</div>
              <div className="text-sm text-gray-500">Total Evaluations</div>
            </div>
            <div className="bg-green-50 rounded-lg p-4">
              <div className="text-2xl font-bold text-green-600">{analytics.enabledCount.toLocaleString()}</div>
              <div className="text-sm text-gray-500">Enabled</div>
            </div>
            <div className="bg-red-50 rounded-lg p-4">
              <div className="text-2xl font-bold text-red-600">{analytics.disabledCount.toLocaleString()}</div>
              <div className="text-sm text-gray-500">Disabled</div>
            </div>
            <div className="bg-blue-50 rounded-lg p-4">
              <div className="text-2xl font-bold text-blue-600">{analytics.enabledPercentage}%</div>
              <div className="text-sm text-gray-500">Actual Rollout</div>
            </div>
          </div>

          <div className="grid grid-cols-3 gap-6">
            <div className="col-span-2">
              <h3 className="text-sm font-medium text-gray-700 mb-2">Evaluations Over Time</h3>
              <ResponsiveContainer width="100%" height={200}>
                <LineChart data={timeSeriesData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                  <XAxis dataKey="time" tick={{ fontSize: 12 }} stroke="#9ca3af" />
                  <YAxis tick={{ fontSize: 12 }} stroke="#9ca3af" />
                  <Tooltip />
                  <Line type="monotone" dataKey="enabled" stroke="#22c55e" strokeWidth={2} dot={false} />
                  <Line type="monotone" dataKey="disabled" stroke="#ef4444" strokeWidth={2} dot={false} />
                </LineChart>
              </ResponsiveContainer>
            </div>
            <div>
              <h3 className="text-sm font-medium text-gray-700 mb-2">Distribution</h3>
              <ResponsiveContainer width="100%" height={200}>
                <PieChart>
                  <Pie
                    data={pieData}
                    cx="50%"
                    cy="50%"
                    innerRadius={40}
                    outerRadius={70}
                    dataKey="value"
                    label={({ name, percent }) => `${name} ${((percent ?? 0) * 100).toFixed(0)}%`}
                  >
                    {pieData.map((_, index) => (
                      <Cell key={`cell-${index}`} fill={PIE_COLORS[index]} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
