import { Injectable } from "@nestjs/common";

@Injectable()
export class DateFormatService {
    private formatterCache = new Map<string, Intl.DateTimeFormat>();

    constructor() {
        this.preloadLanguages(['en', 'es', 'zh-CN']);
    }

    preloadLanguages(languages: string[]) {
        languages.forEach(lang => {
            this.formatterCache.set(lang, new Intl.DateTimeFormat(lang, {
                year: 'numeric',
                month: 'long',
                weekday: 'long',
                day: 'numeric',
                hour12: false,
            }));
        });
    }

    createFormatter(lang: string, withTime: boolean = false, timeZone?: string): Intl.DateTimeFormat {
        const option: Intl.DateTimeFormatOptions = {
            year: 'numeric',
            month: 'long',
            weekday: 'long',
            day: 'numeric',
            ...(withTime ? { hour: '2-digit', minute: '2-digit' } : {}),
            hour12: false,
            timeZone: timeZone || 'UTC', // 默认时区为 UTC
        }

        return new Intl.DateTimeFormat(lang, option);
    }


    formatDate(date: Date, lang: string = 'en', timeZone: string = 'UTC'): string {
        // 通过给定时区格式化日期，获取该时区的时间
        const options: Intl.DateTimeFormatOptions = {
            timeZone: timeZone || 'UTC',
            hour: '2-digit',
            minute: '2-digit',
            hour12: false,
        };

        // 获取该时区的时间
        const formattedDate = new Intl.DateTimeFormat(lang, options).format(date);

        // 分析时间部分：提取小时和分钟
        const [hour, minute] = formattedDate.split(':').map(Number);

        // 判断时间部分是否有效（小时和分钟是否都为 00）
        const hasTime = !(hour === 0 && minute === 0);


        const cacheKey = `${lang}_${timeZone}_${hasTime ? 'datetime' : 'date'}`;

        if (!this.formatterCache.has(cacheKey)) {
            this.formatterCache.set(cacheKey, this.createFormatter(lang, hasTime, timeZone));
        }

        return this.formatterCache.get(cacheKey)!.format(date);
    }

    addDays(date: Date, days: number): Date {
        const result = new Date(date); //我的数据库日期但是UTC时间
        result.setUTCDate(result.getUTCDate() + days);
        return result;
    }
}