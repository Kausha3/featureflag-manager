import { BrowserRouter as Router, Routes, Route, NavLink } from 'react-router-dom';
import { Flag, TestTube } from 'lucide-react';
import FlagList from './components/FlagList';
import FlagDetails from './components/FlagDetails';
import FlagTester from './components/FlagTester';

function App() {
  return (
    <Router>
      <div className="min-h-screen bg-gray-100">
        {/* Header */}
        <header className="bg-white border-b border-gray-200 sticky top-0 z-40">
          <div className="max-w-6xl mx-auto px-4 py-4 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className="w-10 h-10 bg-gradient-to-br from-blue-500 to-purple-600 rounded-lg flex items-center justify-center">
                <Flag className="w-6 h-6 text-white" />
              </div>
              <div>
                <h1 className="text-xl font-bold text-gray-900">FeatureFlag Manager</h1>
                <p className="text-xs text-gray-500">Control your feature rollouts</p>
              </div>
            </div>
            <nav className="flex items-center gap-1">
              <NavLink
                to="/"
                end
                className={({ isActive }) =>
                  `flex items-center gap-2 px-4 py-2 rounded-lg transition ${
                    isActive
                      ? 'bg-blue-100 text-blue-700'
                      : 'text-gray-600 hover:bg-gray-100'
                  }`
                }
              >
                <Flag className="w-4 h-4" />
                Flags
              </NavLink>
              <NavLink
                to="/tester"
                className={({ isActive }) =>
                  `flex items-center gap-2 px-4 py-2 rounded-lg transition ${
                    isActive
                      ? 'bg-blue-100 text-blue-700'
                      : 'text-gray-600 hover:bg-gray-100'
                  }`
                }
              >
                <TestTube className="w-4 h-4" />
                Tester
              </NavLink>
            </nav>
          </div>
        </header>

        {/* Main Content */}
        <main className="max-w-6xl mx-auto px-4 py-8">
          <Routes>
            <Route path="/" element={<FlagList />} />
            <Route path="/flags/:id" element={<FlagDetails />} />
            <Route path="/tester" element={<FlagTester />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;
