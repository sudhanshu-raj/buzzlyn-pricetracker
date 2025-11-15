#!/bin/sh
# Create a self-signed fallback certificate if Let's Encrypt files are missing.
set -e

DOMAIN="localhost"
LIVE_DIR="/etc/letsencrypt/live/$DOMAIN"
KEY="$LIVE_DIR/privkey.pem"
CERT="$LIVE_DIR/fullchain.pem"

if [ ! -f "$CERT" ] || [ ! -f "$KEY" ]; then
  echo "[nginx-entrypoint] Real certs not found for $DOMAIN — creating self-signed fallback certs"
  mkdir -p "$LIVE_DIR"
  openssl req -x509 -nodes -newkey rsa:2048 -days 365 \
    -keyout "$KEY" -out "$CERT" \
    -subj "/CN=$DOMAIN"
  chmod 644 "$CERT" || true
  chmod 600 "$KEY" || true
else
  echo "[nginx-entrypoint] Found existing certs for $DOMAIN"
fi

exec /docker-entrypoint.sh "$@"
