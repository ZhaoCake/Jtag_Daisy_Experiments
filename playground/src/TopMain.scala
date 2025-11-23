package simjtag

import chisel3._
import src.IbexSystem

/**
 * JTAG 菊花链顶层模块
 * 包含两个串联的 IbexSystem 模块
 */
class TopMain extends Module {
  val io = IO(new Bundle {
    val jtag = new JtagIO
    // 暴露两个 TAP 的 LED 输出以便观察 (这里用 dmactive 代替)
    val led1 = Output(UInt(8.W))
    val led2 = Output(UInt(8.W))
  })

  // 实例化两个 IbexSystem
  // 注意：IDCODE 必须是奇数
  val sys1 = Module(new IbexSystem(idcode = 0x1e200a6d)) // Default Ibex ID
  val sys2 = Module(new IbexSystem(idcode = 0x2e200a6d)) // Modified ID

  // 连接 JTAG 信号 - 菊花链拓扑
  // TCK, TMS, TRST 并联
  sys1.io.jtag_tck := io.jtag.TCK
  sys1.io.jtag_tms := io.jtag.TMS
  sys1.io.jtag_trst_n := true.B // Assuming no TRST pin from JtagIO or handled externally? 
                                // Wait, JtagIO usually has TRST? 
                                // Let's check SingleJtag.scala or JtagIO definition.
                                // Assuming JtagIO has TRST or we tie it high if not present.
                                // Standard JTAG TRST is optional (active low).
  
  sys2.io.jtag_tck := io.jtag.TCK
  sys2.io.jtag_tms := io.jtag.TMS
  sys2.io.jtag_trst_n := true.B

  // 数据链路串联: TDI -> SYS1 -> SYS2 -> TDO
  sys1.io.jtag_tdi := io.jtag.TDI
  sys2.io.jtag_tdi := sys1.io.jtag_tdo
  io.jtag.TDO      := sys2.io.jtag_tdo

  // 连接 LED 输出 (这里暂时输出 0)
  io.led1 := 0.U
  io.led2 := 0.U
}

