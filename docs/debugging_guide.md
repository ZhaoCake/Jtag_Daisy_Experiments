# JTAG Debugging Guide for Dual Ibex Cores

## Quick Start

### 1. Start the simulation
```bash
# Build and run the simulation
make jtag-sim
./build/sim_top
```

### 2. Connect OpenOCD (in another terminal)
```bash
openocd -f openocd.cfg
```

### 3. Connect via Telnet (in yet another terminal)
```bash
telnet localhost 4444
```

## Common OpenOCD Commands

### Target Management
```tcl
# List all targets
targets

# Select a specific target
targets core1
targets core2

# Show target status
targets
```

### Execution Control
```tcl
# Halt the current target
halt

# Resume execution
resume

# Single step
step

# Reset and halt
reset halt
```

### Register Access
```tcl
# Show all registers
reg

# Read a specific register
reg pc
reg sp
reg x10

# Write to a register
reg pc 0x00100000
reg x10 0x12345678
```

### Memory Access
```tcl
# Read memory (display words)
mdw 0x00100000 4        # Read 4 words starting at 0x00100000
mdb 0x00100000 16       # Read 16 bytes
mdh 0x00100000 8        # Read 8 half-words

# Write memory
mww 0x00100000 0x0000006f   # Write word
mwb 0x00100100 0xFF         # Write byte
mwh 0x00100200 0x1234       # Write half-word
```

### Loading Programs
```tcl
# Load a binary file
load_image test.bin 0x00100000 bin

# Load an ELF file
load_image test.elf

# Verify loaded image
verify_image test.bin 0x00100000
```

## Memory Map

| Address Range           | Size  | Description        |
|------------------------|-------|-------------------|
| 0x00100000 - 0x0010FFFF | 64KB  | Main RAM          |
| 0x1A110000 - 0x1A110FFF | 4KB   | Debug Module      |

## Example Debug Session

```tcl
# 1. Halt the core
halt

# 2. Check current PC
reg pc

# 3. Write a simple test program
#    li t0, 0x12345678  ->  lui t0, 0x12345; addi t0, t0, 0x678
mww 0x00100000 0x123452b7    # lui t0, 0x12345
mww 0x00100004 0x67828293    # addi t0, t0, 0x678

# 4. Add infinite loop at end
mww 0x00100008 0x0000006f    # j .

# 5. Set PC to start
reg pc 0x00100000

# 6. Single step through
step
reg t0    # Should be 0x12345000
step  
reg t0    # Should be 0x12345678
step
reg pc    # Should still be 0x00100008 (infinite loop)

# 7. Resume execution
resume

# 8. Halt and check again
halt
reg pc
```

## GDB Connection

You can also connect using GDB:
```bash
# For core1
riscv32-unknown-elf-gdb
(gdb) target remote localhost:3334

# For core2
riscv32-unknown-elf-gdb
(gdb) target remote localhost:3333
```

## Troubleshooting

### "Hart 0 unexpectedly reset!"
This happens when the core is running without valid instructions in memory.
Solution: Halt the core first, then load a program before resuming.

### Memory access timeouts
The simulation may be slower than real hardware. Try:
```tcl
riscv set_command_timeout_sec 30
```

### SBA (System Bus Access) failures
If abstract commands fail, OpenOCD will try using the program buffer or SBA.
Make sure the memory addresses are within valid ranges.
