from bs4 import BeautifulSoup
from src.playwright_demo import extract_clean_html

url = "https://www.amazon.in/Lenovo-Windows-i7-14650Hx-83DG004RIN-Backpack/dp/B0DFCGCM3Q/ref=sr_1_16?nsdOptOutParam=true&sr=8-16"
html = extract_clean_html(url)

# Parse the HTML
soup = BeautifulSoup(html, "html.parser")
product_details = {}

priceSectionIV = soup.select_one("span.a-price.a-text-price.a-size-medium")
price = priceSectionIV.find("span", class_="a-offscreen")
price = int(float(price.get_text(strip=True).replace(",", "").split("₹")[1]))
print(price)
