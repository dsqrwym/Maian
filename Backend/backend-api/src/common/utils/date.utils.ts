/**
 * 在指定日期上加/减分钟
 * @param date - 原始日期
 * @param minutes - 要加/减的分钟数，负数表示减
 * @returns 返回新的 Date 对象
 */
function addMinutes(date: Date, minutes: number): Date {
  return new Date(date.getTime() + minutes * 60_000); // 60,000 毫秒 = 1 分钟
}

/**
 * 在指定日期上加/减秒
 * @param date - 原始日期
 * @param seconds - 要加/减的秒数，负数表示减
 * @returns 返回新的 Date 对象
 */
function addSeconds(date: Date, seconds: number): Date {
  return new Date(date.getTime() + seconds * 1_000); // 1,000 毫秒 = 1 秒
}

/**
 * 在指定日期上加/减小时
 * @param date - 原始日期
 * @param hours - 要加/减的小时数，负数表示减
 * @returns 返回新的 Date 对象
 */
function addHours(date: Date, hours: number): Date {
  return new Date(date.getTime() + hours * 3_600_000); // 3,600,000 毫秒 = 1 小时
}

export { addSeconds, addMinutes, addHours };
