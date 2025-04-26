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

    createFormatter(lang: string, withTime: boolean = false, timeZone?:string): Intl.DateTimeFormat {
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
        const hasTime = date.getHours() !== 0 || date.getMinutes() !== 0;

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