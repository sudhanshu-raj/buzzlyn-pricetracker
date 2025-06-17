import json
import os
import time
import google.generativeai as genai
from dotenv import load_dotenv
load_dotenv()


genai.configure(api_key=os.environ["GEMINI_API_KEY"])


def extract_from_html(html_content):
    '''
    1 token ≈ 4 characters for common text
    1 token ≈ 6-8 characters for dense content like HTML
    '''
    generation_config = {
        "temperature": 1,
        "top_p": 0.95,
        "top_k": 40,
        "max_output_tokens": 8192,
        "response_mime_type": "text/plain",
    }
    prompt = '''
            From the given HTML content:
            Determine if the page is a product page by checking for essential product elements like title, price, and images.
            Ensure the product is a physical item by filtering out categories like hotel bookings, flight and train tickets, car rentals, 
            food delivery apps,digital goods (e-books, software, subscriptions), and services.
            
            Extract the following details only if the product is a physical item:
            Stock Status: Check if the product is in stock or out of stock globally. Ignore pincode-specific availability messages such as "Unavailable in your area" or "Not deliverable to this pincode" — these do not indicate that the product is out of stock.
            Title: Extract the product’s exact name.
            Price: Capture the current selling price without the currency symbol if there and in long format only.
            MRP: Extract the maximum retail price (if available)  without the currency symbol if there and in long format only.
            Currency: Currency code like INR,USD,EUR etc of the price extracted if available
            Reviews: Extract the total number of reviews (integer format).
            Ratings: Extract the total number of ratings (integer format).
            Image URL: Extract the main product image URL.
           
            Output JSON Format:
            If the page is a valid physical product page, return:
            {
                "valid_product_page": true,
                "physical_product": true,
                "title": "product_title",
                "price": "product_price in long format",
                "mrp": "product_mrp in long format",
                "currency":"currency code of price extracted"
                "stock": "in_stock or out_stock format",
                "reviews": "product reviews only in int format",
                "ratings": "product ratings only in int format",
                "imageURL": "product_image_url"
            }
            If the page is not a product page or is not a physical product, return:
            {
                "valid_product_page": false,
                "physical_product": false
            }

        '''

    model = genai.GenerativeModel(
        model_name="gemini-2.0-flash",
        generation_config=generation_config,
        system_instruction=prompt
    )

    chat_session = model.start_chat(
        history=[
        ]
    )

    # Count tokens for input HTML
    input_tokens = model.count_tokens(html_content).total_tokens

    try:
        response = chat_session.send_message(html_content)
        output_tokens = model.count_tokens(response.text).total_tokens
        total_tokens = input_tokens + output_tokens
        print("Scraped using Gemini scraper")
        print(
            f"Token usage - Input: {input_tokens}, Output: {output_tokens}, Total: {total_tokens}")

        # Clean the response
        json_str = response.text.replace(
            '```json', '').replace('```', '').strip()

        # Parse with detailed error info
        result = json.loads(json_str)
        return {  # Ensure consistent structure
            "title": result.get("title", "Untitled"),
            "price": result.get("price", "No Price"),
            "mrp": result.get("mrp", "No mrp"),
            "currency": result.get("currency", "No currency"),
            "stock": result.get("stock", "Stock not found"),
            "reviews": result.get("reviews", "No reviews found"),
            "ratings": result.get("ratings", "No ratings found"),
            "imageURL": result.get("imageURL", "No image url found"),
            "valid_product_page": result.get("valid_product_page", "valid_product_page value not found"),
            "physical_product": result.get("physical_product", "physical_product value not found")
        }

    except json.JSONDecodeError as e:
        print(f"JSON Error: {e}\nResponse was: {response.text}")
        return {
            "title": "Untitled",
            "description": "No description available",
            "error": f"Invalid JSON: {str(e)}",
            "raw_response": response.text  # For debugging
        }
    except AttributeError as e:
        print(f"Empty response: {e}")
        return {
            "title": "Untitled",
            "description": "No response from API",
            "error": str(e)
        }


def upload_to_gemini(path, mime_type=None):
    """Uploads the given file to Gemini.

    See https://ai.google.dev/gemini-api/docs/prompting_with_media
    """
    file = genai.upload_file(path, mime_type=mime_type)
    print(f"Uploaded file '{file.display_name}' as: {file.uri}")
    return file


def extract_from_image(image_path):
    # Create the model
    generation_config = {
        "temperature": 1,
        "top_p": 0.95,
        "top_k": 40,
        "max_output_tokens": 8192,
        "response_mime_type": "text/plain",
    }

    model = genai.GenerativeModel(
        model_name="gemini-2.0-flash",
        generation_config=generation_config,
        system_instruction='''from given image, extract the main product details 
        like availability then title, price, mrp, reviews ,ratings, specs if any in json format like this :       
{
    "title": "product_title",
    "price": "prdouct_price without the currency symbol if there and in long format",
    "mrp": "product_mrp without the currency symbol if there and in long format",
    "stock": "in_stock or out_stock format",
    "reviews": "product reviews only in int format",
    "ratings": "product ratings only in int format",
}
''',
    )

    # TODO Make these files available on the local file system
    # You may need to update the file paths
    # image_path = r"C:\Users\rajsu\Downloads\screenshot.png"
    files = [
        upload_to_gemini(image_path, mime_type="image/png"),
    ]

    chat_session = model.start_chat(
        history=[
            {
                "role": "user",
                "parts": [
                    files[0],
                ],
            }
        ]
    )
    input_tokens = model.count_tokens(files).total_tokens
    try:

        response = chat_session.send_message(
            "folllow the system instructions given to you for image extraction")

        output_tokens = model.count_tokens(response.text).total_tokens
        total_tokens = input_tokens + output_tokens

        print(
            f"Token usage - Input: {input_tokens}, Output: {output_tokens}, Total: {total_tokens}")

        # Clean the response
        json_str = response.text.replace(
            '```json', '').replace('```', '').strip()

        # Parse with detailed error info
        result = json.loads(json_str)
        return {  # Ensure consistent structure
            "title": result.get("title", "Untitled"),
            "price": result.get("price", "No Price"),
            "mrp": result.get("mrp", "No mrp"),
            "stock": result.get("stock", "Stock not found"),
            "reviews": result.get("reviews", "No reviews found"),
            "ratings": result.get("ratings", "No ratings found")
        }
    except json.JSONDecodeError as e:
        print(f"JSON Error: {e}\nResponse was: {response.text}")
        return {
            "title": "Untitled",
            "description": "No description available",
            "error": f"Invalid JSON: {str(e)}",
            "raw_response": response.text  # For debugging
        }
    except AttributeError as e:
        print(f"Empty response: {e}")
        return {
            "title": "Untitled",
            "description": "No response from API",
            "error": str(e)
        }


if __name__ == "__main__":
    start_time = time.time()
    print(extract_from_image("cropped_img.jpg"))
    print(f"ended in {time.time()-start_time} ")
