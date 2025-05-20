import { createContext, useState, useContext, useEffect } from 'react';
import { checkAuthToken } from '../apicalls/authAPIs';
import { getErrorMessage } from '../services/handleErrorMssg';

// Create the context
const AuthContext = createContext();

// Custom hook for using the auth context
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

// Provider component
export function AuthProvider({ children }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [user, setUser] = useState(null);

  // Check authentication status on mount
  useEffect(() => {
    const checkAuth = async () => {
      try {
        const response = await checkAuthToken();
        
        if(response.success === false) {
          const error = getErrorMessage(response.error, "Authentication check failed");
          console.error(error);
          setIsAuthenticated(false);
          setIsLoading(false);
          return;
        }

        const data = response.data;
        setIsAuthenticated(data.authenticated === true);
        
        const userData={
            email:data.user.email,
            number:data.user.phoneNumber,
            firstName:data.user.firstName,
            avatarUrl: data.profilePic
            ? `data:image/png;base64,${response.data.profilePic}`
            : "/placeholder.svg?height=40&width=40",
        }
        setUser(userData);
      } catch (error) {
        console.error('Authentication check failed:', error);
        setIsAuthenticated(false);
      } finally {
        setIsLoading(false);
      }
    };

    checkAuth();
  }, []);

  // Function to update auth state after login
  const login = (userData) => {
    setIsAuthenticated(true);
    setUser(userData);
  };

  // Function to update auth state after logout
  const logout = () => {
    setIsAuthenticated(false);
    setUser(null);
  };

  // Values to be provided to consumers
  const value = {
    isAuthenticated,
    isLoading,
    user,
    setUser,
    setIsAuthenticated,
    login,
    logout
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export default AuthContext;