
import { useState } from "react"
import { motion } from "framer-motion"
import { useNavigate } from "react-router-dom"
import styles from "./PriceTrackerForm.module.css"
import { fetchProduct } from "../apicalls/scraperAPIs"
import { useAuth } from "../context/AuthContext";

function PriceTrackerForm({ onProductFound, onLoading, compact = false }) {
   const { isAuthenticated} = useAuth();
   const navigate = useNavigate(); 
  const [url, setUrl] = useState("")
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState("")

  const handleSubmit = async (e) => {
    e.preventDefault()

    if (!isAuthenticated) {
      // Redirect to the login page
      navigate("/auth", { state: { from: window.location.pathname } });
      return
    }

    if (!url) {
      setError("Please enter a product URL")
      return
    }

    setError("")
    setIsLoading(true)
    onLoading?.(true)

    const response = await fetchProduct(url)
    if (!response.success) {
      setError(response.error || "Failed to fetch product details")
      setIsLoading(false)
      onLoading?.(false)
      return
    }
    const rawProduct = response.data;
    const product={
      id: rawProduct.id,
      title: rawProduct.productName,
      url: rawProduct.productURL,
      image: rawProduct.imageURL || "/placeholder.svg?height=300&width=300",
      currency: rawProduct.currency,
      currentPrice: rawProduct.price,
      originalPrice: rawProduct.mrp,
      discount: rawProduct.price < rawProduct.mrp ? ((rawProduct.mrp - rawProduct.price) / rawProduct.mrp * 100).toFixed(2) : 0,
      store: rawProduct.brand,
      rating: rawProduct.ratings,
      reviewCount: rawProduct.reviews,
      inStock: rawProduct.stock_status==="in_stock",
      priceHistory: null,
    }

    
    setIsLoading(false)
    onLoading?.(false)
    onProductFound(product)

    // Reset form
    setUrl("")
  }

  return (
    <div className={`${styles.container} ${compact ? styles.compact : ""}`}>
      <form onSubmit={handleSubmit} className={styles.form}>
        <div className={styles.inputWrapper}>
          <input
            type="url"
            placeholder="Paste product URL from Amazon, Walmart, Best Buy, etc."
            className={styles.input}
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            disabled={isLoading}
          />
          <button type="submit" className={styles.button} disabled={isLoading}>
            {isLoading ? (
              <span className={styles.loader}>
                <svg
                  className={styles.spinner}
                  xmlns="http://www.w3.org/2000/svg"
                  width="24"
                  height="24"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <path d="M21 12a9 9 0 1 1-6.219-8.56" />
                </svg>
              </span>
            ) : (
              <>{compact ? "Track" : "Track Price"}</>
            )}
          </button>
        </div>

        {error && (
          <motion.p initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }} className={styles.error}>
            {error}
          </motion.p>
        )}

        {!compact && (
          <div className={styles.supportedStores}>
            <span className={styles.storesLabel}>Supported stores:</span>
            <div className={styles.storesList}>
              {["Amazon", "Walmart", "Best Buy", "Target", "eBay"].map((store) => (
                <span key={store} className={styles.store}>
                  {store}
                </span>
              ))}
            </div>
          </div>
        )}
      </form>
    </div>
  )
}

export default PriceTrackerForm

