import cv2
import numpy as np

# Load the screenshot
image_path = "laptop_stand.png"  # Ensure this path is correct
image = cv2.imread(image_path)

# Convert to grayscale
gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

# Apply adaptive thresholding (better for UI elements)
thresh = cv2.adaptiveThreshold(gray, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C,
                               cv2.THRESH_BINARY_INV, 11, 2)

# Apply Canny edge detection to find edges
edges = cv2.Canny(gray, 50, 150)

# Combine threshold and edge detection
combined = cv2.bitwise_or(thresh, edges)

# Find contours
contours, _ = cv2.findContours(combined, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

# Sort contours by area (largest first)
contours = sorted(contours, key=cv2.contourArea, reverse=True)

for cnt in contours:
    x, y, w, h = cv2.boundingRect(cnt)

    # Filter based on reasonable image size
    if w > 150 and h > 150:  # Adjust based on your screenshot resolution
        product_img = image[y:y+h, x:x+w]
        cv2.imwrite("extracted_product.png", product_img)  # Save extracted product image
        print("✅ Product image extracted and saved as 'extracted_product.png'")
        break
