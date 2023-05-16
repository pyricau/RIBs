package com.uber.rib.core

import shark.ObjectInspector
import shark.ObjectReporter
import shark.HeapObject.HeapInstance

public class RouterInspector : ObjectInspector {
  override fun inspect(reporter: ObjectReporter) {
    val heapObject = reporter.heapObject
    if (heapObject is HeapInstance && heapObject instanceOf "com.uber.rib.core.Router") {
      val interactor = heapObject["com.uber.rib.core.Router", "interactor"]!!.valueAsInstance!!
      if (interactor instanceOf "com.uber.rib.core.Interactor") {
        val lifecycleFlow =
          interactor["com.uber.rib.core.Interactor", "_lifecycleFlow"]!!.valueAsInstance!!
        // lifecycleFlow is always a MutableSharedFlow
        val buffer =
          lifecycleFlow["kotlinx.coroutines.flow.SharedFlowImpl", "buffer"]!!.valueAsObjectArray!!.readElements().toList()
        val replayIndex = lifecycleFlow["kotlinx.coroutines.flow.SharedFlowImpl", "replayIndex"]!!.value.asLong!!.toInt()
        val interactorEvent = buffer[replayIndex].asObject?.asInstance!!
        val enumName =
          interactorEvent["java.lang.Enum", "name"]!!.valueAsInstance!!.readAsJavaString()
        if (enumName == "INACTIVE") {
          reporter.leakingReasons += "interactor._lifecycleFlow is INACTIVE"
        } else {
          reporter.notLeakingReasons += "interactor._lifecycleFlow is ACTIVE"
        }
      }
    }
  }
}