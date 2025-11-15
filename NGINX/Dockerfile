FROM nginx:alpine

# ensure openssl is available to create fallback certs at container start
RUN apk add --no-cache openssl

COPY production.conf /etc/nginx/conf.d/default.conf
COPY create_dummy_certs.sh /usr/local/bin/create_dummy_certs.sh
RUN chmod +x /usr/local/bin/create_dummy_certs.sh

# use the wrapper script so container can create dummy certs when real certs missing
ENTRYPOINT ["/usr/local/bin/create_dummy_certs.sh"]
CMD ["nginx", "-g", "daemon off;"]
