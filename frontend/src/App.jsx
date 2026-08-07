import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from '@/context/AuthContext';
import AppShell from '@/components/layout/AppShell';
import ProtectedRoute from '@/components/routing/ProtectedRoute';
import SystemCheck from '@/pages/SystemCheck';
import Login from '@/pages/Login';
import Register from '@/pages/Register';
import Dashboard from '@/pages/Dashboard';
import Compose from '@/pages/compose/Compose';
import TemplateList from '@/pages/templates/TemplateList';
import TemplateEditor from '@/pages/templates/TemplateEditor';
import RecipientList from '@/pages/recipients/RecipientList';
import RecipientImport from '@/pages/recipients/RecipientImport';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppShell>
          <Routes>
            <Route path="/" element={<SystemCheck />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />

            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <Dashboard />
                </ProtectedRoute>
              }
            />

            <Route
              path="/compose"
              element={
                <ProtectedRoute>
                  <Compose />
                </ProtectedRoute>
              }
            />

            <Route
              path="/templates"
              element={
                <ProtectedRoute>
                  <TemplateList />
                </ProtectedRoute>
              }
            />
            <Route
              path="/templates/new"
              element={
                <ProtectedRoute>
                  <TemplateEditor />
                </ProtectedRoute>
              }
            />
            <Route
              path="/templates/:id"
              element={
                <ProtectedRoute>
                  <TemplateEditor />
                </ProtectedRoute>
              }
            />

            <Route
              path="/recipients"
              element={
                <ProtectedRoute>
                  <RecipientList />
                </ProtectedRoute>
              }
            />
            <Route
              path="/recipients/import"
              element={
                <ProtectedRoute>
                  <RecipientImport />
                </ProtectedRoute>
              }
            />

            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </AppShell>
      </AuthProvider>
    </BrowserRouter>
  );
}