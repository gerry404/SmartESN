# --- Étape 1 : build de l'app Vue avec Vite ---
FROM node:22-alpine AS build
WORKDIR /app

# l'URL du backend est injectée AU BUILD (Vite l'intègre dans le bundle)
ARG VITE_API_URL=http://localhost:8080
ENV VITE_API_URL=$VITE_API_URL

COPY package*.json ./
RUN npm ci
COPY . .
# build-only : compile sans le type-check bloquant
RUN npm run build-only

# --- Étape 2 : service statique via Nginx ---
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
