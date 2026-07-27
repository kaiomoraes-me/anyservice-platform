# ==========================================
# ESTÁGIO 1: BUILD (Compilação do Node.js)
# ==========================================
FROM node:22-alpine AS builder

WORKDIR /app

# Copia os arquivos de dependência primeiro
COPY package.json package-lock.json ./

# Instala as dependências
RUN npm ci

# Copia o restante do código
COPY . .

# Faz o build de produção do Angular
RUN npm run build

# ==========================================
# ESTÁGIO 2: SERVIDOR WEB (Nginx)
# ==========================================
# O Angular é apenas HTML/JS/CSS no final, precisa de um servidor estático
FROM nginx:alpine

# Remove as configurações padrão do Nginx
RUN rm -rf /usr/share/nginx/html/*

# Copia os arquivos "compilados" do Angular (da pasta dist/frontend/browser)
COPY --from=builder /app/dist/frontend/browser /usr/share/nginx/html

# Copia a nossa configuração customizada do Nginx (para o roteamento do Angular funcionar)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# O Nginx roda na porta 80 por padrão
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
