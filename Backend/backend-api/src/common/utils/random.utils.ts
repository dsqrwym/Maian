/**
 * 生成指定长度的随机数字字符串（使用安全随机数生成器）
 * - crypto.getRandomValues() 基于操作系统的安全随机源，满足密码学安全需求。
 *
 * @param length 随机数字的长度
 * @returns 仅包含数字的字符串
 */
export function generateUniformRandomDigits(length: number): string {
  if (length <= 0) throw new Error('Length must be greater than 0');

  let result = '';

  while (result.length < length) {
    // 生成一个 0~255 的安全随机字节
    const byte = crypto.getRandomValues(new Uint8Array(1))[0];
    // 只接受 0~249 的值, 250~255 会舍弃，因为 256 % 10 = 6 会造成偏差
    if (byte < 250) {
      // 取模 10 后，结果严格均匀分布在 0~9
      result += (byte % 10).toString();
    }
  }

  return result;
}
