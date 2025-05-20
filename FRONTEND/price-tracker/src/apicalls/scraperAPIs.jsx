import axios from "axios";
import { ENDPOINTS } from "./apiConfig";


const fetchProduct=async (url) => {
    try {
        const response = await axios.post(ENDPOINTS.PRODUCT_SCRAPE_URL, 
            {
                url: url,
            },
            {
          withCredentials: true ,
        });
        return {
            success: true,
            data: response.data,
        };
    } catch (error) {
        console.error("Error fetching product:", error);
        if (error.response?.data) {
            // Return the error data from the server
            return {
                success: false,
                error: error.response.data,
            };
        }
        return {
            success: false,
            error: error.message || "Failed to process request",
        };
    }
}

/**
 * 
 * @param {object} TrackingRequest 
 * @returns promise
 * @description This function is used to track the product price and send the request to the backend.
 */
const trackProduct=async (TrackingRequest) => {
    try {
        const response = await axios.post(ENDPOINTS.PRODUCT_TRACK_URL, TrackingRequest, {
          withCredentials: true ,
        });
        return {
            success: true,
            data: response.data,
        };
    } catch (error) {
        console.error("Error tracking product:", error);
        if (error.response?.data) {
            // Return the error data from the server
            return {
                success: false,
                error: error.response.data,
            };
        }
        return {
            success: false,
            error: error.message || "Failed to process request",
        };
    }
}

 const fetchUserTrackedProducts=async (request) => {
    try {
        const response = await axios.post(ENDPOINTS.FETCH_USER_TRACKED_PRODUCTS_URL, request, {
          withCredentials: true ,
        });
        return {
            success: true,
            data: response.data,
        };
    } catch (error) {
        console.error("Error fetching tracked products:", error);
        if (error.response?.data) {
            // Return the error data from the server
            return {
                success: false,
                error: error.response.data,
            };
        }
        return {
            success: false,
            error: error.message || "Failed to process request",
        };
    }
}

const fetchUserConfig=async (id) => {
    try {
        const response = await axios.get(`${ENDPOINTS.FETCH_USER_CONFIG_URL}?id=${id}`, {
          withCredentials: true ,
        });
        return {
            success: true,
            data: response.data,
        };
    } catch (error) {
        console.error("Error fetching user config:", error);
        if (error.response?.data) {
            // Return the error data from the server
            return {
                success: false,
                error: error.response.data,
            };
        }
        return {
            success: false,
            error: error.message || "Failed to process request",
        };
    }
}

//here userConfig is an object containing the user configuration data to be updated.
const updateUserConfig=async (userConfig) => {
    try {
        const response = await axios.post(ENDPOINTS.UPDATE_USER_CONFIG_URL, userConfig, {
          withCredentials: true ,
        });
        return {
            success: true,
            data: response.data,
        };
    } catch (error) {
        console.error("Error updating user config:", error);
        if (error.response?.data) {
            // Return the error data from the server
            return {
                success: false,
                error: error.response.data,
            };
        }
        return {
            success: false,
            error: error.message || "Failed to process request",
        };
    }
}

const fetchPriceHistory=async (productId) => {
    try {
        const response = await axios.get(`${ENDPOINTS.FETCH_PRICE_HISTORY_URL}?id=${productId}`, {
          withCredentials: true ,
        });
        return {
            success: true,
            data: response.data,
        };
    } catch (error) {
        console.error("Error fetching price history:", error);
        if (error.response?.data) {
            // Return the error data from the server
            return {
                success: false,
                error: error.response.data,
            };
        }
        return {
            success: false,
            error: error.message || "Failed to process request",
        };
    }
}

const deleteTrackedProduct=async (trackerId) => {
    try {
        const response = await axios.post(ENDPOINTS.DELETE_TRACKED_PRODUCT_URL,{
            id: trackerId,
        }, {
          withCredentials: true ,
        });
        return {
            success: true,
            data: response.data,
        };
    } catch (error) {
        console.error("Error deleting tracked product:", error);
        if (error.response?.data) {
            // Return the error data from the server
            return {
                success: false,
                error: error.response.data,
            };
        }
        return {
            success: false,
            error: error.message || "Failed to process request",
        };
    }
}

const fetchTrackerByProductId=async (userRequest) =>{
    try {
        const response = await axios.post(ENDPOINTS.FETCH_TRACKER_BY_PRODUCT_ID_URL, userRequest, {
          withCredentials: true ,
        });
        return {
            success: true,
            data: response.data,
        };
    } catch (error) {
        console.error("Error fetching tracker by product ID:", error);
        if (error.response?.data) {
            // Return the error data from the server
            return {
                success: false,
                error: error.response.data,
            };
        }
        return {
            success: false,
            error: error.message || "Failed to process request",
        };
    }
}

export {
    fetchProduct,
    trackProduct,
    fetchUserTrackedProducts,
    fetchUserConfig,
    updateUserConfig,
    fetchPriceHistory,
    deleteTrackedProduct,
    fetchTrackerByProductId
}