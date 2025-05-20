import { useState, useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import Loading from '../components/Loading'; 
import {checkAuthToken} from '../apicalls/authAPIs'
import {getErrorMessage} from './handleErrorMssg';

const ProtectedRoute = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const checkAuth = async () => {
      try {
        const response = await checkAuthToken(); 
        if(response.success === false) {
            const error=getErrorMessage(response.error, "Authentication check failed");
            console.error(error); // Log the error message
            setIsAuthenticated(false);
            setIsLoading(false);
            return;
        }

          const data = response.data
          setIsAuthenticated(data.authenticated === true);
        
      } catch (error) {
        console.error('Authentication check failed:', error);
        setIsAuthenticated(false);
      } finally {
        setIsLoading(false);
      }
    };

    checkAuth();
  }, []);

  if (isLoading) return <Loading />;
  if (!isAuthenticated) return <Navigate to="/auth" />;
  return children;
};

export default ProtectedRoute;