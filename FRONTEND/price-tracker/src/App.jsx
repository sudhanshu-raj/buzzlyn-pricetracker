import { Routes, Route } from "react-router-dom";
import HomePage from "./pages/HomePage.jsx";
import TrackPage from "./pages/TrackPage.jsx";
import DashboardPage from "./pages/DashboardPage.jsx";
import AuthPage from "./pages/AuthPage.jsx";
import AboutPage from "./pages/AboutPage.jsx";
import ContactPage from "./pages/ContactPage.jsx";
import { ThemeProvider } from "./components/ThemeProvider.jsx";
import OAuthCallback from "./services/OAuthCallback.jsx";
import { Navigate } from "react-router-dom";
import Loading from "./components/Loading";
import { AuthProvider, useAuth } from "./context/AuthContext";
import { ProductProvider } from "./context/ProductContext";
import SendNotificationForm from "./components/SampleWebPushDemo.jsx";
import ChartComponent from "./components/ChartBackend.jsx";

function AppRoutes() {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) return <Loading />;

  return (
    <Routes>
      <Route
        path="/"
        element={isAuthenticated ? <TrackPage /> : <HomePage />}
      />
      <Route path="/track" element={<TrackPage />} />
      <Route
        path="/dashboard"
        element={isAuthenticated ? <DashboardPage /> : <Navigate to="/auth" />}
      />
      <Route
        path="/auth"
        element={isAuthenticated ? <Navigate to="/track" /> : <AuthPage />}
      />
      <Route path="/about" element={<AboutPage />} />
      <Route path="/contact" element={<ContactPage />} />
      <Route path="/auth/callback" element={<OAuthCallback />} />
      {/* <Route path="/sampleWebPush" element={<SendNotificationForm />} />
      <Route path="/chart" element={<ChartComponent />} /> */}


    </Routes>
  );
}

function App() {
  return (
    <AuthProvider>
      <ProductProvider>
        <ThemeProvider>
          <AppRoutes />
        </ThemeProvider>
      </ProductProvider>
    </AuthProvider>
  );
}

export default App;
