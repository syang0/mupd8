/**
 * Copyright 2011-2012 @WalmartLabs, a division of Wal-Mart Stores, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.walmartlabs.mupd8

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.io.File
// does not exist until ScalaTest 1.6.1 (for Scala 2.9)
// import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.ShouldMatchers

import scala.util.Random

@RunWith(classOf[JUnitRunner])
class HashRingTest extends FunSuite with ShouldMatchers {

  val random = new Random(java.lang.System.currentTimeMillis)

  test("test hashring2") {
    val hostList1 = Vector.range(0, 10) map (i => Host("192.168.1." + i.toString, "host" + i.toString))
    val hostList2 = hostList1 :+ Host("192.168.1.100","host100")
    var ring2 = HashRing2.initFromHosts(hostList1)
    assert(ring2.stat(hostList1.map(_.ip), 0.05))
    for (i <- 1 to 5000) {
      ring2 = ring2.add(hostList2.map(_.ip), Host("192.168.1.100","host100"))
      assert(ring2.stat(hostList2.map(_.ip), 0.05))
      ring2 = ring2.remove(hostList1.map(_.ip), "192.168.1.100")
      assert(ring2.stat(hostList1.map(_.ip), 0.05))
    }
  }

  test("hashring2 serialization") {
    val hostList = Vector.range(0, 10) map (i => Host("192.168.1." + i.toString, "host" + i.toString + ".example.com"))
    val ring = HashRing2.initFromHosts(hostList)
    val fileOut = new FileOutputStream("hashring.ser");
    val out = new ObjectOutputStream(fileOut);
    out.writeObject(ring)
    out.close()
    fileOut.close()

    val fileIn = new FileInputStream("hashring.ser");
    val in = new ObjectInputStream(fileIn);
    val ring2 = in.readObject();
    in.close();
    fileIn.close();
    assert(ring2 == ring)
    new File("hashring.ser").delete

    val hostList2 = hostList :+ Host("192.168.1.100", "host100")
    val ring3 = HashRing2.initFromHosts(hostList2)
    new ObjectOutputStream(new FileOutputStream("hashring3.ser")).writeObject(ring3);

    val ring4 = new ObjectInputStream(new FileInputStream("hashring3.ser")).readObject
    assert(ring4 != ring)
    new File("hashring3.ser").delete
  }

  test("hashring") {
    val hostList = Vector.range(0, 10) map (i => Host("192.168.1." + i.toString, "host" + i.toString + ".example.com"))
    val r2 = HashRing2.initFromHosts(hostList)
    val ring = new HashRing(r2.hash)
  }

}
