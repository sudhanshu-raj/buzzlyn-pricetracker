import { createContext, useState, useContext, useEffect, useCallback } from 'react';
import { fetchUserTrackedProducts } from '../apicalls/scraperAPIs';
import { getErrorMessage } from '../services/handleErrorMssg';
import { useAuth } from './AuthContext';


const ProductContext = createContext();

export const useProducts = () => {
  const context = useContext(ProductContext);
  if (!context) {
    throw new Error('useProducts must be used within a ProductProvider');
  }
  return context;
};

export function ProductProvider({ children }) {
  const { user,isAuthenticated } = useAuth();
  const [products, setProducts] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [isInitialized, setIsInitialized] = useState(false);
  const [lastFetchTime, setLastFetchTime] = useState(null);



  const fetchProducts = useCallback(async (force=false) => {
    if (!user || !user.email) return;

    if (!force && isInitialized && lastFetchTime && (Date.now() - lastFetchTime < 60000)) {
        console.log("Skipping fetch - already initialized and fetched recently");
        return;
    }

    console.log("Fetching products for", user.email);
    setIsLoading(true);
    try {
      const fetchUserProductsRequest = {
        email: user.email,
        phoneNumber: user.number,
      };
      
      const response = await fetchUserTrackedProducts(fetchUserProductsRequest);
      console.log("Product fetch response:", response);
      if (response.success === false) {
        const errorMsg = getErrorMessage(response.error, "Failed to fetch user products");
        setError(errorMsg);
        return;
      }
      
      const productsList = response.data.map((product) => ({
        id: product.id,
        name: product.productName,
        image: product.imageURL || "/placeholder.svg?height=300&width=300",
        description: null,
        url: product.productURL,
        ratings:product.ratings,
        reviews: product.reviews,
        currency: product.currency,
        store: product.brand,
        currentPrice: product.price,
        originalPrice: product.mrp,
        discount: product.price < product.mrp
          ? (((product.mrp - product.price) / product.mrp) * 100).toFixed(2)
          : 0,
        target: product.userTargetStatus,
        stock_status: product.stock_status === "in_stock",
        running_status: product.running_status==="TRACKING",
      }));
      console.log("Fetched products:", productsList);
      setProducts(productsList);
      setIsInitialized(true);
      setLastFetchTime(Date.now());
    } catch (error) {
      console.error("Error fetching user products:", error);
      setError("Failed to fetch user products");
    } finally {
      setIsLoading(false);
    }
  },  [user?.email, user?.number]);

  useEffect(() => {
    console.log("Auth status changed:", isAuthenticated);
    if (!isAuthenticated) {
      console.log("User logged out, clearing products");
      setProducts([]);
      setIsInitialized(false);
      setLastFetchTime(null);
    }
  }, [isAuthenticated]);

  // This effect handles the fetch logic
  useEffect(() => {
    // User is logged in, has email, and we haven't fetched yet
    if (isAuthenticated && user?.email) {
      console.log("User authenticated with email, fetching products");
      fetchProducts();
    }
  }, [fetchProducts, isAuthenticated, user?.email]);


  // Function to add a product to the list
  const addProduct = (product) => {
    console.log("Adding product:", product);
    
    setProducts(prevProducts => {
      // Check if product with same ID or URL already exists
      const existingProduct = prevProducts.find(
        p => p.id === product.id || p.url === product.url
      );
      
      if (existingProduct) {
        console.log("Product already exists - replacing with updated version:", existingProduct.id);
        // Replace existing product with new one
        return prevProducts.map(p => 
          (p.id === product.id || p.url === product.url) ? product : p
        );
      }
      
      console.log("Adding new product to list");
      return [...prevProducts, product]; // Add new product
    });
    
    // Force context subscribers to refresh by setting a timestamp
    setLastFetchTime(Date.now());
  };

  // Function to update a product in the list
  const updateProduct = (updatedProduct) => {
    setProducts(prevProducts => 
      prevProducts.map(product => 
        product.id === updatedProduct.id ? updatedProduct : product
      )
    );
  };

  // Function to remove a product from the list
  const removeProduct = (productId) => {
    setProducts(prevProducts => 
      prevProducts.filter(product => product.id !== productId)
    );
  };

  // Function to force refresh products from server
  const refreshProducts = () => {
    fetchProducts(true);
  };

  return (
    <ProductContext.Provider value={{
      products,
      isLoading,
      error,
      addProduct,
      updateProduct,
      removeProduct,
      refreshProducts
    }}>
      {children}
    </ProductContext.Provider>
  );
}

export default ProductContext;