import { useState,useEffect } from "react"
import { useLocation } from "react-router-dom"
import { AnimatePresence } from "framer-motion"
import Navbar from "../components/Navbar.jsx"
import Footer from "../components/Footer.jsx"
import styles from "./AuthPage.module.css"

import SignInForm from "../components/SignInForm.jsx"
import SignUpForm from "../components/SignUpForm.jsx"

function AuthPage({setIsAuthenticated}) {
  const location = useLocation();

  const [authMode, setAuthMode] = useState("signin");
  const [oauthEmail, setOauthEmail] = useState("");
  const [isOAuthUser, setIsOAuthUser] = useState(false);

  useEffect(() => {
    if (location.state) {
      console.log("location state", location.state);
      setAuthMode(location.state.authMode || "signin");
      setOauthEmail(location.state.email || "");
      setIsOAuthUser(location.state.isOAuthUser || false);
      
      // Important: Clear the location state after reading it
      window.history.replaceState({}, document.title);
    }
  }, []); 

  return (
    <div className={styles.container}>
      <Navbar />
      <main className={styles.main}>
        <div className={styles.content}>
          <div className={styles.authContainer}>
            <div className={styles.authHeader}>
              <div className={styles.tabs}>
                <button
                  className={`${styles.tab} ${authMode === "signin" ? styles.activeTab : ""}`}
                  onClick={() => setAuthMode("signin")}
                >
                  Sign In
                </button>
                <button
                  className={`${styles.tab} ${authMode === "signup" ? styles.activeTab : ""}`}
                  onClick={() => setAuthMode("signup")}
                >
                  Sign Up
                </button>
              </div>
            </div>

            <div className={styles.authContent}>
              <AnimatePresence mode="wait">
                {authMode === "signin" ? (
                  <SignInForm key="signin"
                  setIsAuthenticated={setIsAuthenticated} />
                ) : (
                  <SignUpForm key="signup" onToggleForm={() => setAuthMode("signin")} 
                  oauthEmail={oauthEmail}
                  isOAuthUser={isOAuthUser}
                  setIsAuthenticated={setIsAuthenticated}/>
                )}
              </AnimatePresence>
            </div>
          </div>
        </div>
      </main>
      <Footer />
    </div>
  )
}

export default AuthPage
