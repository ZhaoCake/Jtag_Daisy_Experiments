package src

import chisel3._
import chisel3.util._

class DmTop extends BlackBox(Map(
  "NrHarts" -> 1,
  "BusWidth" -> 32,
  "DmBaseAddress" -> 0x1A110000,
  "SelectableHarts" -> 1,
  "ReadByteEnable" -> 1
)) {
  override val desiredName = "dm_top"
  val io = IO(new Bundle {
    val clk_i = Input(Clock())
    val rst_ni = Input(Bool())
    val next_dm_addr_i = Input(UInt(32.W))
    val testmode_i = Input(Bool())
    val ndmreset_o = Output(Bool())
    val ndmreset_ack_i = Input(Bool())
    val dmactive_o = Output(Bool())
    val debug_req_o = Output(UInt(1.W))
    val unavailable_i = Input(UInt(1.W))
    val hartinfo_i = Input(UInt(32.W)) // hartinfo_t

    val slave_req_i = Input(Bool())
    val slave_we_i = Input(Bool())
    val slave_addr_i = Input(UInt(32.W))
    val slave_be_i = Input(UInt(4.W))
    val slave_wdata_i = Input(UInt(32.W))
    val slave_rdata_o = Output(UInt(32.W))

    val master_req_o = Output(Bool())
    val master_add_o = Output(UInt(32.W))
    val master_we_o = Output(Bool())
    val master_wdata_o = Output(UInt(32.W))
    val master_be_o = Output(UInt(4.W))
    val master_gnt_i = Input(Bool())
    val master_r_valid_i = Input(Bool())
    val master_r_err_i = Input(Bool())
    val master_r_other_err_i = Input(Bool())
    val master_r_rdata_i = Input(UInt(32.W))

    val dmi_rst_ni = Input(Bool())
    val dmi_req_valid_i = Input(Bool())
    val dmi_req_ready_o = Output(Bool())
    val dmi_req_i = Input(UInt(41.W)) // dmi_req_t

    val dmi_resp_valid_o = Output(Bool())
    val dmi_resp_ready_i = Input(Bool())
    val dmi_resp_o = Output(UInt(34.W)) // dmi_resp_t
  })
}
