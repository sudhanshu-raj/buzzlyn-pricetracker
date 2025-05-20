import Navbar from "../components/Navbar.jsx"
import Footer from "../components/Footer.jsx"
import { Link } from "react-router-dom"
import styles from "./AboutPage.module.css"

function AboutPage() {
  return (
    <div className={styles.container}>
      <Navbar />
      <main className={styles.main}>
        {/* Hero Section */}
        <section className={styles.heroSection}>
          <div className={styles.content}>
            <div className={styles.heroContent}>
              <h1 className={styles.heroTitle}>
                About <span className={styles.gradient}>PriceWatch</span>
              </h1>
              <p className={styles.heroSubtitle}>
                We're on a mission to help shoppers save money and make smarter purchasing decisions.
              </p>
              <div className={styles.heroImageWrapper}>
                <img src="/placeholder.svg?height=300&width=600" alt="PriceWatch Team" className={styles.heroImage} />
              </div>
            </div>
          </div>
        </section>

        {/* Our Story */}
        <section className={styles.storySection}>
          <div className={styles.content}>
            <div className={styles.storyGrid}>
              <div className={styles.storyContent}>
                <h2 className={styles.sectionTitle}>Our Story</h2>
                <div className={styles.storyText}>
                  <p>
                    PriceWatch was founded in 2023 by a group of tech enthusiasts who were tired of overpaying for
                    products online. We noticed that prices for the same items fluctuated wildly across different
                    retailers and even on the same site over time.
                  </p>
                  <p>
                    What started as a simple tool to track prices for our own shopping needs quickly grew into a
                    comprehensive platform that helps thousands of shoppers save money every day.
                  </p>
                  <p>
                    Our team combines expertise in e-commerce, data science, and consumer advocacy to build the most
                    accurate and user-friendly price tracking service available.
                  </p>
                </div>
                <div className={styles.ctaWrapper}>
                  <Link to="/track" className={styles.ctaButton}>
                    Start Saving Today
                    <svg
                      className={styles.arrowIcon}
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
                      <path d="M5 12h14"></path>
                      <path d="m12 5 7 7-7 7"></path>
                    </svg>
                  </Link>
                </div>
              </div>
              <div className={styles.statsWrapper}>
                <div className={styles.statsContent}>
                  {[
                    {
                      icon: "users",
                      title: "50,000+ Users",
                      description: "Helping shoppers save money across the globe",
                    },
                    {
                      icon: "chart",
                      title: "$2.5M+ Saved",
                      description: "Total savings for our users and counting",
                    },
                    {
                      icon: "bell",
                      title: "100,000+ Alerts",
                      description: "Price drop notifications sent to happy users",
                    },
                  ].map((stat, index) => (
                    <div key={index} className={styles.statItem}>
                      <div className={styles.statIcon}>
                        {stat.icon === "users" && (
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
                            <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
                            <circle cx="9" cy="7" r="4"></circle>
                            <path d="M22 21v-2a4 4 0 0 0-3-3.87"></path>
                            <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                          </svg>
                        )}
                        {stat.icon === "chart" && (
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
                            <path d="M18 20V10"></path>
                            <path d="M12 20V4"></path>
                            <path d="M6 20v-6"></path>
                          </svg>
                        )}
                        {stat.icon === "bell" && (
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
                            <path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"></path>
                            <path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"></path>
                          </svg>
                        )}
                      </div>
                      <div>
                        <h3 className={styles.statTitle}>{stat.title}</h3>
                        <p className={styles.statDescription}>{stat.description}</p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Our Values */}
        <section className={styles.valuesSection}>
          <div className={styles.content}>
            <div className={styles.valuesHeader}>
              <h2 className={styles.sectionTitle}>Our Values</h2>
              <p className={styles.sectionSubtitle}>The principles that guide everything we do at PriceWatch</p>
            </div>
            <div className={styles.valuesGrid}>
              {[
                {
                  icon: "heart",
                  title: "Customer First",
                  description: "We put our users at the center of everything we build and every decision we make.",
                },
                {
                  icon: "shield",
                  title: "Privacy & Security",
                  description: "We never sell your data and maintain the highest standards of security and privacy.",
                },
                {
                  icon: "users",
                  title: "Transparency",
                  description: "We're open about how our service works and how we make money.",
                },
              ].map((value, index) => (
                <div key={index} className={styles.valueCard}>
                  <div className={styles.valueIcon}>
                    {value.icon === "heart" && (
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
                        <path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"></path>
                      </svg>
                    )}
                    {value.icon === "shield" && (
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
                        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10"></path>
                        <path d="m9 12 2 2 4-4"></path>
                      </svg>
                    )}
                    {value.icon === "users" && (
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
                        <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
                        <circle cx="9" cy="7" r="4"></circle>
                        <path d="M22 21v-2a4 4 0 0 0-3-3.87"></path>
                        <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                      </svg>
                    )}
                  </div>
                  <h3 className={styles.valueTitle}>{value.title}</h3>
                  <p className={styles.valueDescription}>{value.description}</p>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* Team */}
        <section className={styles.teamSection}>
          <div className={styles.content}>
            <div className={styles.teamHeader}>
              <h2 className={styles.sectionTitle}>Meet Our Team</h2>
              <p className={styles.sectionSubtitle}>The passionate people behind PriceWatch</p>
            </div>
            <div className={styles.teamGrid}>
              {[
                {
                  name: "Alex Johnson",
                  role: "Founder & CEO",
                  image: "/placeholder.svg?height=300&width=300",
                },
                {
                  name: "Sarah Chen",
                  role: "CTO",
                  image: "/placeholder.svg?height=300&width=300",
                },
                {
                  name: "Michael Rodriguez",
                  role: "Head of Product",
                  image: "/placeholder.svg?height=300&width=300",
                },
                {
                  name: "Priya Patel",
                  role: "Lead Designer",
                  image: "/placeholder.svg?height=300&width=300",
                },
              ].map((member, index) => (
                <div key={index} className={styles.teamMember}>
                  <div className={styles.memberImage}>
                    <img src={member.image || "/placeholder.svg"} alt={member.name} />
                  </div>
                  <h3 className={styles.memberName}>{member.name}</h3>
                  <p className={styles.memberRole}>{member.role}</p>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* CTA */}
        <section className={styles.ctaSection}>
          <div className={styles.content}>
            <div className={styles.ctaContent}>
              <h2 className={styles.ctaTitle}>Ready to Start Saving?</h2>
              <p className={styles.ctaSubtitle}>
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

export default AboutPage

