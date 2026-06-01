#!/bin/sh
set -e

case "${APP_MODE}" in
  cluster)
    echo "[Entrypoint] Starting in CLUSTER mode..."
    exec node dist/src/cluster-main.js
    ;;
  pm2)
    echo "[Entrypoint] Starting in PM2 mode..."
    exec pm2-runtime start ecosystem.config.cjs --env production
    ;;
  *)
    echo "[Entrypoint] Starting in SINGLE process mode..."
    exec node dist/src/main.js
    ;;
esac
