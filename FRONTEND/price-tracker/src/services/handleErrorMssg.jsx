/**
 * Extracts a readable error message from various error formats
 * @param {*} error - The error object or string from the API response
 * @param {string} fallbackMessage - Default message if no specific error is found
 * @returns {string} A human-readable error message
 */
export const getErrorMessage = (error, fallbackMessage = "An unknown error occurred") => {
    // If error is null or undefined
    if (!error) return fallbackMessage;
    
    // If error is a string, return it directly
    if (typeof error === 'string') return error;
    
    // If error is an object, try to extract the most relevant message
    if (typeof error === 'object') {
      // Spring Boot error format
      if (error.message) return error.message;
      if (error.error) return error.error;
      
      // Look for any property that might contain an error message
      for (const key of ['detail', 'description', 'reason', 'statusText']) {
        if (error[key]) return error[key];
      }
      
      // Check for nested errors
      if (error.errors && Array.isArray(error.errors) && error.errors.length > 0) {
        return error.errors[0].message || JSON.stringify(error.errors[0]);
      }
    }
    
    // If we can't extract a specific message, return the fallback
    return fallbackMessage;
  };