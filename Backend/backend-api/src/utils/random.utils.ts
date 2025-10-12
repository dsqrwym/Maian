/**
 * 生成指定长度的随机数字字符串（使用安全随机数生成器）
 * - crypto.getRandomValues() 基于操作系统的安全随机源，满足密码学安全需求。
 * - 使用拒绝采样避免取模偏差
 *
 * @param length 随机数字的长度
 * @returns 仅包含数字的字符串
 */
export function generateUniformRandomDigits(length: number): string {
  if (length <= 0) throw new Error('Length must be greater than 0');

  const result: string[] = [];
  const buf = new Uint8Array(length * 1.5);

  while (result.length < length) {
    // 生成一个 0~255 的安全随机字节
    const byte = crypto.getRandomValues(buf);
    for (let i = 0; i < byte.length && result.length < length; i++) {
      const byte = buf[i];
      // 只接受 0~249 的值, 250~255 会舍弃，因为 256 % 10 = 6 会造成偏差
      if (byte < 250) {
        // 取模 10 后，结果严格均匀分布在 0~9
        result.push((byte % 10).toString());
      }
    }
  }

  return result.join('');
}

/**
 * 生成一个安全、完全随机的强密码。
 *
 * - 使用 crypto.getRandomValues() 提供密码学安全的随机源；
 * - 使用拒绝采样（rejection sampling）避免取模偏差；
 * - 确保至少包含 1 个大写字母、1 个小写字母、1 个数字；
 * - 可选包含符号；
 * - 通过缓冲区批量生成随机字节，提高性能；
 * - 使用 Fisher–Yates 洗牌算法打乱顺序，避免 predictable pattern。
 * - 最大长度为 256, 不然会产生死循环
 *
 * @param length 密码长度（最小 6）
 * @param includeSymbols 是否包含符号字符（默认 true）
 * @returns 生成的强密码字符串
 */
export function generateUniformStrongPassword(
  length = 10,
  includeSymbols = true,
): string {
  if (length < 6) throw new Error('Password length must be >= 6');
  if (length > 256) throw new Error('Password length must be <= 256');

  // 定义字符集分类
  const LOWERCASE = 'abcdefghijklmnopqrstuvwxyz';
  const UPPERCASE = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
  const DIGITS = '0123456789';
  const SYMBOLS = '!@#$%^&*()_+-=[]{}|;:,.<>?';

  // 拼接完整字符集
  const CHARSET = includeSymbols
    ? LOWERCASE + UPPERCASE + DIGITS + SYMBOLS
    : LOWERCASE + UPPERCASE + DIGITS;

  /**
   * 为减少频繁调用 crypto.getRandomValues() 带来的系统开销，
   * 我们一次性分配一个随机缓冲区（默认至少 64 字节或 length * 4）
   * 并从中按需提取字节，缓冲区用尽后再刷新。
   */
  const BUFFER_SIZE = Math.max(64, length * 4);
  const randomBuffer = new Uint8Array(BUFFER_SIZE);
  let bufferIndex = BUFFER_SIZE; // 初始化为已用完，强制第一次 refill()

  /** 重新填充随机缓冲区 */
  function refillBuffer(): void {
    crypto.getRandomValues(randomBuffer);
    bufferIndex = 0;
  }

  /** 获取一个随机字节（0~255），用尽则自动 refill */
  function nextRandomByte(): number {
    if (bufferIndex >= randomBuffer.length) refillBuffer();
    return randomBuffer[bufferIndex++];
  }

  /**
   * 从给定字符集中选择一个字符，使用拒绝采样避免偏差。
   * 例如当字符集长度不能整除 256 时，通过丢弃部分随机值来保持均匀分布。
   */
  function getUniformChar(chars: string): string {
    const maxValidByte = 256 - (256 % chars.length);
    while (true) {
      const byte = nextRandomByte();
      if (byte < maxValidByte) return chars[byte % chars.length];
    }
  }

  // 初始化密码字符数组
  const passwordChars: string[] = new Array<string>(length);

  // 确保密码至少包含小写、大写和数字
  passwordChars[0] = getUniformChar(LOWERCASE);
  passwordChars[1] = getUniformChar(UPPERCASE);
  passwordChars[2] = getUniformChar(DIGITS);

  // 其余位置随机填充
  for (let i = 3; i < length; i++) {
    passwordChars[i] = getUniformChar(CHARSET);
  }

  /**
   * Fisher–Yates 洗牌算法
   * 通过拒绝采样生成均匀分布的随机索引，随机打乱字符顺序。
   */
  for (let i = length - 1; i > 0; i--) {
    const maxValidByte = 256 - (256 % (i + 1));
    let byte = nextRandomByte();
    while (byte >= maxValidByte) byte = nextRandomByte();
    const j = byte % (i + 1);
    [passwordChars[i], passwordChars[j]] = [passwordChars[j], passwordChars[i]];
  }

  // 返回拼接后的密码字符串
  return passwordChars.join('');
}
