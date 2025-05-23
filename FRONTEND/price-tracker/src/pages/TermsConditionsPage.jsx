import { Navbar } from "../components/Navbar"
import  Footer  from "../components/Footer"
import styles from "./TermsConditionsPage.module.css"

export default function TermsConditionsPage() {
  return (
    <div className={styles.pageContainer}>
      <Navbar />
      <main className={styles.main}>
        <div className={styles.container}>
          <div className={styles.header}>
            <h1 className={styles.title}>Terms and Conditions</h1>
            <p className={styles.lastUpdated}>Last updated: January 23, 2025</p>
          </div>

          <div className={styles.content}>
            <section className={styles.section}>
              <h2>1. Acceptance of Terms</h2>
              <p>
                By accessing and using Buzzlyn's price tracking service ("Service"), you accept and agree to be bound by
                the terms and provision of this agreement. If you do not agree to abide by the above, please do not use
                this service.
              </p>
            </section>

            <section className={styles.section}>
              <h2>2. Description of Service</h2>
              <p>Buzzlyn provides a price tracking service that allows users to:</p>
              <ul>
                <li>Track prices of physical products from various e-commerce websites</li>
                <li>Set target prices and receive alerts when prices drop</li>
                <li>Receive notifications via email, WhatsApp, SMS, or web push notifications</li>
                <li>Monitor global products from supported e-commerce platforms</li>
              </ul>
              <p>
                Our service is currently limited to physical products available on e-commerce platforms and does not
                include digital products or services.
              </p>
            </section>

            <section className={styles.section}>
              <h2>3. User Accounts and Registration</h2>
              <h3>Account Creation</h3>
              <ul>
                <li>You must provide accurate and complete information during registration</li>
                <li>You are responsible for maintaining the confidentiality of your account credentials</li>
                <li>You must be at least 13 years old to use our service</li>
                <li>One person may not maintain multiple accounts</li>
              </ul>

              <h3>Account Responsibilities</h3>
              <ul>
                <li>You are responsible for all activities that occur under your account</li>
                <li>You must notify us immediately of any unauthorized use of your account</li>
                <li>We reserve the right to suspend or terminate accounts that violate these terms</li>
              </ul>
            </section>

            <section className={styles.section}>
              <h2>4. Acceptable Use Policy</h2>
              <h3>Permitted Uses</h3>
              <ul>
                <li>Track prices for personal shopping and purchasing decisions</li>
                <li>Set reasonable price alerts for products you intend to purchase</li>
                <li>Use the service in compliance with all applicable laws</li>
              </ul>

              <h3>Prohibited Uses</h3>
              <ul>
                <li>Using the service for commercial price monitoring without authorization</li>
                <li>Attempting to overload or disrupt our systems</li>
                <li>Tracking products for illegal or harmful purposes</li>
                <li>Sharing your account credentials with others</li>
                <li>Using automated tools to create excessive tracking requests</li>
                <li>Attempting to reverse engineer or copy our service</li>
              </ul>
            </section>

            <section className={styles.section}>
              <h2>5. Communication and Consent</h2>
              <p>By using our service, you understand that:</p>
              <ul>
                <li>We will only send notifications with your explicit consent</li>
                <li>You can modify your communication preferences at any time</li>
                <li>You can withdraw consent and stop receiving notifications</li>
                <li>We may send service-related communications regardless of your marketing preferences</li>
              </ul>
            </section>

            <section className={styles.section}>
              <h2>6. Service Availability and Accuracy</h2>
              <h3>Service Availability</h3>
              <ul>
                <li>We strive to maintain 99% uptime but cannot guarantee uninterrupted service</li>
                <li>We may temporarily suspend the service for maintenance or updates</li>
                <li>We are not liable for any losses due to service interruptions</li>
              </ul>

              <h3>Price Accuracy</h3>
              <ul>
                <li>We make reasonable efforts to provide accurate price information</li>
                <li>Prices are sourced from third-party e-commerce websites</li>
                <li>We are not responsible for pricing errors on external websites</li>
                <li>Always verify prices on the retailer's website before making purchases</li>
              </ul>
            </section>

            <section className={styles.section}>
              <h2>7. Intellectual Property Rights</h2>
              <p>
                The Buzzlyn service, including its design, functionality, and content, is protected by copyright,
                trademark, and other intellectual property laws. You may not:
              </p>
              <ul>
                <li>Copy, modify, or distribute our service or content</li>
                <li>Use our trademarks or branding without permission</li>
                <li>Create derivative works based on our service</li>
                <li>Remove or alter any copyright or proprietary notices</li>
              </ul>
            </section>

            <section className={styles.section}>
              <h2>8. Privacy and Data Protection</h2>
              <p>
                Your privacy is important to us. Our collection and use of personal information is governed by our
                Privacy Policy, which is incorporated into these Terms by reference. By using our service, you consent
                to the collection and use of your information as described in our Privacy Policy.
              </p>
            </section>

            <section className={styles.section}>
              <h2>9. Limitation of Liability</h2>
              <p>To the maximum extent permitted by law:</p>
              <ul>
                <li>Buzzlyn shall not be liable for any indirect, incidental, or consequential damages</li>
                <li>Our total liability shall not exceed the amount paid by you for the service</li>
                <li>We are not responsible for decisions made based on price information provided</li>
                <li>We do not guarantee that tracked prices will result in successful purchases</li>
              </ul>
            </section>

            <section className={styles.section}>
              <h2>10. Disclaimer of Warranties</h2>
              <p>
                The service is provided "as is" and "as available" without warranties of any kind. We disclaim all
                warranties, express or implied, including but not limited to:
              </p>
              <ul>
                <li>Merchantability and fitness for a particular purpose</li>
                <li>Accuracy, reliability, or completeness of price information</li>
                <li>Uninterrupted or error-free operation</li>
                <li>Security of data transmission</li>
              </ul>
            </section>

            <section className={styles.section}>
              <h2>11. Termination</h2>
              <h3>Termination by You</h3>
              <ul>
                <li>You may terminate your account at any time through your account settings</li>
                <li>Upon termination, your tracking data will be deleted according to our data retention policy</li>
              </ul>

              <h3>Termination by Us</h3>
              <ul>
                <li>We may suspend or terminate your account for violation of these terms</li>
                <li>We may discontinue the service with reasonable notice</li>
                <li>We reserve the right to refuse service to anyone</li>
              </ul>
            </section>

            <section className={styles.section}>
              <h2>12. Governing Law and Jurisdiction</h2>
              <p>
                These Terms and Conditions are governed by the laws of India. Any disputes arising from these terms or
                your use of the service shall be subject to the exclusive jurisdiction of the courts in India.
              </p>
            </section>

            <section className={styles.section}>
              <h2>13. Changes to Terms</h2>
              <p>
                We reserve the right to modify these Terms and Conditions at any time. We will notify users of material
                changes via email or through our service. Your continued use of the service after such modifications
                constitutes acceptance of the updated terms.
              </p>
            </section>

            <section className={styles.section}>
              <h2>14. Contact Information</h2>
              <p>If you have any questions about these Terms and Conditions, please contact us:</p>
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

            <section className={styles.section}>
              <h2>15. Severability</h2>
              <p>
                If any provision of these Terms and Conditions is found to be unenforceable or invalid, that provision
                will be limited or eliminated to the minimum extent necessary so that the remaining terms will remain in
                full force and effect.
              </p>
            </section>
          </div>
        </div>
      </main>
      <Footer />
    </div>
  )
}
