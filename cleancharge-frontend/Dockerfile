FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:1.27-alpine
RUN apk add --no-cache curl
COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=build /app/dist/cleancharge-frontend/browser /usr/share/nginx/html
RUN mkdir -p /var/cache/nginx /var/run-nginx \
    && chown -R 101:101 /var/cache/nginx /var/run-nginx /usr/share/nginx/html
USER 101:101
EXPOSE 3000
ENTRYPOINT ["nginx", "-g", "daemon off;"]
