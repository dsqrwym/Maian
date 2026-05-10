/**
 * Spanish tax identification type.
 *
 * 西班牙税号类型：
 *
 * - DNI:
 *   西班牙公民个人身份证 / 税号。
 *   格式：12345678Z
 *
 * - NIE:
 *   外国居民身份号 / 税号。
 *   格式：X1234567L
 *
 * - CIF:
 *   企业/组织税号（历史名称）。
 *   现代法律层面属于 business NIF。
 *   格式：B12345678 / P1234567A
 */
type SpanishTaxIdKind = 'DNI' | 'NIE' | 'CIF';

/**
 * DNI/NIE 控制字母映射表。
 *
 * 算法：
 * number % 23
 * 然后根据余数取对应字母。
 *
 * 例如：
 * 12345678 % 23 = 14
 * => Z
 */
const DNI_NIE_CONTROL_LETTERS = 'TRWAGMYFPDXBNJZSQVHLCKE';

/**
 * CIF 控制字母映射表。
 *
 * CIF 的校验位可能是：
 * - 数字
 * - 字母
 *
 * 数字与字母映射：
 * 0 -> J
 * 1 -> A
 * 2 -> B
 * 3 -> C
 * ...
 * 9 -> I
 */
const CIF_CONTROL_LETTERS = 'JABCDEFGHI';

/**
 * 这些实体类型的 CIF
 * 控制位必须为数字。
 *
 * 常见：
 * - A: Sociedad Anónima
 * - B: Sociedad Limitada
 * - E: Comunidades de bienes
 * - H: Comunidades de propietarios
 */
const CIF_MUST_BE_DIGIT = new Set(['A', 'B', 'E', 'H']);

/**
 * 这些实体类型的 CIF
 * 控制位必须为字母。
 *
 * 常见：
 * - K: 老式个人实体
 * - P: 公共机构
 * - Q: 公共组织
 * - S: 国家机构
 * - W: 常设机构
 */
const CIF_MUST_BE_LETTER = new Set(['K', 'P', 'Q', 'S', 'W']);

/**
 * 验证西班牙税号是否合法。
 * 支持：
 * - DNI
 * - NIE
 * - CIF / business NIF
 * 注意：
 * 输入必须已经是 canonical format：
 * - 大写
 * - 无空格
 * - 无 "-"
 * - 无 "ES"
 */
export function isValidSpanishTaxId(taxId: string): boolean {
  return getSpanishTaxIdKind(taxId) !== null;
}

/**
 * 检测西班牙税号类型并验证控制位。
 * 返回：
 * - 'DNI'
 * - 'NIE'
 * - 'CIF'
 * - null（非法）
 */
function getSpanishTaxIdKind(taxId: string): SpanishTaxIdKind | null {
  const value = taxId.toUpperCase();

  /**
   * NIE:
   * X/Y/Z + 7 digits + letter
   * 例：
   * X1234567L
   */
  if (/^\d{8}[A-Z]$/.test(value)) {
    return isValidDni(value) ? 'DNI' : null;
  }

  /**
   * CIF / business NIF:
   * entity letter + 7 digits + control char
   * 例：
   * B12345678
   * P1234567A
   */
  if (/^[XYZ]\d{7}[A-Z]$/.test(value)) {
    return isValidNie(value) ? 'NIE' : null;
  }

  /**
   * 验证 DNI。
   *
   * 算法：
   * 1. 取前 8 位数字
   * 2. number % 23
   * 3. 对照控制字母表
   */
  if (/^[ABCDEFGHJNPQRSUVW]\d{7}[0-9A-J]$/.test(value)) {
    return isValidCif(value) ? 'CIF' : null;
  }

  return null;
}

/**
 * 验证 DNI。
 *
 * 算法：
 * 1. 取前 8 位数字
 * 2. number % 23
 * 3. 对照控制字母表
 */
function isValidDni(dni: string): boolean {
  const number = Number(dni.slice(0, 8));
  const controlLetter = dni[8];

  return DNI_NIE_CONTROL_LETTERS[number % 23] === controlLetter;
}

/**
 * 验证 NIE。
 *
 * 算法：
 * 1. X -> 0
 *    Y -> 1
 *    Z -> 2
 * 2. 转换为普通数字
 * 3. 使用 DNI 相同算法：
 *    number % 23
 */
function isValidNie(nie: string): boolean {
  const first = nie[0];
  const prefix =
    first === 'X' ? '0' : first === 'Y' ? '1' : first === 'Z' ? '2' : null;

  if (prefix === null) return false;

  const number = Number(prefix + nie.slice(1, 8));
  const controlLetter = nie[8];

  return DNI_NIE_CONTROL_LETTERS[number % 23] === controlLetter;
}

/**
 * 验证 CIF / business NIF。
 *
 * 算法（官方 checksum）：
 *
 * 1. 对奇数位数字乘 2
 * 2. 若结果 >= 10，则拆分数字相加
 *    例如：
 *    16 -> 1 + 6 = 7
 * 3. 偶数位直接相加
 * 4. 总和取：
 *    (10 - (sum % 10)) % 10
 * 5. 得到控制位
 *
 * 不同实体类型：
 * - 有些必须数字控制位
 * - 有些必须字母控制位
 * - 有些两者都允许
 */
function isValidCif(cif: string): boolean {
  const entityLetter = cif[0];
  const digits = cif.slice(1, 8);
  const control = cif[8];

  let sum = 0;

  for (let i = 0; i < digits.length; i++) {
    const n = Number(digits[i]);

    if (i % 2 === 0) {
      const doubled = n * 2;
      sum += Math.floor(doubled / 10) + (doubled % 10);
    } else {
      sum += n;
    }
  }

  const controlDigit = (10 - (sum % 10)) % 10;
  const expectedDigit = String(controlDigit);
  const expectedLetter = CIF_CONTROL_LETTERS[controlDigit];

  if (CIF_MUST_BE_DIGIT.has(entityLetter)) {
    return control === expectedDigit;
  }

  if (CIF_MUST_BE_LETTER.has(entityLetter)) {
    return control === expectedLetter;
  }

  return control === expectedDigit || control === expectedLetter;
}
