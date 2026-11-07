# The web app's image (ADR 0021): the Vue app from web/app, served as static files by Caddy on
# :8080. Build from the repository root, since the design system reads design/ and the words i18n/:
#   docker build -f web/Dockerfile -t coach-poulpy-web .
# ANDROID_CERT_SHA256, the Android signing key's SHA-256 as keytool prints it, adds
# /.well-known/assetlinks.json so the account email links open the app. See docs/deploying-the-server.md.

FROM node:22-alpine AS build
WORKDIR /src
COPY design design
COPY i18n i18n
COPY web web
WORKDIR /src/web
RUN npm ci && npm run build
ARG ANDROID_CERT_SHA256=
RUN if [ -n "$ANDROID_CERT_SHA256" ]; then \
      mkdir -p app/dist/.well-known && \
      printf '[{"relation":["delegate_permission/common.handle_all_urls"],"target":{"namespace":"android_app","package_name":"ch.coachpoulpy","sha256_cert_fingerprints":["%s"]}}]\n' \
        "$ANDROID_CERT_SHA256" > app/dist/.well-known/assetlinks.json; \
    fi

FROM caddy:2-alpine
COPY web/Caddyfile /etc/caddy/Caddyfile
COPY --from=build /src/web/app/dist /srv
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD ["wget", "-q", "-O", "/dev/null", "http://127.0.0.1:8080/"]
