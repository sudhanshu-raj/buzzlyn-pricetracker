from PIL import Image

# Define the bounding box coordinates
box = (52, 91, 171, 362)  # (left, top, right, bottom)

# Input and output file paths
input_image_path = "screenshot.png"  # Replace with the actual path to your image file
output_image_path = "cropped_image.png"

try:
    # Open the image
    img = Image.open(input_image_path)

    # Crop the image
    cropped_img = img.crop(box)

    # Save the cropped image
    cropped_img.save(output_image_path)

    print(f"Image cropped and saved to {output_image_path}")

except FileNotFoundError:
    print(f"Error: Input image not found at {input_image_path}")
except Exception as e:
    print(f"An error occurred: {e}")