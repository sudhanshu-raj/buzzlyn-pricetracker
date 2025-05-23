import { Link } from "react-router-dom"
import styles from "./Footer.module.css"
import LogoIcon from "../assets/logoIcon.png"

function Footer() {
  return (
    <footer className={styles.footer}>
      <div className={styles.container}>
        <div className={styles.content}>
          <div className={styles.branding}>
            <Link to="/" className={styles.logo}>
              <img src={LogoIcon || "/placeholder.svg"} alt="Buzzlyn Logo" className={styles.logoIcon} />
              <span className={styles.logoText}>buzzlyn</span>
            </Link>
            <p className={styles.tagline}>Track prices, get alerts, and never overpay again.</p>
          </div>

          <div className={styles.links}>
            <Link to="/about" className={styles.link}>
              About Us
            </Link>
            <Link to="/contact" className={styles.link}>
              Contact Us
            </Link>
            <Link to="/privacypolicy" className={styles.link}>
              Privacy Policy
            </Link>
            <Link to="/termsandconditions" className={styles.link}>
              Terms & Conditions
            </Link>
          </div>
        </div>
      </div>
    </footer>
  )
}

export default Footer
