import { generateUniformRandomDigits } from '../src/utils/random.utils';

function bench(fn: () => void, label: string, iterations = 1000) {
  const start = performance.now();
  for (let i = 0; i < iterations; i++) fn();
  const end = performance.now();
  const total = end - start;
  console.log(
    `${label.padEnd(45)} ${iterations} 次 | 总耗时: ${total.toFixed(
      2,
    )} ms | 平均: ${(total / iterations).toFixed(4)} ms`,
  );
}

console.log(' 开始性能对比测试...\n');

const PASSWORD_LENGTH = 15;
const ITERATIONS = 5000;

bench(
  () => generateUniformRandomDigits(PASSWORD_LENGTH),
  '优化版 generateUniformStrongPasswordOptimized',
  ITERATIONS,
);

console.log('\n测试完成');
