import google.generativeai as genai
import PIL.Image
import io
import cv2
import numpy as np


def extract_product_image(image_path, api_key, prompt="Find the product in this image.  Provide its bounding box coordinates in the format 'x1, y1, x2, y2' where x1,y1 is the top-left and x2,y2 is the bottom-right corner.  Respond ONLY with those coordinates.  If you cannot find the product, respond with 'None'"):
    """
    Extracts the main product image from a screenshot using Gemini Pro Vision and OpenCV.

    Args:
        image_path: Path to the screenshot image.
        api_key: Your Google AI API key.
        prompt: Optional custom prompt for Gemini Pro Vision.

    Returns:
        PIL.Image.Image or None: The extracted product image, or None if extraction fails.
    """
    genai.configure(api_key=api_key)
    model = genai.GenerativeModel('gemini-2.0-flash')

    try:
        img = PIL.Image.open(image_path)
        response = model.generate_content([prompt, img])

        coordinates = response.text.strip()

        if coordinates.lower() == "none":
            print("Product not found by Gemini.")
            return None

        x1, y1, x2, y2 = map(int, coordinates.split(','))


        # Open the image with PIL for cropping
        pil_img = PIL.Image.open(image_path)
        cropped_img = pil_img.crop((x1, y1, x2, y2))

        return cropped_img


    except Exception as e:
        print(f"Error during extraction: {e}")
        return None
