import { Navbar } from "../components/Navbar"
import Footer from "../components/Footer"
import styles from "./AboutPage.module.css"
import bannerImage from "../assets/banner.png"
import { Link } from "react-router-dom"

export default function AboutPage() {
  return (
    <div className={styles.pageContainer}>
      <Navbar />
      <main className={styles.main}>
        {/* Hero Section */}
        <section className={styles.heroSection}>
          <div className={styles.container}>
            <div className={styles.heroContent}>
              <h1 className={styles.heroTitle}>
                About <span className={styles.brandName}>buzzlyn</span>
              </h1>
              <p className={styles.heroSubtitle}>Track prices, get alerts, and never overpay again.</p>
              {/* <div className={styles.heroImage}>
                <img
                  src="/placeholder.svg?height=300&width=600"
                  alt="Buzzlyn Price Tracking"
                  className={styles.heroImg}
                />
              </div> */}
            </div>
          </div>
        </section>

        {/* Our Story */}
        <section className={styles.storySection}>
          <div className={styles.container}>
            <div className={styles.storyGrid}>
              <div className={styles.storyContent}>
                <h2 className={styles.sectionTitle}>Our Story</h2>
                <div className={styles.storyText}>
                  <p>
                    Buzzlyn was founded with a simple mission: to help shoppers make smarter purchasing decisions and
                    never overpay for products again. We noticed that prices for the same items fluctuated wildly across
                    different retailers and even on the same site over time.
                  </p>
                  <p>
                    What started as a personal tool to track prices for our own shopping needs quickly grew into a
                    comprehensive platform that helps thousands of shoppers save money every day by monitoring global
                    e-commerce products.
                  </p>
                  <p>
                    <span>
                    Our team combines expertise in e-commerce, data science, and consumer advocacy to build the most
                    accurate and user-friendly price tracking service available for physical products worldwide.
                    </span>
                  </p>
                </div>
              </div>
              <div className={styles.bannerImage}>
                <img
                  src={bannerImage}
                  alt="Buzzlyn Price Tracking"
                  className={styles.bannerImg}
                />
              </div>
              {/* <div className={styles.statsContainer}>
                <div className={styles.statCard}>
                  <div className={styles.statIcon}>📊</div>
                  <h3 className={styles.statNumber}>50,000+</h3>
                  <p className={styles.statLabel}>Active Users</p>
                </div>
                <div className={styles.statCard}>
                  <div className={styles.statIcon}>💰</div>
                  <h3 className={styles.statNumber}>₹2.5M+</h3>
                  <p className={styles.statLabel}>Total Savings</p>
                </div>
                <div className={styles.statCard}>
                  <div className={styles.statIcon}>🔔</div>
                  <h3 className={styles.statNumber}>100,000+</h3>
                  <p className={styles.statLabel}>Price Alerts Sent</p>
                </div>
              </div> */}
            </div>
          </div>
        </section>

        {/* What We Do */}
        <section className={styles.servicesSection}>
          <div className={styles.container}>
            <h2 className={styles.sectionTitle}>What We Do</h2>
            <div className={styles.servicesGrid}>
              <div className={styles.serviceCard}>
                <div className={styles.serviceIcon}>🌍</div>
                <h3 className={styles.serviceTitle}>Global Product Tracking</h3>
                <p className={styles.serviceDescription}>
                  We monitor prices of physical products from e-commerce websites worldwide, giving you access to the
                  best deals across different platforms and regions.
                </p>
              </div>
              <div className={styles.serviceCard}>
                <div className={styles.serviceIcon}>🎯</div>
                <h3 className={styles.serviceTitle}>Smart Price Alerts</h3>
                <p className={styles.serviceDescription}>
                  Set your target prices and receive instant notifications via email, WhatsApp, SMS, or web push when
                  prices drop to your desired level.
                </p>
              </div>
              <div className={styles.serviceCard}>
                <div className={styles.serviceIcon}>🔒</div>
                <h3 className={styles.serviceTitle}>Privacy-First Approach</h3>
                <p className={styles.serviceDescription}>
                  We only send notifications with your explicit consent. You have full control over your communication
                  preferences and can modify them anytime.
                </p>
              </div>
            </div>
          </div>
        </section>

        {/* Our Values */}
        <section className={styles.valuesSection}>
          <div className={styles.container}>
            <h2 className={styles.sectionTitle}>Our Values</h2>
            <div className={styles.valuesGrid}>
              <div className={styles.valueCard}>
                <div className={styles.valueIcon}>❤️</div>
                <h3 className={styles.valueTitle}>Customer First</h3>
                <p className={styles.valueDescription}>
                  We put our users at the center of everything we build and every decision we make. Your savings and
                  satisfaction are our top priorities.
                </p>
              </div>
              <div className={styles.valueCard}>
                <div className={styles.valueIcon}>🛡️</div>
                <h3 className={styles.valueTitle}>Privacy & Security</h3>
                <p className={styles.valueDescription}>
                  We never sell your data and maintain the highest standards of security and privacy. Your information
                  is safe with us.
                </p>
              </div>
              <div className={styles.valueCard}>
                <div className={styles.valueIcon}>🔍</div>
                <h3 className={styles.valueTitle}>Transparency</h3>
                <p className={styles.valueDescription}>
                  We're open about how our service works, how we collect data, and how we help you save money. No hidden
                  agendas.
                </p>
              </div>
            </div>
          </div>
        </section>

        {/* Contact Section */}
        <section className={styles.contactSection}>
          <div className={styles.container}>
            <div className={styles.contactContent}>
              <h2 className={styles.sectionTitle}>Get in Touch</h2>
              <p className={styles.contactDescription}>
                Have questions about our service or need help with price tracking? We're here to help you save money and
                make smarter shopping decisions.
              </p>
              <div className={styles.contactInfo}>
                <div className={styles.contactItem}>
                  <div className={styles.contactIcon}>📧</div>
                  <div>
                    <h4>Email Support</h4>
                    <p>support@buzzlyn.com</p>
                  </div>
                </div>
                <div className={styles.contactItem}>
                  <div className={styles.contactIcon}>🇮🇳</div>
                  <div>
                    <h4>Based in India</h4>
                    <p>Serving customers globally</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* CTA Section */}
        <section className={styles.ctaSection}>
          <div className={styles.container}>
            <div className={styles.ctaContent}>
              <h2 className={styles.ctaTitle}>Ready to Start Saving?</h2>
              <p className={styles.ctaDescription}>
                Join thousands of smart shoppers who never overpay for their favorite products again.
              </p>
              <div className={styles.ctaButtons}>
                <Link to="/track" className={styles.primaryButton}>
                 Start Tracking Now
                </Link>
                <Link to="/contact" className={styles.secondaryButton}>
                 Contact Us
                </Link>
              </div>
            </div>
          </div>
        </section>
      </main>
      <Footer />
    </div>
  )
}
