# from ultralytics import YOLO
# import cv2


# currently not in use
# def image_extracter(image_path=None):
#     # Load pre-trained YOLO model
#     model = YOLO("yolov8x.pt")  # Use yolov8n.pt for a smaller model

#     # Load the image
#     image_path = "screenshot_crop.png"
#     img = cv2.imread(image_path)

#     # Run YOLO object detection
#     results = model(image_path)

#     # Extract largest detected object
#     largest_box = None
#     largest_area = 0

#     for result in results:
#         for box in result.boxes.xyxy:  # Bounding box coordinates
#             x1, y1, x2, y2 = map(int, box[:4])
#             area = (x2 - x1) * (y2 - y1)

#             if area > largest_area:
#                 largest_area = area
#                 largest_box = (x1, y1, x2, y2)

#     if largest_box:
#         x1, y1, x2, y2 = largest_box
#         product_img = img[y1:y2, x1:x2]
#         cv2.imwrite("extracted_product.png", product_img)
#         print("✅ Product image saved as 'extracted_product.png'")
#     else:
#         print("❌ No product detected!")


from PIL import Image

def crop_image(input_path, output_path, width=1276, height=1200):
    # Open the image
    image = Image.open(input_path)

    # Define cropping box (starting from top-left corner)
    left = 0
    top = 0
    right = width
    bottom = height

    # Crop the image
    cropped_image = image.crop((left, top, right, bottom))

    # Save the cropped image
    cropped_image.save(output_path)
    print(f"Cropped image saved as {output_path}")



