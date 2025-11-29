# 双 Ibex 核心 JTAG 调试指南

## 快速开始

### 1. 启动仿真
```bash
# 编译并运行仿真
make jtag-sim
./build/sim_top
```

### 2. 连接 OpenOCD（在另一个终端）
```bash
openocd -f openocd.cfg
```

### 3. 通过 Telnet 连接（在第三个终端）
```bash
telnet localhost 4444
```

## 常用 OpenOCD 命令

### 目标管理
```tcl
# 列出所有目标
targets

# 选择特定目标
targets core1
targets core2

# 显示目标状态
targets
```

### 执行控制
```tcl
# 暂停当前目标
halt

# 恢复执行
resume

# 单步执行
step

# 复位并暂停
reset halt
```

### 寄存器访问
```tcl
# 显示所有寄存器
reg

# 读取特定寄存器
reg pc
reg sp
reg x10

# 写入寄存器
reg pc 0x00100000
reg x10 0x12345678
```

### 内存访问
```tcl
# 读取内存（按字显示）
mdw 0x00100000 4        # 从 0x00100000 开始读取 4 个字
mdb 0x00100000 16       # 读取 16 个字节
mdh 0x00100000 8        # 读取 8 个半字

# 写入内存
mww 0x00100000 0x0000006f   # 写入字
mwb 0x00100100 0xFF         # 写入字节
mwh 0x00100200 0x1234       # 写入半字
```

### 加载程序
```tcl
# 加载二进制文件
load_image sw/test.bin 0x00100000 bin

# 加载 ELF 文件
load_image sw/test.elf

# 验证已加载的镜像
verify_image sw/test.bin 0x00100000
```

## 内存映射

| 地址范围                 | 大小  | 描述               |
|-------------------------|-------|-------------------|
| 0x00100000 - 0x0010FFFF | 64KB  | 主内存 (RAM)       |
| 0x1A110000 - 0x1A110FFF | 4KB   | 调试模块 (DM)      |

## 调试示例

```tcl
# 1. 暂停核心
halt

# 2. 查看当前 PC
reg pc

# 3. 手动写入简单测试程序
#    li t0, 0x12345678  ->  lui t0, 0x12345; addi t0, t0, 0x678
mww 0x00100000 0x123452b7    # lui t0, 0x12345
mww 0x00100004 0x67828293    # addi t0, t0, 0x678

# 4. 添加无限循环
mww 0x00100008 0x0000006f    # j .

# 5. 设置 PC 到起始位置
reg pc 0x00100000

# 6. 单步执行
step
reg t0    # 应该是 0x12345000
step  
reg t0    # 应该是 0x12345678
step
reg pc    # 应该仍在 0x00100008（无限循环）

# 7. 恢复执行
resume

# 8. 再次暂停并检查
halt
reg pc
```

## GDB 连接

### 基本连接

```bash
# 进入 nix 开发环境
nix develop

# 启动 GDB 并加载 ELF 文件（推荐）
gdb sw/test.elf

# 在 GDB 中连接到 OpenOCD
(gdb) set arch riscv:rv32
(gdb) target remote localhost:3333    # 连接 core2
# 或
(gdb) target remote localhost:3334    # 连接 core1
```

### 完整调试流程

```bash
# 1. 启动 GDB 并加载程序
gdb sw/test.elf

# 2. 连接到目标
(gdb) target remote localhost:3333

# 3. 加载程序到目标内存
(gdb) load

# 4. 设置断点
(gdb) break _start
(gdb) break loop

# 5. 运行程序
(gdb) continue

# 6. 单步调试
(gdb) step       # 单步（进入函数）
(gdb) next       # 单步（跳过函数）
(gdb) stepi      # 单条指令

# 7. 查看寄存器
(gdb) info registers
(gdb) info reg pc
(gdb) info reg t0 t1 t2

# 8. 查看内存
(gdb) x/4xw 0x00100000    # 显示 4 个字
(gdb) x/10i $pc           # 反汇编当前位置的 10 条指令

# 9. 修改寄存器/内存
(gdb) set $pc = 0x00100000
(gdb) set {int}0x00100100 = 0x12345678
```

### 无符号文件时的连接

如果没有 ELF 文件，可以直接连接：

```bash
gdb
(gdb) set arch riscv:rv32
(gdb) target remote localhost:3333
```

> **注意**：如果看到 `Ignoring packet error, continuing...` 警告，通常是因为：
> 1. 仿真速度较慢，增加 OpenOCD 超时时间
> 2. 核心处于复位状态，先在 OpenOCD telnet 中执行 `halt`

### GDB 常用命令速查

| 命令 | 说明 |
|------|------|
| `target remote HOST:PORT` | 连接远程目标 |
| `load` | 加载程序到目标 |
| `continue` / `c` | 继续执行 |
| `step` / `s` | 单步（进入函数） |
| `next` / `n` | 单步（跳过函数） |
| `stepi` / `si` | 单条指令 |
| `break ADDR` | 设置断点 |
| `delete` | 删除所有断点 |
| `info registers` | 显示所有寄存器 |
| `x/FMT ADDR` | 查看内存 |
| `disassemble` | 反汇编当前函数 |
| `monitor CMD` | 发送命令到 OpenOCD |

### 通过 GDB 控制 OpenOCD

```gdb
# 在 GDB 中执行 OpenOCD 命令
(gdb) monitor halt
(gdb) monitor resume
(gdb) monitor reg pc
(gdb) monitor mdw 0x00100000 4
```

## 故障排除

### 已解决的问题

#### 1. Hart 意外复位 ("Hart unexpectedly reset!")

**症状**：OpenOCD 连接后报告 `Hart 0 unexpectedly reset!`，核心不断复位。

**原因**：`fetch_enable_i` 信号配置错误。Ibex 使用 MuBi4 编码：
- `IbexMuBiOn = 4'b0101 (0x5)` - 允许取指
- `IbexMuBiOff = 4'b1010 (0xA)` - 禁止取指

**解决方案**：在 `IbexSystem.scala` 中设置正确的值：
```scala
ibex.io.fetch_enable_i := 0x5.U  // IbexMuBiOn
```

#### 2. 核心执行垃圾指令

**症状**：仿真中报告非法指令，核心行为异常。

**原因**：核心从 `boot_addr` 开始执行，但 RAM 中没有有效程序。

**解决方案**：在 `openocd.cfg` 中启动时自动加载测试程序：
```tcl
# 在 init 之后添加
halt
load_image sw/test.bin 0x00100000 bin
reg pc 0x00100000
```

#### 3. SBA 内存访问失败

**症状**：`mdw` 命令对某些地址失败。

**原因**：SBA（System Bus Access）只能访问 RAM 地址范围 (`0x00100000 - 0x0010FFFF`)，不能访问 Debug Module 地址 (`0x1A110xxx`)。

**解决方案**：这是正常行为。DM 地址应该由核心通过 slave 接口访问，不通过 SBA。

#### 4. 编译错误：缺少子模块

**症状**：Verilator 编译失败，找不到 IP 文件。

**解决方案**：初始化并更新 git 子模块：
```bash
git submodule update --init --recursive
```

### 已知问题

#### GDB 连接时 "Ignoring packet error"

**症状**：GDB 连接时出现多次 `Ignoring packet error, continuing...`，某些命令超时。

**根本原因分析**：

1. **dmstatus.anyhalted = 0**：尽管 telnet 中 `halt` 命令看起来成功，但 `dmstatus` 寄存器显示核心没有通过正常途径报告 halted 状态。这表明核心可能没有正确执行 Debug ROM 中的代码来设置 `halted` 标志。

2. **Abstract command 工作正常**：telnet 中 `reg` 命令能读取所有寄存器，`abstractcs.cmderr = 0` 没有错误。

3. **GDB 读取大量寄存器**：GDB 连接时会尝试读取所有寄存器（包括几百个 CSR），仿真速度慢导致超时。

4. **无效内存访问**：GDB 尝试读取 `0xffffc` 等无效地址（查找栈帧），这些地址不在 RAM 范围内。

**当前状态**：
- ✅ Telnet 调试完全正常（`halt`、`step`、`resume`、`reg`、`mdw` 都工作）
- ⚠️ GDB 可以连接，但有 packet error
- ❌ GDB 中 `info registers` 和 `x` 命令可能超时

**临时解决方案**：
1. 增加 GDB 超时时间：
   ```gdb
   set remotetimeout 600
   ```

2. 使用 telnet 进行调试（功能完整，无超时问题）

3. 强制使用 SBA 访问内存：
   ```tcl
   riscv set_mem_access sysbus
   ```

**待深入调查**：
- Ibex 与 riscv-dbg 的 Debug ROM 交互
- 核心进入 debug mode 后是否正确执行 Debug ROM 代码
- `halted` 标志为何没有被设置

### 通用故障排除

#### 内存访问超时
仿真可能比真实硬件慢。增加超时时间：
```tcl
riscv set_command_timeout_sec 120
```

#### 检查 Debug Module 状态
```tcl
# 读取 dmstatus
riscv dmi_read 0x11

# 读取 dmcontrol
riscv dmi_read 0x10

# 读取 abstractcs（检查错误）
riscv dmi_read 0x16
```

#### DMI 寄存器快速参考

| 地址 | 寄存器 | 说明 |
|------|--------|------|
| 0x10 | dmcontrol | 控制寄存器（haltreq, resumereq 等）|
| 0x11 | dmstatus | 状态寄存器（halted, running 等）|
| 0x16 | abstractcs | Abstract command 状态（busy, cmderr）|
| 0x17 | command | Abstract command |
| 0x04-0x0F | data0-data11 | Abstract command 数据 |
| 0x20-0x2F | progbuf0-15 | Program buffer |

## 架构说明

### 调试信号流

```
OpenOCD <--JTAG--> dmi_jtag <--DMI--> dm_top <--debug_req--> Ibex
                                        |
                                        +--> slave 接口 (Debug ROM/Data)
                                        +--> master 接口 (SBA)
```

### 调试流程

1. OpenOCD 通过 JTAG 发送 halt 请求
2. `dm_top` 设置 `debug_req_o = 1`
3. Ibex 收到 `debug_req_i`，进入 debug mode
4. Ibex 跳转到 `DmHaltAddr` (0x1A110800) 执行 Debug ROM
5. Debug ROM 写入 `HALTED` 地址 (0x1A110100) 通知 DM
6. DM 设置 `dmstatus.anyhalted = 1`
7. OpenOCD 可以执行 abstract command 读写寄存器
