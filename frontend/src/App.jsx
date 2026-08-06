import { BrowserRouter, Route, Routes } from 'react-router-dom';
import AppShell from '@/components/layout/AppShell';
import SystemCheck from '@/pages/SystemCheck';

export default function App() {
  return (
    <BrowserRouter>
      <AppShell>
        <Routes>
          <Route path="/" element={<SystemCheck />} />
        </Routes>
      </AppShell>
    </BrowserRouter>
  );
}
