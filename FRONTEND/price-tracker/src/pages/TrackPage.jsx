"use client"

import { useState, useRef, useEffect } from "react"
import { motion, AnimatePresence } from "framer-motion"
import Navbar from "../components/Navbar.jsx"
import Footer from "../components/Footer.jsx"
import PriceTrackerForm from "../components/PriceTrackerForm.jsx"
import ProductDetails from "../components/ProductDetails.jsx"
import styles from "./TrackPage.module.css"

function TrackPage() {
  const [productDetails, setProductDetails] = useState(null)
  const [isLoading, setIsLoading] = useState(false)
  const formRef = useRef(null)
  const productRef = useRef(null)

  const handleProductFound = (product) => {
    setProductDetails(product)
    setIsLoading(false)
  }

  const handleReset = () => {
    setProductDetails(null)
    // Scroll back to center when resetting
    window.scrollTo({
      top: 0,
      behavior: "smooth",
    })
  }

  // Scroll to ensure product details are visible when they appear
  useEffect(() => {
    if (productDetails && productRef.current) {
      // Small delay to ensure animations have started
      setTimeout(() => {
        window.scrollTo({
          top: 0,
          behavior: "smooth",
        })
      }, 100)
    }
  }, [productDetails])

  return (
    <div className={styles.container}>
      <Navbar />
      <main className={styles.main}>
        <div className={styles.content}>
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            className={`${styles.wrapper} ${productDetails ? styles.withProduct : styles.withoutProduct}`}
          >
            {!productDetails && (
              <div className={styles.header}>
                <h1 className={styles.title}>Track Your Product</h1>
                <p className={styles.subtitle}>
                  Paste a product URL below to start tracking its price and get notified when it drops.
                </p>
              </div>
            )}

            <motion.div
              ref={formRef}
              layout
              transition={{
                type: "spring",
                stiffness: 300,
                damping: 30,
              }}
              className={`${styles.formWrapper} ${productDetails ? styles.compact : ""}`}
            >
              <PriceTrackerForm
                onProductFound={handleProductFound}
                onLoading={setIsLoading}
                compact={!!productDetails}
              />
            </motion.div>

            <AnimatePresence mode="wait">
              {productDetails && (
                <motion.div
                  ref={productRef}
                  key="product-details"
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -20 }}
                  transition={{ duration: 0.5 }}
                >
                  <ProductDetails product={productDetails} onReset={handleReset} />
                </motion.div>
              )}
            </AnimatePresence>

            {!productDetails && !isLoading && (
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ delay: 0.3, duration: 0.5 }}
                className={styles.emptyState}
              >
                <div className={styles.iconWrapper}>
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="32"
                    height="32"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    className={styles.icon}
                  >
                    <path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z" />
                  </svg>
                </div>
                <h3 className={styles.emptyTitle}>Start Saving Money Today</h3>
                <p className={styles.emptyText}>
                  Track prices from Amazon, Walmart, Best Buy, Target, and more. Get notified when prices drop to your
                  target.
                </p>
              </motion.div>
            )}
          </motion.div>
        </div>
      </main>
      <Footer />
    </div>
  )
}

export default TrackPage

