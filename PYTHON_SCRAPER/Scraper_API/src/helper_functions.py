def extract_website_name(url):
    domain = url.split("//")[-1].split("/")[0]
    if domain.split(".")[0] == "www":
        domain = ".".join(domain.split(".")[1:])
    return domain


if __name__ == "__main__":
    url = "https://www.amazon.in/802-11n-150Mbps-Wireless-Adapter-Network/dp/B07FVRKCZJ"
    print(extract_website_name(url))
