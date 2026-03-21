#!/bin/bash

##
# 综合性能基准测试运行脚本
#
# 该脚本运行所有性能基准测试，并生成综合报告
#
# 功能：
# 1. 运行 idgen 模块的UID、BID、RingBuffer基准测试
# 2. 运行 remote 模块的API响应基准测试
# 3. 汇总所有基准测试结果
# 4. 生成性能基准报告
#
# 使用方式：
#   ./run-benchmarks.sh [output-dir]
#
# 示例：
#   ./run-benchmarks.sh ./benchmark-results
##

set -e

# 配置参数
OUTPUT_DIR="${1:-.}"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BENCHMARK_DIR="$OUTPUT_DIR/benchmarks_$TIMESTAMP"
REPORT_FILE="$BENCHMARK_DIR/BENCHMARK_REPORT.md"

echo "================================================"
echo "性能基准测试框架启动"
echo "================================================"
echo "输出目录: $BENCHMARK_DIR"
echo "时间戳: $TIMESTAMP"
echo ""

# 创建输出目录
mkdir -p "$BENCHMARK_DIR"

# 初始化报告文件
cat > "$REPORT_FILE" << 'EOF'
# AngusInfra 性能基准测试报告

## 执行信息
EOF

echo "执行时间: $(date)" >> "$REPORT_FILE"
echo "运行系统: $(uname -a)" >> "$REPORT_FILE"
echo "Java版本: $(java -version 2>&1 | head -1)" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"

# 函数：运行单个基准测试
run_benchmark() {
  local benchmark_class=$1
  local benchmark_name=$2
  local module=$3
  
  echo ""
  echo "================================================"
  echo "正在运行: $benchmark_name"
  echo "================================================"
  echo ""
  
  local result_file="$BENCHMARK_DIR/${benchmark_class}.json"
  
  # 运行基准测试
  if cd "$module"; then
    mvn clean test -Dtest="$benchmark_class" 2>&1 | tee -a "$BENCHMARK_DIR/execution.log"
    
    if [ -f "$result_file" ]; then
      echo "✅ $benchmark_name 完成，结果已保存"
    else
      echo "⚠️  $benchmark_name 完成但找不到JSON结果（可能使用了不同的运行方式）"
    fi
    
    cd - > /dev/null
  else
    echo "❌ 无法进入目录: $module"
  fi
  
  echo "" >> "$REPORT_FILE"
  echo "### $benchmark_name" >> "$REPORT_FILE"
  echo "" >> "$REPORT_FILE"
  
  if [ -f "$result_file" ]; then
    # 提取JSON中的关键性能指标
    echo '```json' >> "$REPORT_FILE"
    cat "$result_file" >> "$REPORT_FILE"
    echo '```' >> "$REPORT_FILE"
  else
    echo "结果文件: 未找到 (benchmark_${benchmark_class}.json)" >> "$REPORT_FILE"
  fi
}

# 运行所有基准测试
echo "" >> "$REPORT_FILE"
echo "## 基准测试详情" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"

# idgen 模块
IDGEN_MODULE="/workspaces/AngusInfra/idgen/starter"
run_benchmark "UidGeneratorBenchmark" "UID生成器性能基准测试" "$IDGEN_MODULE"
run_benchmark "BidGeneratorBenchmark" "BID生成器性能基准测试" "$IDGEN_MODULE"
run_benchmark "RingBufferBenchmark" "RingBuffer性能基准测试" "$IDGEN_MODULE"

# remote 模块
REMOTE_MODULE="/workspaces/AngusInfra/remote"
run_benchmark "ApiResponseBenchmark" "API响应序列化性能基准测试" "$REMOTE_MODULE"

# 生成性能总结
cat >> "$REPORT_FILE" << 'EOF'

## 性能指标总结

### idgen 模块

#### UID生成性能目标
| 场景 | 目标吞吐量 | 说明 |
|------|-----------|------|
| 单线程顺序 | ≥6.5M ops/sec | Snowflake算法的标准性能 |
| 多线程并发 | ≥5M ops/sec | 允许30%的并发开销 |
| 批量生成(100) | 线性扩展 | 批大小 x 单线程吞吐 |

#### BID生成性能目标
| 场景 | 目标吞吐量 | 说明 |
|------|-----------|------|
| 单租户单业务 | ≥200K ops/sec | 基准性能 |
| 多租户并发 | ≥100K ops/sec | 支持10个租户 |
| 多线程并发 | ≥80K ops/sec | 线程间竞争开销 |

#### RingBuffer性能目标
| 场景 | 目标吞吐量 | 说明 |
|------|-----------|------|
| 单线程Put | ≥50M ops/sec | 环形缓冲无阻塞操作 |
| 单线程Take | ≥50M ops/sec | 消费预缓存的UID |
| 多线程生产-消费 | ≥20M ops/sec | 生产-消费平衡 |

### remote 模块

#### API响应序列化性能目标
| 场景 | 目标延迟 | 目标吞吐量 | 说明 |
|------|--------|---------|------|
| 简单对象序列化 | <0.5ms | >200K ops/sec | String返回 |
| 复杂对象序列化 | <1.0ms | >100K ops/sec | 嵌套DTO |
| 列表序列化 | <2.0ms | >50K ops/sec | List<DTO> |
| 反序列化 | <1.0ms | >100K ops/sec | 与序列化对称 |

## 性能调优建议

### idgen 模块优化机会
1. **RingBuffer填充因子调整** - 根据CPU物理核数调整PADDING_FACTOR
2. **UID时间戳精度** - 考虑使用System.currentTimeMillis()缓存（机制已在CachedUidGenerator中实现）
3. **Rejection Policy选择** - 在BlockPolicy和DiscardPolicy间选择（取决于丢ID容忍度）
4. **批量生成优化** - 为高吞吐场景预消费多个ID

### remote模块优化机会
1. **Jackson缓存优化** - 使用ObjectMapper单例（已在框架中实现）
2. **序列化格式** - 考虑Protocol Buffers或其他二进制格式减小序列化时间
3. **异步响应处理** - 对大型集合使用流式serialize
4. **缓存序列化结果** - 对频繁返回的相同对象进行缓存

## 性能验证检查清单

- [ ] UID单线程吞吐达到6.5M ops/sec
- [ ] UID多线程吞吐≥5M ops/sec
- [ ] BID单线程吞吐达到200K ops/sec  
- [ ] BID支持10个并发租户
- [ ] RingBuffer单线程吞吐>50M ops/sec
- [ ] API序列化对象延迟<1ms
- [ ] 无OOM异常或内存泄漏检测
- [ ] CPU利用率在可接受范围内（<80%持续）

## 下一步行动

1. **建立性能基线** - 使用本基准测试作为CI/CD流程中的性能检查点
2. **定期回归测试** - 在每个版本发布前运行此基准测试
3. **性能监控集成** - 在Micrometer指标中记录这些基准数值
4. **持续优化** - 根据实际生产负载调整参数（boostPower, paddingFactor等）

EOF

# 生成最终报告
echo ""
echo "================================================"
echo "基准测试执行完成"
echo "================================================"
echo ""
echo "报告生成位置: $REPORT_FILE"
echo ""
echo "关键结果："
echo "  - UID生成器基准测试: $([ -f "$BENCHMARK_DIR/UidGeneratorBenchmark.json" ] && echo '✅ 完成' || echo '⏳ 待结果')"
echo "  - BID生成器基准测试: $([ -f "$BENCHMARK_DIR/BidGeneratorBenchmark.json" ] && echo '✅ 完成' || echo '⏳ 待结果')"
echo "  - RingBuffer基准测试: $([ -f "$BENCHMARK_DIR/RingBufferBenchmark.json" ] && echo '✅ 完成' || echo '⏳ 待结果')"
echo "  - API响应基准测试: $([ -f "$BENCHMARK_DIR/ApiResponseBenchmark.json" ] && echo '✅ 完成' || echo '⏳ 待结果')"
echo ""

# 显示报告内容摘要
if [ -f "$REPORT_FILE" ]; then
  echo "报告摘要："
  head -50 "$REPORT_FILE"
fi
