/**
 * 1. 全局类型扩展 (Global Type Augmentation)
 * 必须使用 declare global，让 TypeScript 编译器知道 BigInt 实例上新增了 toJSON 方法。
 * 这样在后续代码中调用 JSON.stringify 时，TS 不会报错。
 */
declare global {
  interface BigInt {
    toJSON(): string;
  }
}

/**
 * 2. 运行时原型注入 (Runtime Polyfill)
 * JSON.stringify 在处理对象时，如果发现对象有 toJSON 方法，会调用它。
 * 因为原生 BigInt 没有这个方法，导致序列化报错。这里补全它。
 */
BigInt.prototype.toJSON = function (this: bigint): string {
  return this.toString();
};
/**
 * 理论上以上操作应该在程序运行之前，所以程序入口为下
 */
import { bootstrap } from './bootstrap.js';

void bootstrap(); // 启动应用程序，创建 NestJS 应用实例并配置相关功能
