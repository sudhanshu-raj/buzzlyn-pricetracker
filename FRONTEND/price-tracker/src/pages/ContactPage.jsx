"use client"

import { useState } from "react"
import Navbar from "../components/Navbar.jsx"
import Footer from "../components/Footer.jsx"
import styles from "./ContactPage.module.css"

function ContactPage() {
  const [formData, setFormData] = useState({
    name: "",
    email: "",
    subject: "",
    message: "",
  })
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [toastVisible, setToastVisible] = useState(false)
  const [toastMessage, setToastMessage] = useState({ title: "", description: "" })

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }))
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    setIsSubmitting(true)

    // Simulate form submission
    setTimeout(() => {
      setIsSubmitting(false)
      setToastMessage({
        title: "Message sent!",
        description: "We'll get back to you as soon as possible.",
      })
      setToastVisible(true)

      // Hide toast after 3 seconds
      setTimeout(() => {
        setToastVisible(false)
      }, 3000)

      setFormData({
        name: "",
        email: "",
        subject: "",
        message: "",
      })
    }, 1500)
  }

  const contactInfo = [
    {
      icon: "mail",
      title: "Email",
      details: "support@pricewatch.com",
      description: "Our friendly team is here to help.",
    },
    {
      icon: "map",
      title: "Office",
      details: "123 Market Street, San Francisco, CA",
      description: "Come say hello at our office.",
    },
    {
      icon: "phone",
      title: "Phone",
      details: "+1 (555) 123-4567",
      description: "Mon-Fri from 8am to 5pm.",
    },
  ]

  return (
    <div className={styles.container}>
      <Navbar />
      <main className={styles.main}>
        {/* Hero Section */}
        <section className={styles.heroSection}>
          <div className={styles.content}>
            <div className={styles.heroContent}>
              <h1 className={styles.heroTitle}>Get in Touch</h1>
              <p className={styles.heroSubtitle}>Have questions or feedback? We'd love to hear from you.</p>
            </div>
          </div>
        </section>

        {/* Contact Info & Form */}
        <section className={styles.contactSection}>
          <div className={styles.content}>
            <div className={styles.contactGrid}>
              {/* Contact Information */}
              <div className={styles.contactInfo}>
                <h2 className={styles.sectionTitle}>Contact Information</h2>
                <div className={styles.infoGrid}>
                  {contactInfo.map((item, index) => (
                    <div key={index} className={styles.infoItem}>
                      <div className={styles.infoIcon}>
                        {item.icon === "mail" && (
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
                            <rect width="20" height="16" x="2" y="4" rx="2"></rect>
                            <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"></path>
                          </svg>
                        )}
                        {item.icon === "map" && (
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
                            <path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"></path>
                            <circle cx="12" cy="10" r="3"></circle>
                          </svg>
                        )}
                        {item.icon === "phone" && (
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
                            <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path>
                          </svg>
                        )}
                      </div>
                      <div>
                        <h3 className={styles.infoTitle}>{item.title}</h3>
                        <p className={styles.infoDescription}>{item.description}</p>
                        <p className={styles.infoDetails}>{item.details}</p>
                      </div>
                    </div>
                  ))}
                </div>

                <div className={styles.socialSection}>
                  <h3 className={styles.socialTitle}>Follow Us</h3>
                  <div className={styles.socialIcons}>
                    {["Twitter", "LinkedIn", "Facebook", "Instagram"].map((platform, index) => (
                      <a key={index} href="#" className={styles.socialIcon} aria-label={platform}>
                        {platform === "Twitter" && (
                          <svg
                            xmlns="http://www.w3.org/2000/svg"
                            width="20"
                            height="20"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="currentColor"
                            strokeWidth="2"
                            strokeLinecap="round"
                            strokeLinejoin="round"
                          >
                            <path d="M22 4s-.7 2.1-2 3.4c1.6 10-9.4 17.3-18 11.6 2.2.1 4.4-.6 6-2C3 15.5.5 9.6 3 5c2.2 2.6 5.6 4.1 9 4-.9-4.2 4-6.6 7-3.8 1.1 0 3-1.2 3-1.2z"></path>
                          </svg>
                        )}
                        {platform === "LinkedIn" && (
                          <svg
                            xmlns="http://www.w3.org/2000/svg"
                            width="20"
                            height="20"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="currentColor"
                            strokeWidth="2"
                            strokeLinecap="round"
                            strokeLinejoin="round"
                          >
                            <path d="M16 8a6 6 0 0 1 6 6v7h-4v-7a2 2 0 0 0-2-2 2 2 0 0 0-2 2v7h-4v-7a6 6 0 0 1 6-6z"></path>
                            <rect width="4" height="12" x="2" y="9"></rect>
                            <circle cx="4" cy="4" r="2"></circle>
                          </svg>
                        )}
                        {platform === "Facebook" && (
                          <svg
                            xmlns="http://www.w3.org/2000/svg"
                            width="20"
                            height="20"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="currentColor"
                            strokeWidth="2"
                            strokeLinecap="round"
                            strokeLinejoin="round"
                          >
                            <path d="M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3z"></path>
                          </svg>
                        )}
                        {platform === "Instagram" && (
                          <svg
                            xmlns="http://www.w3.org/2000/svg"
                            width="20"
                            height="20"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="currentColor"
                            strokeWidth="2"
                            strokeLinecap="round"
                            strokeLinejoin="round"
                          >
                            <rect width="20" height="20" x="2" y="2" rx="5" ry="5"></rect>
                            <path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z"></path>
                            <line x1="17.5" x2="17.51" y1="6.5" y2="6.5"></line>
                          </svg>
                        )}
                      </a>
                    ))}
                  </div>
                </div>
              </div>

              {/* Contact Form */}
              <div className={styles.contactForm}>
                <div className={styles.formCard}>
                  <div className={styles.formHeader}>
                    <h2 className={styles.formTitle}>Send us a message</h2>
                    <p className={styles.formDescription}>
                      Fill out the form below and we'll get back to you as soon as possible.
                    </p>
                  </div>
                  <div className={styles.formContent}>
                    <form onSubmit={handleSubmit} className={styles.form}>
                      <div className={styles.formRow}>
                        <div className={styles.formGroup}>
                          <label htmlFor="name" className={styles.formLabel}>
                            Your Name
                          </label>
                          <input
                            id="name"
                            name="name"
                            className={styles.formInput}
                            placeholder="John Doe"
                            required
                            value={formData.name}
                            onChange={handleChange}
                            disabled={isSubmitting}
                          />
                        </div>
                        <div className={styles.formGroup}>
                          <label htmlFor="email" className={styles.formLabel}>
                            Your Email
                          </label>
                          <input
                            id="email"
                            name="email"
                            type="email"
                            className={styles.formInput}
                            placeholder="john@example.com"
                            required
                            value={formData.email}
                            onChange={handleChange}
                            disabled={isSubmitting}
                          />
                        </div>
                      </div>
                      <div className={styles.formGroup}>
                        <label htmlFor="subject" className={styles.formLabel}>
                          Subject
                        </label>
                        <input
                          id="subject"
                          name="subject"
                          className={styles.formInput}
                          placeholder="How can we help you?"
                          required
                          value={formData.subject}
                          onChange={handleChange}
                          disabled={isSubmitting}
                        />
                      </div>
                      <div className={styles.formGroup}>
                        <label htmlFor="message" className={styles.formLabel}>
                          Message
                        </label>
                        <textarea
                          id="message"
                          name="message"
                          className={styles.formTextarea}
                          placeholder="Your message here..."
                          rows={5}
                          required
                          value={formData.message}
                          onChange={handleChange}
                          disabled={isSubmitting}
                        />
                      </div>
                      <button type="submit" className={styles.submitButton} disabled={isSubmitting}>
                        {isSubmitting ? "Sending..." : "Send Message"}
                      </button>
                    </form>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* FAQ Section */}
        <section className={styles.faqSection}>
          <div className={styles.content}>
            <div className={styles.faqHeader}>
              <h2 className={styles.sectionTitle}>Frequently Asked Questions</h2>
              <p className={styles.sectionSubtitle}>Find answers to common questions about PriceWatch</p>
            </div>
            <div className={styles.faqGrid}>
              {[
                {
                  question: "How does PriceWatch work?",
                  answer:
                    "PriceWatch monitors product prices across major retailers. Simply paste a product URL, set your target price, and we'll notify you when the price drops.",
                },
                {
                  question: "Is PriceWatch free to use?",
                  answer:
                    "Yes, PriceWatch is free for basic tracking. We also offer premium plans with additional features like unlimited product tracking and priority notifications.",
                },
                {
                  question: "Which stores do you support?",
                  answer:
                    "We support all major online retailers including Amazon, Walmart, Best Buy, Target, eBay, and many more. We're constantly adding new stores to our platform.",
                },
                {
                  question: "How do I get notified of price drops?",
                  answer:
                    "You can choose to receive notifications via email, SMS, or WhatsApp. You can customize your notification preferences for each tracked product.",
                },
              ].map((faq, index) => (
                <div key={index} className={styles.faqCard}>
                  <h3 className={styles.faqQuestion}>{faq.question}</h3>
                  <p className={styles.faqAnswer}>{faq.answer}</p>
                </div>
              ))}
            </div>
            <div className={styles.supportSection}>
              <p className={styles.supportText}>Still have questions? Contact our support team.</p>
              <button className={styles.supportButton}>
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
                  className={styles.supportIcon}
                >
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
                </svg>
                Chat with Support
              </button>
            </div>
          </div>
        </section>

        {/* Toast notification */}
        {toastVisible && (
          <div className={styles.toast}>
            <div className={styles.toastContent}>
              <div className={styles.toastTitle}>{toastMessage.title}</div>
              <div className={styles.toastDescription}>{toastMessage.description}</div>
            </div>
          </div>
        )}
      </main>
      <Footer />
    </div>
  )
}

export default ContactPage

