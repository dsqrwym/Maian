import { Injectable } from '@nestjs/common';
import { LRUCache } from 'lru-cache';


@Injectable()
export class DateFormatService {
    // 使用 LRU 缓存替代原生 Map
    private formatterCache = new LRUCache<string, Intl.DateTimeFormat>({
        max: 1000, // 最多缓存 1000 个格式化实例
        ttl: 86400000, // 24 小时缓存
        allowStale: false // 严格过期策略
    });

    constructor() {
        this.preloadCommonFormats();
    }

    // 预加载常见格式组合
    private preloadCommonFormats() {
        const commonCombinations = [
            { lang: "en", timeZone: "UTC" },
            { lang: "zh-CN", timeZone: "Asia/Shanghai" },
            { lang: "es", timeZone: "Europe/Madrid" }
        ];

        commonCombinations.forEach(({ lang, timeZone }) => {
            const key = this.generateCacheKey(lang, timeZone);
            if (!this.formatterCache.has(key)) {
                this.formatterCache.set(
                    key,
                    this.createFormatter(lang, timeZone)
                );
            }
        });
    }

    // 统一格式化方法（始终包含时间）
    formatDate(
        date: Date,
        lang: string = "en",
        timeZone: string = "UTC"
    ): string {
        try {
            const formatter = this.getFormatter(lang, timeZone);
            return formatter.format(date);
        } catch (e) {
            console.error(`Formatting failed: ${e.message}`);
            // 降级处理：返回 ISO 格式
            return date.toISOString();
        }
    }

    // 获取或创建格式化器
    private getFormatter(lang: string, timeZone: string): Intl.DateTimeFormat {
        const cacheKey = this.generateCacheKey(lang, timeZone);

        // 缓存命中直接返回
        const cachedFormatter = this.formatterCache.get(cacheKey);
        if (cachedFormatter) {
            return cachedFormatter;
        }

        // 缓存未命中创建新实例
        const newFormatter = this.createFormatter(lang, timeZone);
        this.formatterCache.set(cacheKey, newFormatter);
        return newFormatter;
    }

    // 创建格式化器（统一包含时间）
    private createFormatter(
        lang: string,
        timeZone: string
    ): Intl.DateTimeFormat {
        const options: Intl.DateTimeFormatOptions = {
            year: "numeric",
            month: "long",
            weekday: "long",
            day: "numeric",
            hour: "2-digit",
            minute: "2-digit",
            hour12: false,
            timeZone
        };

        return new Intl.DateTimeFormat(lang, options);
    }

    // 生成唯一缓存键
    private generateCacheKey(lang: string, timeZone: string): string {
        return `${lang.toLowerCase()}_${timeZone.toLowerCase()}`;
    }

    // 增强的日期计算方法
    addDays(date: Date, days: number): Date {
        const result = new Date(date);
        result.setUTCDate(result.getUTCDate() + days);

        // 处理夏令时边界情况
        if (result.getUTCHours() !== date.getUTCHours()) {
            result.setUTCHours(date.getUTCHours());
        }

        return result;
    }
}