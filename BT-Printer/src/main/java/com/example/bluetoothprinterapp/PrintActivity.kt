package com.example.bluetoothprinterapp

import androidx.appcompat.app.AppCompatActivity
import android.bluetooth.BluetoothAdapter
import android.app.ProgressDialog
import android.bluetooth.BluetoothSocket
import android.bluetooth.BluetoothDevice
import android.widget.EditText
import android.os.Bundle
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import java.io.IOException
import java.lang.Exception
import java.lang.StringBuilder
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * Created by hp on 12/23/2016.
 */
class PrintActivity : AppCompatActivity(), Runnable {
    var mScan: Button? = null
    var mPrint: Button? = null
    var mDisc: Button? = null
    var mBluetoothAdapter: BluetoothAdapter? = null
    private val applicationUUID = UUID
        .fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var mBluetoothConnectProgressDialog: ProgressDialog? = null
    private var mBluetoothSocket: BluetoothSocket? = null
    var mBluetoothDevice: BluetoothDevice? = null
    private var accountNoEditText: EditText? = null
    private var accountNameEditText: EditText? = null
    private var amountEditText: EditText? = null
    public override fun onCreate(mSavedInstanceState: Bundle?) {
        super.onCreate(mSavedInstanceState)
        setContentView(R.layout.activity_bt_print)
        accountNoEditText = findViewById<View>(R.id.accountNoEditText) as EditText
        accountNameEditText = findViewById<View>(R.id.accountNameEditText) as EditText
        amountEditText = findViewById<View>(R.id.amountEditText) as EditText
        mScan = findViewById<View>(R.id.Scan) as Button
        mScan!!.setOnClickListener(scanClickListener)
        mPrint = findViewById<View>(R.id.mPrint) as Button
        mPrint!!.setOnClickListener(printClickListener)
        mDisc = findViewById<View>(R.id.dis) as Button
        mDisc!!.setOnClickListener(disableClickListener)
    }

    private val message: String
        private get() {
            var BILL = ""
            BILL = """                   XXXX MART    
                   XX.AA.BB.CC.     
                  NO 25 ABC ABCDE    
                  XXXXX YYYYYY      
                   MMM 590019091      
"""
            BILL = """
        $BILL-----------------------------------------------
        
        """.trimIndent()
            BILL =
                BILL + String.format("%1$-10s %2$10s %3$13s %4$10s", "Item", "Qty", "Rate", "Totel")
            BILL = """
        $BILL
        
        """.trimIndent()
            BILL = (BILL
                    + "-----------------------------------------------")
            BILL = """$BILL
 ${String.format("%1$-10s %2$10s %3$11s %4$10s", "item-001", "5", "10", "50.00")}"""
            BILL = """$BILL
 ${String.format("%1$-10s %2$10s %3$11s %4$10s", "item-002", "10", "5", "50.00")}"""
            BILL = """$BILL
 ${String.format("%1$-10s %2$10s %3$11s %4$10s", "item-003", "20", "10", "200.00")}"""
            BILL = """$BILL
 ${String.format("%1$-10s %2$10s %3$11s %4$10s", "item-004", "50", "10", "500.00")}"""
            BILL = """
        $BILL
        -----------------------------------------------
        """.trimIndent()
            BILL = "$BILL\n\n "
            BILL = "$BILL                   Total Qty:      85\n"
            BILL = "$BILL                   Total Value:     700.00\n"
            BILL = """
        $BILL-----------------------------------------------
        
        """.trimIndent()
            BILL = "$BILL\n\n "
            return BILL
        }

    override fun onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy()
        try {
            if (mBluetoothSocket != null) mBluetoothSocket!!.close()
        } catch (e: Exception) {
            Log.e("Tag", "Exe ", e)
        }
    }

    override fun onBackPressed() {
        try {
            if (mBluetoothSocket != null) mBluetoothSocket!!.close()
        } catch (e: Exception) {
            Log.e("Tag", "Exe ", e)
        }
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CONNECT_DEVICE -> if (resultCode == RESULT_OK) {
                val mDeviceAddress = data?.extras?.getString("DeviceAddress")
                Log.v(TAG, "Coming incoming address $mDeviceAddress")
                mBluetoothDevice = mBluetoothAdapter
                    ?.getRemoteDevice(mDeviceAddress)
                mBluetoothConnectProgressDialog = ProgressDialog.show(this,
                    "Connecting...", mBluetoothDevice?.getName() + " : "
                            + mBluetoothDevice?.getAddress(), true, false)
                val mBlutoothConnectThread = Thread(this)
                mBlutoothConnectThread.start()
                // pairToDevice(mBluetoothDevice); This method is replaced by
                // progress dialog with thread
            }
            REQUEST_ENABLE_BT -> if (resultCode == RESULT_OK) {
                ListPairedDevices()
                val connectIntent = Intent(this@PrintActivity,
                    BTDeviceListActivity::class.java)
                startActivityForResult(connectIntent, REQUEST_CONNECT_DEVICE)
            } else {
                Toast.makeText(this@PrintActivity, "Message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun ListPairedDevices() {
        val mPairedDevices = mBluetoothAdapter
            ?.getBondedDevices()
        if (mPairedDevices?.size!! > 0) {
            for (mDevice in mPairedDevices) {
                Log.v(TAG, "PairedDevices: " + mDevice.name + "  "
                        + mDevice.address)
            }
        }
    }

    override fun run() {
        try {
            mBluetoothSocket = mBluetoothDevice
                ?.createRfcommSocketToServiceRecord(applicationUUID)
            mBluetoothAdapter?.cancelDiscovery()
            mBluetoothSocket?.connect()
            mHandler.sendEmptyMessage(0)
        } catch (eConnectException: IOException) {
            Log.d(TAG, "CouldNotConnectToSocket", eConnectException)
            closeSocket(mBluetoothSocket)
            return
        }
    }

    private fun closeSocket(nOpenSocket: BluetoothSocket?) {
        try {
            nOpenSocket!!.close()
            Log.d(TAG, "SocketClosed")
        } catch (ex: IOException) {
            Log.d(TAG, "CouldNotCloseSocket")
        }
    }

    private var printClick = false
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            mBluetoothConnectProgressDialog!!.dismiss()
            Toast.makeText(this@PrintActivity, "DeviceConnected", Toast.LENGTH_SHORT).show()
            if (printClick) {
                printToBT()
            } else {
                printClick = false
            }
        }
    }

    fun sel(`val`: Int): ByteArray {
        val buffer = ByteBuffer.allocate(2)
        buffer.putInt(`val`)
        buffer.flip()
        return buffer.array()
    }

    private val scanClickListener = View.OnClickListener {
        printClick = false
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter == null) {
            Toast.makeText(this@PrintActivity, "Message1", Toast.LENGTH_SHORT).show()
        } else {
            if (!mBluetoothAdapter!!.isEnabled) {
                val enableBtIntent = Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent,
                    REQUEST_ENABLE_BT)
            } else {
                ListPairedDevices()
                val connectIntent = Intent(this@PrintActivity,
                    BTDeviceListActivity::class.java)
                startActivityForResult(connectIntent,
                    REQUEST_CONNECT_DEVICE)
            }
        }
    }
    private val printClickListener = View.OnClickListener {
        printToBT()
        printClick = true
    }

    private fun printToBT() {
        if (mBluetoothSocket == null) {
            scanClickListener.onClick(null)
            return
        }
        val t: Thread = object : Thread() {
            override fun run() {
                try {
                    val os = mBluetoothSocket!!
                        .outputStream
                    val message = getCollectionMessage(accountNoEditText!!.text.toString(),
                        accountNameEditText!!.text.toString(),
                        amountEditText!!.text.toString().toDouble(),
                        "Hasan")
                    os.write(message.toByteArray(StandardCharsets.UTF_8))
                    //This is printer specific code you can comment ==== > Start

                    // Setting height
                    val gs = 29
                    os.write(intToByteArray(gs).toInt())
                    val h = 104
                    os.write(intToByteArray(h).toInt())
                    val n = 162
                    os.write(intToByteArray(n).toInt())

                    // Setting Width
                    val gs_width = 29
                    os.write(intToByteArray(gs_width).toInt())
                    val w = 119
                    os.write(intToByteArray(w).toInt())
                    val n_width = 2
                    os.write(intToByteArray(n_width).toInt())
                } catch (e: Exception) {
                    Log.e("PrintActivity", "Exe ", e)
                }
            }
        }
        t.start()
    }

    private val disableClickListener =
        View.OnClickListener { if (mBluetoothAdapter != null) mBluetoothAdapter!!.disable() }

    private fun getCollectionMessage(
        accountNo: String,
        accountName: String,
        amount: Double,
        printedBy: String
    ): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(String.format("** Alor Feri Pathagar **\n", amount))
        stringBuilder.append(String.format("           Member Reg.           \n", amount))
        stringBuilder.append(String.format("................................\n", amount))
        stringBuilder.append(String.format("Deposited amount: %.2f\n", amount))
        stringBuilder.append(String.format("Account No: %s\n", accountNo))
        stringBuilder.append(String.format("Account Name: %s\n", accountName))
        stringBuilder.append("\n")
        stringBuilder.append(String.format("................................\n", amount))
        stringBuilder.append(String.format("Authorized by: %s\n", printedBy))
        stringBuilder.append("\n\n")
        return stringBuilder.toString()
    }

    companion object {
        protected const val TAG = "TAG"
        private const val REQUEST_CONNECT_DEVICE = 1
        private const val REQUEST_ENABLE_BT = 2
        fun intToByteArray(value: Int): Byte {
            val b = ByteBuffer.allocate(4).putInt(value).array()
            for (k in b.indices) {
                println("Selva  [" + k + "] = " + "0x"
                        + UnicodeFormatter.byteToHex(b[k]))
            }
            return b[3]
        }
    }
}