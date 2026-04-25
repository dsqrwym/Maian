"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
require("dotenv/config");
const drizzle_kit_1 = require("drizzle-kit");
exports.default = (0, drizzle_kit_1.defineConfig)({
    dialect: 'postgresql',
    schema: './src/generated/drizzle',
    out: './drizzle',
    dbCredentials: {
        url: process.env.DIRECT_URL,
    },
    introspect: {
        casing: 'preserve',
    },
});
//# sourceMappingURL=drizzle.config.js.map