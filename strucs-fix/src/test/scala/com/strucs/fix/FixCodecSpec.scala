package com.strucs.fix

import com.strucs.fix.dict.fix42._
import org.joda.time.{DateTimeZone, DateTime}
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{FlatSpec, Matchers}
import org.strucs.{ComposeCodec, Struct}
import FixCodec._
import FixGroup.SOH

import scala.util.Success


/**
 * More examples at http://fiximulator.org/FIXimulator_Thesis.pdf
 */
class FixCodecSpec extends FlatSpec with Matchers with TypeCheckedTripleEquals {

  /** It's easier to use \n or ; for separating key/value pairs in testcases. */
  private implicit class SemicolonToSOH(s: String) {
    def toSOH: String = s.stripMargin.replaceAll("[;\n]", SOH)
  }

  "a FixCodec" should "encode a New Order Single" in {
    val struct = Struct.empty +
      // TODO BeginString("FIX.4.2") +
      // TODO MsgType("D") +
      MsgSeqNum("4") + // TODO remove ?: protocol field
      SenderCompID("ABC_DEFG01") +
      SendingTime(new DateTime(2009,3,23,15,40,29, DateTimeZone.UTC)) +
      TargetCompID("CCG") +
      OnBehalfOfCompID("XYZ") +
      ClOrdId("NF 0542/03232009") +
      Side("1") + // TODO enum
      OrderQty("100") + // TODO BigDecimal
      Symbol("CVS") +
      OrdType("1") + // TODO enum
      TimeInForce("0") +
      Rule80A("A") + // TODO enum
      TransactTime(new DateTime(2009,3,23,15,40,29, DateTimeZone.UTC)) +
      HandlInst("1") + // TODO enum
      SecurityExchange("N")

    // TODO auto creation does not work :-(
    implicit val codec = ComposeCodec.makeCodec[FixCodec, struct.tpe]
    //val codec = implicitly[FixCodec[Struct[struct.tpe]]]
    //val actualFix = Success(codec.encode(struct).toFixString)
    val actualFix = struct.toFixMessageString
    //val actualFix = struct.toFixMessageString


    // Is the NYSE message bogus ?? 9 should be 146, and checksum does not look right => try with other sample
    val expectedFix =
      """8=FIX.4.2
        |9=146
        |35=D
        |34=4
        |49=ABC_DEFG01
        |52=20090323-15:40:29
        |56=CCG
        |115=XYZ
        |11=NF 0542/03232009
        |54=1
        |38=100
        |55=CVS
        |40=1
        |59=0
        |47=A
        |60=20090323-15:40:29
        |21=1
        |207=N
        |10=195""".toSOH


    actualFix should be (expectedFix)
  }


  val fixMsgString = "8=FIX.4.2;9=65;35=A;49=SERVER;56=CLIENT;34=177;52=20090107-18:15:16;98=0;108=30;10=062;".toSOH

  "A FixMessage" should "encode with length and checksum" in {
    // Example from https://en.wikipedia.org/wiki/Financial_Information_eXchange#Body_length
    val msg = FixMessage("FIX.4.2", "A", FixGroup(49 -> "SERVER", 56 -> "CLIENT", 34 -> "177", 52 -> "20090107-18:15:16", 98 -> "0", 108 -> "30"))
    msg.toFixString should be (fixMsgString)
  }

  "A FixMessage" should "decode" in {
    FixMessage.decode(fixMsgString) should ===(Success(FixMessage("FIX.4.2", "A", FixGroup(49 -> "SERVER", 56 -> "CLIENT", 34 -> "177", 52 -> "20090107-18:15:16", 98 -> "0", 108 -> "30"))))
  }

  "A FixMessage" should "keep the same fix string after decode and encode" in {
    val fixStr = "8=FIX.4.2;9=137;35=D;34=8;49=BANZAI;52=20081005-14:35:46.672;56=FIXIMULATOR;11=1223217346597;21=1;38=5000;40=1;54=2;55=IBM;59=0;60=20081005-14:35:46.668;10=153;".toSOH
    FixMessage.decode(fixStr) map (_.toFixString) should === (Success(fixStr))
  }


}


