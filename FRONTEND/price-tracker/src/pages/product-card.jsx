

import { motion } from "framer-motion"
import Image from "next/image"
import { ExternalLink } from "lucide-react"

import styles from "./product-card.module.css"

export default function ProductCard({ product }) {
  return (
    <motion.div
      className={styles.card}
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4 }}
    >
      <div className={styles.imageContainer}>
        <Image
          src={product.image || "/placeholder.svg"}
          alt={product.name}
          width={100}
          height={100}
          className={styles.image}
        />
      </div>
      <div className={styles.content}>
        <h3 className={styles.name}>{product.name}</h3>
        <div className={styles.store}>{product.store}</div>
        <div className={styles.priceContainer}>
          <div className={styles.currentPrice}>${product.currentPrice.toFixed(2)}</div>
          {product.originalPrice && <div className={styles.originalPrice}>${product.originalPrice.toFixed(2)}</div>}
          {product.discount && <div className={styles.discount}>-{product.discount}%</div>}
        </div>
        <a href={product.url} target="_blank" rel="noopener noreferrer" className={styles.link}>
          View Product <ExternalLink className={styles.icon} />
        </a>
      </div>
    </motion.div>
  )
}

