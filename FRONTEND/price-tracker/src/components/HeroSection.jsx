"use client"

import { motion } from "framer-motion"
import { Link } from "react-router-dom"
import styles from "./HeroSection.module.css"

function HeroSection() {
  return (
    <section className={styles.hero}>
      <div className={styles.container}>
        <div className={styles.content}>
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            className={styles.textContent}
          >
            <h1 className={styles.title}>
              Never Overpay <span className={styles.highlight}>Again</span>
            </h1>
            <p className={styles.subtitle}>
              Track prices across major retailers and get notified when prices drop. Save money on your favorite
              products with our smart price tracking technology.
            </p>
            <div className={styles.actions}>
              <Link to="/track" className={styles.primaryButton}>
                Start Tracking Now
              </Link>
              <a href="#how-it-works" className={styles.secondaryButton}>
                How It Works
              </a>
            </div>
            <div className={styles.stats}>
              {[
                { value: "50K+", label: "Active Users" },
                { value: "$2.5M", label: "Money Saved" },
                { value: "100K+", label: "Price Alerts" },
              ].map((stat, index) => (
                <div key={index} className={styles.stat}>
                  <div className={styles.statValue}>{stat.value}</div>
                  <div className={styles.statLabel}>{stat.label}</div>
                </div>
              ))}
            </div>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.5, delay: 0.2 }}
            className={styles.imageContainer}
          >
            <img
              src="/placeholder.svg?height=400&width=500"
              alt="Price tracking dashboard"
              className={styles.heroImage}
            />
          </motion.div>
        </div>
      </div>
    </section>
  )
}

export default HeroSection

