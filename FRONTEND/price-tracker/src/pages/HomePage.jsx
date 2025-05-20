import Navbar from "../components/Navbar.jsx"
import Footer from "../components/Footer.jsx"
import HeroSection from "../components/HeroSection.jsx"
import ProductTracker from "../components/ProductTracker.jsx"
import Features from "../components/Features.jsx"
import styles from "./HomePage.module.css"

function HomePage() {
  return (
    <div className={styles.container}>
      <Navbar />
      <main className={styles.main}>
        <HeroSection />
        <ProductTracker />
        <Features />
      </main>
      <Footer />
    </div>
  )
}

export default HomePage

