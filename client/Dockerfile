# --- Étape 1 : build de l'app Angular (avec SSR) ---
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# --- Étape 2 : exécution du serveur SSR Node ---
FROM node:22-alpine
WORKDIR /app
# on copie le build (browser + server) produit par Angular
COPY --from=build /app/dist ./dist
# le serveur SSR écoute sur PORT (défaut 4000) ; on fixe 4200
ENV PORT=4200
EXPOSE 4200
CMD ["node", "dist/client/server/server.mjs"]
