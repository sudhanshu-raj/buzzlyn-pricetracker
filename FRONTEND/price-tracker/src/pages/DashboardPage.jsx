"use client"

import { useState, useEffect } from "react"
import style from "./DashboardPage.module.css"
import Navbar from "../components/Navbar"
import Footer from "../components/Footer"
import DashboardProductDetails from "../components/DashboardProductDetails"
import { fetchUserConfig, fetchPriceHistory, deleteTrackedProduct } from "../apicalls/scraperAPIs"
import { getErrorMessage } from "../services/handleErrorMssg"
import { truncateText, formatPrice } from "../utils/helperFunctions"
import { useProducts } from "../context/ProductContext"
import SearchBar from "../components/SearchBar"
import Loading from "../components/Loading"

const StatCard = ({ value, label }) => (
  <div className={style["stat-card"]}>
    <h2>{value}</h2>
    <p>{label}</p>
  </div>
)

const ProductCard = ({ product }) => {
  const mockProduct = {
    id: "prod-" + Math.random().toString(36).substr(2, 9),
    title: "Sony WH-1000XM4 Wireless Noise Cancelling Headphones",
    url: "https://www.amazon.in",
    image: "/placeholder.svg?height=300&width=300",
    currentPrice: 278.0,
    originalPrice: 349.99,
    discount: 20,
    store: "Amazon",
    rating: 4.7,
    reviewCount: 28456,
    inStock: true,
    priceHistory: [
      { date: "2025-01-01", price: 349.99 },
      { date: "2025-01-01", price: 349.99 },
      { date: "2025-02-01", price: 349.99 },
      { date: "2025-02-01", price: 349.99 },
      { date: "2025-03-01", price: 349.99 },
      { date: "2025-04-01", price: 278.0 },
      { date: "2025-04-10", price: 258.0 },
      { date: "2025-04-20", price: 250.0 },
      { date: "2025-05-01", price: 258.0 },
      { date: "2025-05-05", price: 278.0 },
    ],
    //priceHistory: priceHistory,
  }

  const { removeProduct } = useProducts()

  const [productData, setProductData] = useState(product)
  const [showDetails, setShowDetails] = useState(false)
  const [activeTab, setActiveTab] = useState("overview")
  const [error, setError] = useState("")
  const [configData, setConfigData] = useState(null)
  const [isConfigLoading, setIsConfigLoading] = useState(false)

  const handleEditClick = async () => {
    setShowDetails(true)
    setActiveTab("notifications")
    setIsConfigLoading(true)
    setError("")

    try {
      const response = await fetchUserConfig(product.id)
      if (response.success === false) {
        const errorMsg = getErrorMessage(response.error, "Failed to fetch user products")
        console.error("Error fetching product config:", errorMsg)
        setError(errorMsg)
        return
      }
      const config = response.data
      setConfigData(config)
    } catch (error) {
      console.error("Error fetching product config:", error)
      setError("Failed to fetch product config")
    } finally {
      setIsConfigLoading(false)
    }
  }

  const handleShowData = async () => {
    setShowDetails(true)
    setActiveTab("overview")
    setError("")

    try {
      const response = await fetchPriceHistory(product.id)
      console.log("Price history response:", response)

      if (response.success === false) {
        setError(getErrorMessage(response.error, "Failed to fetch Data"))
        return
      }

      // Transform data
      const priceHistoryData = response.data.priceHistory.map((item) => ({
        date: item.date,
        price: Number.parseFloat(item.price.replace(/[^\d.]/g, "")),
      }))

      // Properly update state with a new object
      setProductData({
        ...productData,
        priceHistory: priceHistoryData,
      })
    } catch (error) {
      console.error("Error fetching price history:", error)
      setError("Failed to fetch price history data")
    }
  }

  const handleDeleteTracker = async () => {
    try {
      const response = await deleteTrackedProduct(product.id)
      if (response.success === false) {
        const errorMsg = getErrorMessage(response.error, "Failed to delete tracker")
        console.error("Error deleting tracker:", errorMsg)
        setError(errorMsg)
        return
      }
      removeProduct(product.id)
    } catch (error) {
      console.error("Error deleting tracker:", error)
      setError("Failed to delete tracker")
    }
  }

  const closeDetailsModal = () => {
    setShowDetails(false)
    setConfigData(null) // Reset config when closing
  }

  return (
    <div className={style["product-card"]}>
      <div className={style["product-image"]}>
        <img
          src={product.image || "/placeholder.svg?height=300&width=300"}
          alt={product.name}
          className={style.image}
          onError={(e) => {
            e.target.onerror = null
            e.target.src = "/placeholder.svg?height=300&width=300"
          }}
        />
      </div>
      <div className={style["product-info"]}>
        <h3 title={product.name}>{truncateText(product.name, 60)}</h3>
        <p>{product.description}</p>

        <div className={style.brandStockSection}>
          <span className={style.store}>{product.store}</span>

          {/* <div className={style.availability}>
            {product.stock_status ? (
              <span className={style.inStock}>
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  className={style.stockIcon}
                >
                  <path d="M20 6 9 17l-5-5" />
                </svg>
                In Stock
              </span>
            ) : (
              <span className={style.outOfStock}>
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  className={style.stockIcon}
                >
                  <line x1="18" y1="6" x2="6" y2="18" />
                  <line x1="6" y1="6" x2="18" y2="18" />
                </svg>
                Out of Stock
              </span>
            )}
          </div> */}
        </div>

        <div className={style["price-info"]}>
          <span className={style["current-price"]}>{formatPrice(product.currentPrice, product.currency)}</span>
          <span className={style["original-price"]}>{formatPrice(product.originalPrice, product.currency)}</span>
          <span className={style.discount}>{product.discount}</span>
        </div>

        <div className={style["product-actions"]}>
          <button className={style.view} onClick={handleShowData}>
            {/* <img className={style.icon} src={Eye || "/placeholder.svg"} alt="View" /> */}
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="#3b82f6" viewBox="0 0 24 24">
              <path d="M12 4.5C7.305 4.5 3.135 7.445 1.5 12c1.635 4.555 5.805 7.5 10.5 7.5s8.865-2.945 10.5-7.5c-1.635-4.555-5.805-7.5-10.5-7.5zm0 12a4.5 4.5 0 1 1 0-9 4.5 4.5 0 0 1 0 9z" />
              <circle cx="12" cy="12" r="2.5" />
            </svg>
          </button>
          <button className={style.edit} onClick={handleEditClick}>
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="#3b82f6" viewBox="0 0 24 24">
              <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04a1.003 1.003 0 0 0 0-1.41l-2.34-2.34a1.003 1.003 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z" />
            </svg>
          </button>
          <button className={style.delete} onClick={handleDeleteTracker}>
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="#3b82f6" viewBox="0 0 24 24">
              <path d="M9 3h6a1 1 0 0 1 1 1v1h5v2h-1v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V7H3V5h5V4a1 1 0 0 1 1-1zm1 3V5h4v1h-4zm-2 4v10h2V10H8zm4 0v10h2V10h-2zm4 0v10h2V10h-2z" />
            </svg>
          </button>
        </div>

        <p className={style.target}>
          Goal : {" "}
          {Object.entries(product.target).map(([targetName, isCompleted]) => (
            <span className={style.store} key={targetName}>
              {targetName}
              {isCompleted && (
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  className={style.stockIcon}
                >
                  <path d="M20 6 9 17l-5-5" />
                </svg>
              )}
            </span>
          ))}
        </p>

        <p className={style.trackerStatus}>
          Tracker Status :{" "}
          {
            <span className={style.trackerStatusValue}>
              {product.running_status ? (
                <span className={style.inStock}>Running</span>
              ) : (
                <span className={style.outOfStock}>STOPPED</span>
              )}
            </span>
          }
        </p>
      </div>

      {showDetails && (
        <div className={style.modalBackdrop} onClick={() => setShowDetails(false)}>
          <div
            className={style.modalContent}
            onClick={(e) => e.stopPropagation()} // Prevent clicks inside modal from closing it
          >
            <button className={style.closeButton} onClick={closeDetailsModal}>
              ✕
            </button>
            {configData === "notifications" ? (
              <DashboardProductDetails
                product={productData}
                configData={configData}
                setConfigData={setConfigData}
                isConfigLoading={isConfigLoading}
                activeTab={activeTab}
                onClose={closeDetailsModal}
              />
            ) : (
              <DashboardProductDetails
                product={productData}
                configData={configData}
                setConfigData={setConfigData}
                isConfigLoading={isConfigLoading}
                activeTab={activeTab}
                onClose={closeDetailsModal}
              />
            )}
          </div>
        </div>
      )}
    </div>
  )
}

function DashboardPage() {
  const { products, isLoading, error, refreshProducts } = useProducts()

  const stats = {
    productsTracked: 4,
    potentialSavings: 573.99,
    targetsmet: 0,
  }

  const [theme, setTheme] = useState("dark")
  const [filteredProducts, setFilteredProducts] = useState([])

  const toggleTheme = () => {
    const newTheme = theme === "dark" ? "light" : "dark"
    setTheme(newTheme)
    document.documentElement.setAttribute("data-theme", newTheme)
  }

  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme)
    setFilteredProducts(products)
  }, [theme, products])

  return (
    <>
      <Navbar />

      <div id={style.app}>
        {/* {isLoading && <div className={style.loading}>Loading your products...</div>} */}
       

        <header>
          <h1>Your Dashboard</h1>
        </header>

        <SearchBar
          onSearch={(filteredProducts) => {
            // Update the displayed products without changing the original products array
            setFilteredProducts(filteredProducts)
          }}
          products={products}
        />
{isLoading && <Loading />}
{error && <div className={style.error}>{error}</div>}


        {!isLoading && (
          <>
           
            {/* <div className={style.stats}>
              <StatCard value={stats.productsTracked} label="Products Tracked" />
              <StatCard value={`$${stats.potentialSavings}`} label="Potential Savings" />
              <StatCard value={stats.targetsmet} label="Price Targets Met" />
            </div> */}

            <div className={style["product-grid"]}>
              {filteredProducts.map((product, index) => (
                <ProductCard key={index} product={product} />
              ))}
            </div>
          </>
        )}
      </div>
      <Footer />
    </>
  )
}

export default DashboardPage
