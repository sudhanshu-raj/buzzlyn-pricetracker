import { Navbar } from "../components/Navbar"
import Footer  from "../components/Footer"
import styles from "./PrivacyPolicyPage.module.css"

export default function PrivacyPolicyPage() {
  return (
    <div className={styles.pageContainer}>
      <Navbar />
      <main className={styles.main}>
        <div className={styles.container}>
          <div className={styles.header}>
            <h1 className={styles.title}>Privacy Policy</h1>
            <p className={styles.lastUpdated}>Last updated: January 23, 2025</p>
          </div>

          <div className={styles.content}>
            <section className={styles.section}>
              <h2>Introduction</h2>
              <p>
                Welcome to Buzzlyn ("we," "our," or "us"). We are committed to protecting your privacy and ensuring the
                security of your personal information. This Privacy Policy explains how we collect, use, disclose, and
                safeguard your information when you use our price tracking service.
              </p>
            </section>

            <section className={styles.section}>
              <h2>Information We Collect</h2>

              <h3>Personal Information</h3>
              <ul>
                <li>Email address (for account creation and notifications)</li>
                <li>Phone number (for SMS/WhatsApp alerts)</li>
                <li>Name (for personalized communications)</li>
                <li>Communication preferences and consent settings</li>
              </ul>

              <h3>Product Tracking Data</h3>
              <ul>
                <li>Product URLs you choose to track</li>
                <li>Target prices you set for alerts</li>
                <li>Notification preferences (email, WhatsApp, web push)</li>
                <li>Tracking history and alert logs</li>
              </ul>

              <h3>Technical Information</h3>
              <ul>
                <li>IP address and device information</li>
                <li>Browser type and version</li>
                <li>Usage patterns and interaction data</li>
                <li>Cookies and similar tracking technologies</li>
              </ul>
            </section>

            <section className={styles.section}>
              <h2>How We Use Your Information</h2>
              <ul>
                <li>
                  <strong>Price Tracking:</strong> Monitor product prices and send you alerts when prices drop
                </li>
                <li>
                  <strong>Communication:</strong> Send notifications via your preferred channels (email, WhatsApp, web
                  push)
                </li>
                <li>
                  <strong>Service Improvement:</strong> Analyze usage patterns to enhance our tracking accuracy
                </li>
                <li>
                  <strong>Customer Support:</strong> Respond to your inquiries and provide assistance
                </li>
                <li>
                  <strong>Legal Compliance:</strong> Comply with applicable laws and regulations
                </li>
              </ul>
            </section>

            <section className={styles.section}>
              <h2>Consent and Communication Preferences</h2>
              <p>
                We obtain explicit consent before sending any notifications through email, WhatsApp, SMS, or web push
                notifications. You have full control over:
              </p>
              <ul>
                <li>Which notification channels you want to use</li>
                <li>Frequency of notifications</li>
                <li>Types of alerts you receive</li>
                <li>The ability to modify or withdraw consent at any time</li>
              </ul>
              <p>
                You can update your communication preferences or unsubscribe from all communications through your
                account settings or by contacting us at support@buzzlyn.com.
              </p>
            </section>

            <section className={styles.section}>
              <h2>Data Sharing and Disclosure</h2>
              <p>
                We do not sell, trade, or rent your personal information to third parties. We may share your information
                only in the following circumstances:
              </p>
              <ul>
                <li>
                  <strong>Service Providers:</strong> With trusted third-party services that help us operate our
                  platform
                </li>
                <li>
                  <strong>Legal Requirements:</strong> When required by law or to protect our rights and safety
                </li>
                <li>
                  <strong>Business Transfers:</strong> In connection with a merger, acquisition, or sale of assets
                </li>
              </ul>
            </section>

            <section className={styles.section}>
              <h2>Data Security</h2>
              <p>
                We implement appropriate technical and organizational measures to protect your personal information
                against unauthorized access, alteration, disclosure, or destruction. However, no method of transmission
                over the internet is 100% secure.
              </p>
            </section>

            <section className={styles.section}>
              <h2>Data Retention</h2>
              <p>
                We retain your personal information for as long as necessary to provide our services and comply with
                legal obligations. You can request deletion of your account and associated data at any time.
              </p>
            </section>

            <section className={styles.section}>
              <h2>Your Rights</h2>
              <p>You have the right to:</p>
              <ul>
                <li>Access your personal information</li>
                <li>Correct inaccurate or incomplete data</li>
                <li>Delete your account and personal information</li>
                <li>Withdraw consent for communications</li>
                <li>Export your data in a portable format</li>
              </ul>
            </section>

            <section className={styles.section}>
              <h2>Cookies and Tracking Technologies</h2>
              <p>
                We use cookies and similar technologies to enhance your experience, analyze usage patterns, and provide
                personalized content. You can control cookie settings through your browser preferences.
              </p>
            </section>

            <section className={styles.section}>
              <h2>International Data Transfers</h2>
              <p>
                As we track global products, your information may be processed in countries other than India. We ensure
                appropriate safeguards are in place for such transfers.
              </p>
            </section>

            <section className={styles.section}>
              <h2>Changes to This Privacy Policy</h2>
              <p>
                We may update this Privacy Policy from time to time. We will notify you of any material changes by
                posting the new Privacy Policy on this page and updating the "Last updated" date.
              </p>
            </section>

            <section className={styles.section}>
              <h2>Contact Us</h2>
              <p>If you have any questions about this Privacy Policy or our data practices, please contact us at:</p>
              <div className={styles.contactInfo}>
                <p>
                  <strong>Email:</strong> support@buzzlyn.com
                </p>
                <p>
                  <strong>Company:</strong> Buzzlyn
                </p>
                <p>
                  <strong>Jurisdiction:</strong> India
                </p>
              </div>
            </section>
          </div>
        </div>
      </main>
      <Footer />
    </div>
  )
}
