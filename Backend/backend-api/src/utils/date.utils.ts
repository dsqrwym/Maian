const SECOND = 1000;
const MINUTE = 60 * SECOND;
const HOUR = 60 * MINUTE;
const DAY = 24 * HOUR;

/**
 * 在指定日期上加/减分钟
 * @param date - 原始日期
 * @param minutes - 要加/减的分钟数，负数表示减
 * @returns 返回新的 Date 对象
 */
function addMinutes(date: Date, minutes: number): Date {
  return new Date(date.getTime() + minutes * MINUTE); // 60,000 毫秒 = 1 分钟
}

/**
 * 在指定日期上加/减秒
 * @param date - 原始日期
 * @param seconds - 要加/减的秒数，负数表示减
 * @returns 返回新的 Date 对象
 */
function addSeconds(date: Date, seconds: number): Date {
  return new Date(date.getTime() + seconds * SECOND); // 1,000 毫秒 = 1 秒
}

/**
 * 在指定日期上加/减小时
 * @param date - 原始日期
 * @param hours - 要加/减的小时数，负数表示减
 * @returns 返回新的 Date 对象
 */
function addHours(date: Date, hours: number): Date {
  return new Date(date.getTime() + hours * HOUR); // 3,600,000 毫秒 = 1 小时
}

/**
 * 在指定日期上加天数
 * @param date - 原始日期
 * @param days - 要增加的天数，负数表示减
 * @returns 返回新的 Date 对象（UTC 时间安全，可用于数据库查询）
 */
function addDays(date: Date, days: number): Date {
  // 1 天 = 24 小时 * 60 分钟 * 60 秒 * 1000 毫秒
  const milliseconds = days * DAY;
  return new Date(date.getTime() + milliseconds);
}

/**
 * 在指定日期上减天数
 * @param date - 原始日期
 * @param days - 要减少的天数，负数表示加
 * @returns 返回新的 Date 对象（UTC 时间安全，可用于数据库查询）
 */
function reduceDay(date: Date, days: number): Date {
  // 1 天 = 24 小时 * 60 分钟 * 60 秒 * 1000 毫秒
  const milliseconds = days * DAY;
  return new Date(date.getTime() - milliseconds);
}

export {
  addSeconds,
  addMinutes,
  addHours,
  addDays,
  reduceDay,
  SECOND,
  HOUR,
  DAY,
  MINUTE,
};
