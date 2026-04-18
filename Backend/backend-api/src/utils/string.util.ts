/**
 * - 去掉首尾空格
 * - 删除所有换行符 (\n, \r\n, \r)
 * - 其它特殊字符（零宽字符、制表符、非断行空格等）不会被清理
 * - 函数主要保证文本在前端显示时不换行且首尾没有多余空格
 * @param str 要清理的字符串
 * @returns 清理后的字符串
 */
export function cleanString(str: string): string {
  return str.trim().replace(/[\r\n]+/g, '');
}

/**
 * 将字符串转换为无重音的平铺格式 (例如: "día" -> "dia", "Café" -> "Cafe")
 */
export function toUnaccent(str: string): string {
  return str
    .normalize('NFD') // 将字符分解为基础字符和重音标记
    .replace(/[\u0300-\u036f]/g, ''); // 移除所有组合重音标记 (Unicode 范围)
}
