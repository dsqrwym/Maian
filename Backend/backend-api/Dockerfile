# --- 环境配置 ---
# 使用 Node.js 官方镜像 版本24
FROM node:24-slim AS base
# 设置环境变量
ENV PNPM_HOME="/pnpm"
# 将 pnpm 目录加入到系统的 PATH 中
ENV PATH="$PNPM_HOME:$PATH"
# 启用 Corepack。是 Node.js 内置的工具，用来管理 pnpm/yarn，不需要手动安装
RUN corepack enable

# --- 构建 ---
FROM base AS builder
# 设置容器内的操作目录
WORKDIR /app

# 复制配置、依赖文件
COPY package.json pnpm-lock.yaml ./

# 安装所有依赖（包括 开发依赖）
# --frozen-lockfile 确保不会自动更新 lock 文件
RUN pnpm install --frozen-lockfile

# 复制源代码
COPY . .
# 执行 NestJS 构建
RUN pnpm run build
 # 清理 devDependencies
RUN pnpm prune --prod

# --- 运行 ---
FROM base AS runner
WORKDIR /app

# 只从构建阶段复制必要文件
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/package.json ./
COPY --from=builder /app/pnpm-lock.yaml ./
# 从 builder 阶段把编译好的代码 (dist) 复制过来
COPY --from=builder /app/dist ./dist
# i18n
COPY --from=builder /app/src/i18n/generated ./src/i18n/generated
COPY --from=builder /app/src/i18n/locales ./src/i18n/locales

# 暴露端口（NestJS 默认 3000）
EXPOSE 3000

# 启动命令
CMD ["node", "dist/src/main"]