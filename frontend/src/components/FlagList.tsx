import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Flag, Plus, ToggleLeft, ToggleRight, Trash2, Settings, Loader2 } from 'lucide-react';
import type { Flag as FlagType } from '../types';
import { getFlags, toggleFlag, deleteFlag } from '../api/flagApi';
import FlagModal from './FlagModal';

export default function FlagList() {
  const [flags, setFlags] = useState<FlagType[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showModal, setShowModal] = useState(false);
  const [editingFlag, setEditingFlag] = useState<FlagType | null>(null);

  useEffect(() => {
    loadFlags();
  }, []);

  const loadFlags = async () => {
    try {
      setLoading(true);
      const data = await getFlags();
      setFlags(data);
      setError(null);
    } catch (err) {
      setError('Failed to load flags');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleToggle = async (flag: FlagType) => {
    try {
      await toggleFlag(flag.id, !flag.enabled);
      setFlags(flags.map(f =>
        f.id === flag.id ? { ...f, enabled: !f.enabled } : f
      ));
    } catch (err) {
      console.error('Failed to toggle flag:', err);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to delete this flag?')) return;

    try {
      await deleteFlag(id);
      setFlags(flags.filter(f => f.id !== id));
    } catch (err) {
      console.error('Failed to delete flag:', err);
    }
  };

  const handleCreateOrUpdate = () => {
    setShowModal(false);
    setEditingFlag(null);
    loadFlags();
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
          <Flag className="w-7 h-7" />
          Feature Flags
        </h1>
        <button
          onClick={() => setShowModal(true)}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
        >
          <Plus className="w-5 h-5" />
          Create Flag
        </button>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
          {error}
        </div>
      )}

      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-4 text-left text-sm font-semibold text-gray-900">Name</th>
              <th className="px-6 py-4 text-left text-sm font-semibold text-gray-900">Description</th>
              <th className="px-6 py-4 text-center text-sm font-semibold text-gray-900">Status</th>
              <th className="px-6 py-4 text-center text-sm font-semibold text-gray-900">Rollout</th>
              <th className="px-6 py-4 text-center text-sm font-semibold text-gray-900">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {flags.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-6 py-12 text-center text-gray-500">
                  No feature flags yet. Create one to get started!
                </td>
              </tr>
            ) : (
              flags.map((flag) => (
                <tr key={flag.id} className="hover:bg-gray-50 transition">
                  <td className="px-6 py-4">
                    <Link
                      to={`/flags/${flag.id}`}
                      className="font-mono text-blue-600 hover:text-blue-800 font-medium"
                    >
                      {flag.name}
                    </Link>
                  </td>
                  <td className="px-6 py-4 text-gray-600 max-w-xs truncate">
                    {flag.description || '—'}
                  </td>
                  <td className="px-6 py-4 text-center">
                    <button
                      onClick={() => handleToggle(flag)}
                      className={`inline-flex items-center gap-1 px-3 py-1 rounded-full text-sm font-medium transition ${
                        flag.enabled
                          ? 'bg-green-100 text-green-700 hover:bg-green-200'
                          : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                      }`}
                    >
                      {flag.enabled ? (
                        <>
                          <ToggleRight className="w-4 h-4" />
                          ON
                        </>
                      ) : (
                        <>
                          <ToggleLeft className="w-4 h-4" />
                          OFF
                        </>
                      )}
                    </button>
                  </td>
                  <td className="px-6 py-4 text-center">
                    <span className="inline-flex items-center px-3 py-1 bg-blue-100 text-blue-700 rounded-full text-sm font-medium">
                      {flag.rolloutPercentage}%
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex items-center justify-center gap-2">
                      <Link
                        to={`/flags/${flag.id}`}
                        className="p-2 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition"
                        title="View Details"
                      >
                        <Settings className="w-5 h-5" />
                      </Link>
                      <button
                        onClick={() => handleDelete(flag.id)}
                        className="p-2 text-gray-500 hover:text-red-600 hover:bg-red-50 rounded-lg transition"
                        title="Delete"
                      >
                        <Trash2 className="w-5 h-5" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {showModal && (
        <FlagModal
          flag={editingFlag}
          onClose={() => {
            setShowModal(false);
            setEditingFlag(null);
          }}
          onSave={handleCreateOrUpdate}
        />
      )}
    </div>
  );
}
