package src

import chisel3._
import chisel3.util._

class DmiJtag(idcode: Int) extends BlackBox(Map(
  "IdcodeValue" -> idcode
)) {
  override val desiredName = "dmi_jtag"
  val io = IO(new Bundle {
    val clk_i = Input(Clock())
    val rst_ni = Input(Bool())
    val testmode_i = Input(Bool())

    val dmi_rst_no = Output(Bool())
    val dmi_req_o = Output(UInt(41.W)) // dmi_req_t
    val dmi_req_valid_o = Output(Bool())
    val dmi_req_ready_i = Input(Bool())

    val dmi_resp_i = Input(UInt(34.W)) // dmi_resp_t
    val dmi_resp_ready_o = Output(Bool())
    val dmi_resp_valid_i = Input(Bool())

    val tck_i = Input(Clock())
    val tms_i = Input(Bool())
    val trst_ni = Input(Bool())
    val td_i = Input(Bool())
    val td_o = Output(Bool())
    val tdo_oe_o = Output(Bool())
  })
}
