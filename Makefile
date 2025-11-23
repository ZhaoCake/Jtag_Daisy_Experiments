BUILD_DIR = ./build

PRJ = playground

test:
	mill -i $(PRJ).test

verilog:
	$(call git_commit, "generate verilog")
	mkdir -p $(BUILD_DIR)
	mill -i $(PRJ).runMain Elaborate --target-dir $(BUILD_DIR)

help:
	mill -i $(PRJ).runMain Elaborate --help

reformat:
	mill -i __.reformat

checkformat:
	mill -i __.checkFormat

bsp:
	mill -i mill.bsp.BSP/install

idea:
	mill -i mill.idea.GenIdea/idea

clean:
	-rm -rf $(BUILD_DIR)

.PHONY: test verilog help reformat checkformat clean

sim:
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@echo "Write this Makefile by yourself."

# ==========================================
# JTAG Simulation Targets
# ==========================================
VERILATOR = verilator
CPP_SRCS = csrc/sim_main.cpp
GEN_DIR = ./generated
TOP_MODULE = TopMain

jtag-verilog:
	mkdir -p $(GEN_DIR)
	mill -i $(PRJ).runMain Elaborate --target-dir $(GEN_DIR)

jtag-sim: jtag-verilog
	mkdir -p $(BUILD_DIR)
	$(VERILATOR) --cc --exe --build -j 4 \
		--top-module $(TOP_MODULE) \
		-DASSERTS_OFF \
		-I$(GEN_DIR) \
		-Iplayground/resources/ibex \
		-Iplayground/resources/riscv-dbg \
		-Iip/ibex/vendor/lowrisc_ip/ip/prim/rtl \
		-Iip/ibex/vendor/lowrisc_ip/dv/sv/dv_utils \
		-Iip/common_cells/include \
		-Iip/tech_cells_generic/src/rtl \
		-Iip/tech_cells_generic/src/rtl \
		ip/ibex/vendor/lowrisc_ip/ip/prim/rtl/prim_util_pkg.sv \
		ip/ibex/vendor/lowrisc_ip/ip/prim/rtl/prim_mubi_pkg.sv \
		ip/ibex/vendor/lowrisc_ip/ip/prim/rtl/prim_cipher_pkg.sv \
		ip/ibex/vendor/lowrisc_ip/ip/prim/rtl/prim_count_pkg.sv \
		ip/ibex/vendor/lowrisc_ip/ip/prim/rtl/prim_ram_1p_pkg.sv \
		ip/ibex/vendor/lowrisc_ip/ip/prim/rtl/prim_secded_pkg.sv \
		ip/ibex/dv/uvm/core_ibex/common/prim/prim_pkg.sv \
		ip/ibex/dv/uvm/core_ibex/common/prim/prim_clock_gating.sv \
		ip/ibex/vendor/lowrisc_ip/ip/prim_generic/rtl/prim_generic_clock_gating.sv \
		ip/ibex/dv/uvm/core_ibex/common/prim/prim_buf.sv \
		ip/ibex/vendor/lowrisc_ip/ip/prim_generic/rtl/prim_generic_buf.sv \
		ip/tech_cells_generic/src/rtl/tc_clk.sv \
		ip/tech_cells_generic/src/rtl/tc_sram.sv \
		ip/common_cells/src/deprecated/fifo_v2.sv \
		ip/common_cells/src/fifo_v3.sv \
		ip/common_cells/src/cdc_2phase_clearable.sv \
		ip/common_cells/src/sync.sv \
		ip/common_cells/src/cdc_reset_ctrlr_pkg.sv \
		ip/common_cells/src/cdc_reset_ctrlr_pkg.sv \
		ip/common_cells/src/cdc_reset_ctrlr.sv \
		ip/common_cells/src/spill_register.sv \
		ip/common_cells/src/spill_register_flushable.sv \
		ip/common_cells/src/stream_register.sv \
		ip/common_cells/src/lzc.sv \
		ip/common_cells/src/rr_arb_tree.sv \
		ip/common_cells/src/popcount.sv \
		ip/common_cells/src/unread.sv \
		ip/common_cells/src/cf_math_pkg.sv \
		ip/tech_cells_generic/src/rtl/tc_sram.sv \
		ip/common_cells/src/cdc_4phase.sv \
		ip/common_cells/src/spill_register.sv \
		ip/common_cells/src/stream_register.sv \
		ip/riscv-dbg/debug_rom/debug_rom.sv \
		playground/resources/ibex/ibex_pkg.sv \
		playground/resources/riscv-dbg/dm_pkg.sv \
		playground/resources/ibex/ibex_top.sv \
		playground/resources/riscv-dbg/dm_csrs.sv \
		playground/resources/riscv-dbg/dm_mem.sv \
		playground/resources/riscv-dbg/dm_sba.sv \
		playground/resources/riscv-dbg/dm_obi_top.sv \
		playground/resources/riscv-dbg/dm_top.sv \
		playground/resources/riscv-dbg/dm_csrs.sv \
		playground/resources/riscv-dbg/dm_mem.sv \
		playground/resources/riscv-dbg/dm_sba.sv \
		playground/resources/riscv-dbg/dm_obi_top.sv \
		playground/resources/riscv-dbg/dmi_jtag.sv \
		playground/resources/riscv-dbg/dmi_jtag_tap.sv \
		playground/resources/riscv-dbg/dmi_cdc.sv \
		$(GEN_DIR)/$(TOP_MODULE).sv \
		$(CPP_SRCS) \
		--Mdir $(BUILD_DIR) \
		-o sim_top \
		-Wno-WIDTH -Wno-PINMISSING -Wno-IMPLICIT -Wno-MODDUP -Wno-UNOPTFLAT -Wno-REDEFMACRO -Wno-CMPCONST

run-jtag: jtag-sim
	$(BUILD_DIR)/sim_top

run-only-jtag:
	@echo "[Warn] Can only be used after `make jtag-sim`"
	$(BUILD_DIR)/sim_top

-include ../Makefile
