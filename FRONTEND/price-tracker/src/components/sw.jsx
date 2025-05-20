/**
 * Registers the service worker for the application
 * @returns {Promise<ServiceWorkerRegistration>} The service worker registration
 */
export const registerServiceWorker = async () => {
    if ('serviceWorker' in navigator) {
      try {
        // Changed to service-worker.js to match your actual file
        const reg = await navigator.serviceWorker.register('/service-worker.js', { scope: '/' });
        console.log('ServiceWorker registration successful with scope: ', reg.scope);
        return reg;
      } catch (error) {
        console.error('ServiceWorker registration failed: ', error);
        throw error; // Rethrow the error to handle it outside
      }
    } else {
      console.log('Service workers aren\'t supported in this browser.');
      throw new Error('Service workers aren\'t supported in this browser.');
    }
  };
  
  /**
   * Converts a base64 string to a Uint8Array (for VAPID keys)
   * @param {string} base64String - Base64 encoded string
   * @returns {Uint8Array} Converted array
   */
  export const urlB64ToUint8Array = (base64String) => {
    const padding = '='.repeat((4 - (base64String.length % 4)) % 4);
    const base64 = (base64String + padding)
      .replace(/\-/g, '+')
      .replace(/\_/g, '/');
    
    const rawData = window.atob(base64);
    const outputArray = new Uint8Array(rawData.length);
    
    for (let i = 0; i < rawData.length; ++i) {
      outputArray[i] = rawData.charCodeAt(i);
    }
    return outputArray;
  };
  
  /**
   * Encodes a Uint8Array back to a base64 string
   * @param {Uint8Array} array - Uint8Array to encode
   * @returns {string} Base64 encoded string
   */
  export const encodeUint8Array = (array) => {
    const binaryString = Array.from(array)
      .map(byte => String.fromCharCode(byte))
      .join('');
    return btoa(binaryString);
  };
  
  /**
   * WebPushSubscription data structure
   * @typedef {Object} WebPushSubscription
   * @property {number} [id] - Optional ID field
   * @property {string} notificationEndPoint - Push endpoint URL
   * @property {string} publicKey - Public key
   * @property {string} auth - Auth token
   * @property {number} userId - User ID
   */
  
  /**
   * WebPushMessage data structure
   * @typedef {Object} WebPushMessage
   * @property {string} title - Notification title
   * @property {string} clickTarget - URL to open when notification is clicked
   * @property {string} message - Notification message content
   * @property {string} icon - URL to notification icon
   */