package src

import chisel3._
import chisel3.util._

class IbexTop extends BlackBox(Map(
  "DmBaseAddr" -> 0x1A110000,
  "DmAddrMask" -> 0x00000FFF,
  "DmHaltAddr" -> 0x1A110800,
  "DmExceptionAddr" -> 0x1A110808,
  "DbgTriggerEn" -> 1,
  "ICache" -> 0,
  "PMPEnable" -> 0
)) {
  override val desiredName = "ibex_top"
  val io = IO(new Bundle {
    val clk_i = Input(Clock())
    val rst_ni = Input(Bool())
    val test_en_i = Input(Bool())
    val ram_cfg_i = Input(UInt(10.W)) // ram_1p_cfg_t is struct, assuming default width or unused
    val hart_id_i = Input(UInt(32.W))
    val boot_addr_i = Input(UInt(32.W))

    val instr_req_o = Output(Bool())
    val instr_gnt_i = Input(Bool())
    val instr_rvalid_i = Input(Bool())
    val instr_addr_o = Output(UInt(32.W))
    val instr_rdata_i = Input(UInt(32.W))
    val instr_rdata_intg_i = Input(UInt(7.W))
    val instr_err_i = Input(Bool())

    val data_req_o = Output(Bool())
    val data_gnt_i = Input(Bool())
    val data_rvalid_i = Input(Bool())
    val data_we_o = Output(Bool())
    val data_be_o = Output(UInt(4.W))
    val data_addr_o = Output(UInt(32.W))
    val data_wdata_o = Output(UInt(32.W))
    val data_wdata_intg_o = Output(UInt(7.W))
    val data_rdata_i = Input(UInt(32.W))
    val data_rdata_intg_i = Input(UInt(7.W))
    val data_err_i = Input(Bool())

    val irq_software_i = Input(Bool())
    val irq_timer_i = Input(Bool())
    val irq_external_i = Input(Bool())
    val irq_fast_i = Input(UInt(15.W))
    val irq_nm_i = Input(Bool())

    val scramble_key_valid_i = Input(Bool())
    val scramble_key_i = Input(UInt(128.W))
    val scramble_nonce_i = Input(UInt(64.W))
    val scramble_req_o = Output(Bool())

    val debug_req_i = Input(Bool())
    val crash_dump_o = Output(UInt(160.W)) // crash_dump_t struct flattened
    val double_fault_seen_o = Output(Bool())

    val fetch_enable_i = Input(UInt(4.W)) // ibex_mubi_t
    val alert_minor_o = Output(Bool())
    val alert_major_internal_o = Output(Bool())
    val alert_major_bus_o = Output(Bool())
    val core_sleep_o = Output(Bool())
    val scan_rst_ni = Input(Bool())
  })
}
