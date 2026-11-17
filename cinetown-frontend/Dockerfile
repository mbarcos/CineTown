FROM node:24-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM node:24-alpine
ENV NODE_ENV=production
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build --chown=app:app /app/dist/cinetown-frontend ./dist/cinetown-frontend
USER app
EXPOSE 4000
CMD ["node", "dist/cinetown-frontend/server/server.mjs"]
