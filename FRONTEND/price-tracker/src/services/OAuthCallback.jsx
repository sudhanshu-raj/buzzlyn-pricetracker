import { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import {getProfilePic} from "../apicalls/authAPIs";
import Loading from "../components/Loading";

const OAuthCallback = () => {
  const { setUser, setIsAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);

  useEffect(() => {
    const handleAuth = async () => {
      // Extract auth data from URL parameters
      const email = queryParams.get("email");
      const newUser = queryParams.get("newUser") === "true";
      const isVerified = queryParams.get("isVerified") === "true";
      const token = queryParams.get("token");
      const firstName = queryParams.get("firstName");
      const profilePic = queryParams.get("profilePic");
      let number= queryParams.get("number");
      if (number) {
        number = number.replace(/ /g, '+');
      }
  
      const userData = {
        email: email ? email : "",
        firstName: firstName ? firstName : "",
        number: number ? number : "",

      };

      // Redirect based on user status
      if (newUser || !isVerified) {
        navigate("/auth", {
          state: {
            authMode: "signup",
            email: email,
            isOAuthUser: true,
          },
        });
      } else {
        // if user is verified then get the profile pic of it
        try {
          const response = await getProfilePic(email);
          if (response.data && response.data.imageFound) {
            const profilePicBase64 = response.data.profilePic;
            userData.avatarUrl = `data:image/png;base64,${profilePicBase64}`;
          } else {
            userData.avatarUrl = "/placeholder.svg?height=40&width=40";
          }
          setUser(userData);
          setIsAuthenticated(true);
          navigate("/track");
        } catch (error) {
          console.error("Error fetching profile picture:", error);
          // Still authenticate even if profile pic fetch fails
          setUser(userData);
          setIsAuthenticated(true);
          navigate("/track");
        }
      }
    };

    handleAuth();
  }, [navigate, queryParams, setUser, setIsAuthenticated]);

  return <Loading />; // Show a loading spinner while processing the callback
};

export default OAuthCallback;
