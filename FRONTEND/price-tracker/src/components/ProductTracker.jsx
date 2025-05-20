import { useState } from "react"
import { motion } from "framer-motion"
import { Link } from "react-router-dom"
import styles from "./ProductTracker.module.css"

function ProductTracker() {
  const [url, setUrl] = useState("")

  const handleSubmit = (e) => {
    e.preventDefault()
    // Redirect to track page with the URL as a query parameter
    window.location.href = `/track?url=${encodeURIComponent(url)}`
  }

  return (
    <section className={styles.section} id="how-it-works">
      <div className={styles.container}>
        <div className={styles.header}>
          <h2 className={styles.title}>How It Works</h2>
          <p className={styles.subtitle}>Track prices from your favorite stores in three simple steps</p>
        </div>

        <div className={styles.steps}>
          {[
            {
              icon: "link",
              title: "Paste Product URL",
              description:
                "Simply paste the URL of any product from major retailers like Amazon, Walmart, Best Buy, and more.",
            },
            {
              icon: "bell",
              title: "Set Your Target Price",
              description:
                "Tell us how much you want to pay, and we'll notify you when the price drops to your target.",
            },
            {
              icon: "check",
              title: "Get Notified & Save",
              description: "Receive instant notifications via email or SMS when prices drop, so you never miss a deal.",
            },
          ].map((step, index) => (
            <motion.div
              key={index}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: index * 0.1 }}
              viewport={{ once: true }}
              className={styles.step}
            >
              <div className={styles.stepIcon}>
                {step.icon === "link" && (
                  <svg
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
                    <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71" />
                    <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71" />
                  </svg>
                )}
                {step.icon === "bell" && (
                  <svg
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
                    <path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9" />
                    <path d="M10.3 21a1.94 1.94 0 0 0 3.4 0" />
                  </svg>
                )}
                {step.icon === "check" && (
                  <svg
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
                    <path d="M20 6 9 17l-5-5" />
                  </svg>
                )}
              </div>
              <h3 className={styles.stepTitle}>{step.title}</h3>
              <p className={styles.stepDescription}>{step.description}</p>
            </motion.div>
          ))}
        </div>

        <div className={styles.trackerCard}>
          <div className={styles.trackerContent}>
            <h3 className={styles.trackerTitle}>Start Tracking Now</h3>
            <p className={styles.trackerDescription}>Paste a product URL below to start tracking its price</p>
            <form onSubmit={handleSubmit} className={styles.form}>
              <div className={styles.inputWrapper}>
                <input
                  type="url"
                  placeholder="https://www.amazon.com/product-url"
                  className={styles.input}
                  value={url}
                  onChange={(e) => setUrl(e.target.value)}
                  required
                />
                <button type="submit" className={styles.button}>
                  Track Price
                </button>
              </div>
            </form>
            <div className={styles.trackerFooter}>
              <p className={styles.trackerNote}>
                Already tracking products?{" "}
                <Link to="/dashboard" className={styles.link}>
                  View Dashboard
                </Link>
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}

export default ProductTracker

