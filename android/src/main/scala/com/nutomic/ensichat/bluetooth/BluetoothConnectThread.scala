package com.nutomic.ensichat.bluetooth

import java.io.IOException

import android.bluetooth.BluetoothSocket
import android.util.Log

/**
 * Attempts to connect to another device and calls [[onConnected]] on success.
 */
class BluetoothConnectThread(device: Device, onConnected: (Device, BluetoothSocket) => Unit) extends Thread {

  private val Tag = "ConnectThread"

  private val socket = try {
    Option(device.btDevice.get.createInsecureRfcommSocketToServiceRecord(BluetoothInterface.AppUuid))
  } catch {
    case e: IOException =>
      Log.w(Tag, "Failed to open Bluetooth connection", e)
      None
  }

  override def run(): Unit = {
    if (socket.isEmpty)
      return

    Log.i(Tag, "Connecting to " + device.toString)
    try {
      socket.get.connect()
    } catch {
      case e: IOException =>
        Log.v(Tag, "Failed to connect to " + device.toString, e)
        try {
          socket.get.close()
        } catch {
          case e2: IOException =>
            Log.e(Tag, "Failed to close socket", e2)
        }
        return
    }

    Log.i(Tag, "Successfully connected to device " + device.name)
    onConnected(new Device(device.btDevice.get, true), socket.get)
  }

}
