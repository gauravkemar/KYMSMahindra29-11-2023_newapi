package com.kemarport.kymsmahindra.activity.newactivity

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.UUID

class BluetoothConnector (private val deviceAddress: String,context: Context) {
    var  context:Context
    init {
        this.context=context
    }

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // SPP UUID

    fun connect(): BluetoothSocket? {
        val device: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(deviceAddress)
        val socket: BluetoothSocket?
        Log.d("connection","connected successfully"+"  $device")

        try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return null
            }
            socket = device?.createRfcommSocketToServiceRecord(uuid)
            socket?.connect()
            return socket
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    fun disconnect(socket: BluetoothSocket?) {
        try {
            socket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}