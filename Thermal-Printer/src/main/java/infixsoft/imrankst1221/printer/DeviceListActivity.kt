package infixsoft.imrankst1221.printer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import infixsoft.imrankst1221.printer.R
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView
import android.content.IntentFilter
import android.bluetooth.BluetoothDevice
import infixsoft.imrankst1221.printer.DeviceListActivity
import android.bluetooth.BluetoothAdapter
import android.widget.Toast
import android.widget.ArrayAdapter
import android.content.Intent
import android.app.Activity
import android.content.BroadcastReceiver
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import java.io.IOException
import java.lang.Exception
import java.util.*

/**
 * Created by imrankst1221@gmail.com
 */
class DeviceListActivity : AppCompatActivity() {
    var mPairedListView: ListView? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)
        title = "Bluetooth Devices"
        mPairedListView = findViewById<View>(R.id.paired_devices) as ListView
        mPairedListView!!.onItemClickListener =
            OnItemClickListener { adapterView, view, i, l -> onListItemClick(i, l) }
        try {
            if (initDevicesList() != 0) {
                finish()
            }
        } catch (ex: Exception) {
            finish()
        }
        proceedDiscovery()
    }

    protected fun proceedDiscovery() {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED)
        registerReceiver(mBTReceiver, filter)
        mBluetoothAdapter!!.startDiscovery()
    }

    private fun flushData() {
        try {
            if (socket != null) {
                socket!!.close()
                socket = null
            }
            if (mBluetoothAdapter != null) {
                mBluetoothAdapter!!.cancelDiscovery()
            }
            if (btDevices != null) {
                btDevices!!.clear()
                btDevices = null
            }
            if (mArrayAdapter != null) {
                mArrayAdapter!!.clear()
                mArrayAdapter!!.notifyDataSetChanged()
                mArrayAdapter!!.notifyDataSetInvalidated()
                mArrayAdapter = null
            }

            //finalize();
        } catch (ex: Exception) {
            Log.e(TAG, ex.message!!)
        }
    }

    private fun initDevicesList(): Int {
        flushData()
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter == null) {
            Toast.makeText(applicationContext,
                "Bluetooth not supported!!", Toast.LENGTH_LONG).show()
            return -1
        }
        if (mBluetoothAdapter!!.isDiscovering) {
            mBluetoothAdapter!!.cancelDiscovery()
        }
        mArrayAdapter = ArrayAdapter(applicationContext,
            R.layout.layout_list)

//        setListAdapter(mArrayAdapter);
        mPairedListView!!.adapter = mArrayAdapter
        val enableBtIntent = Intent(
            BluetoothAdapter.ACTION_REQUEST_ENABLE)
        try {
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } catch (ex: Exception) {
            return -2
        }
        Toast.makeText(applicationContext,
            "Getting all available Bluetooth Devices", Toast.LENGTH_SHORT)
            .show()
        return 0
    }

    override fun onActivityResult(reqCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(reqCode, resultCode, intent)
        when (reqCode) {
            REQUEST_ENABLE_BT -> if (resultCode == RESULT_OK) {
                val btDeviceList = mBluetoothAdapter
                    ?.getBondedDevices()
                try {
                    if (btDeviceList?.size!! > 0) {
                        if (btDevices == null) {
                            btDevices = ArrayAdapter(
                                applicationContext, R.layout.layout_list)
                        }
                        for (device in btDeviceList) {
//                                if (btDeviceList.contains(device) == false) {
                            btDevices!!.add(device)
                            mArrayAdapter!!.add("""
    ${device.name}
    ${device.address}
    """.trimIndent())
                            mArrayAdapter!!.notifyDataSetInvalidated()
                            //                                }
                        }
                    }
                } catch (ex: Exception) {
                    Log.e(TAG, ex.message!!)
                }
            }
        }
        mBluetoothAdapter!!.startDiscovery()
    }

    private val mBTReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device = intent
                    .getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                try {
                    if (btDevices == null) {
                        btDevices = ArrayAdapter(
                            applicationContext, R.layout.layout_list)
                    }
                    if (btDevices!!.getPosition(device) < 0) {
                        btDevices!!.add(device)
                        mArrayAdapter!!.add("""
    ${device!!.name}
    ${device.address}
    
    """.trimIndent())
                        mArrayAdapter!!.notifyDataSetInvalidated()
                    }
                } catch (ex: Exception) {
                    ex.fillInStackTrace()
                }
            }
        }
    }

    protected fun onListItemClick(
        position: Int,
        id: Long
    ) {
        if (mBluetoothAdapter == null) {
            return
        }
        if (mBluetoothAdapter!!.isDiscovering) {
            mBluetoothAdapter!!.cancelDiscovery()
        }
        Toast.makeText(
            applicationContext,
            "Connecting to " + btDevices!!.getItem(position)!!.name + ","
                    + btDevices!!.getItem(position)!!.address,
            Toast.LENGTH_SHORT).show()
        val connectThread = Thread {
            try {
                val gotuuid = btDevices!!.getItem(position)
                    ?.fetchUuidsWithSdp()
                val uuid = btDevices!!.getItem(position)!!
                    .uuids[0]
                    .uuid
                socket = btDevices!!.getItem(position)
                    ?.createRfcommSocketToServiceRecord(uuid)
                socket?.connect()
            } catch (ex: IOException) {
                runOnUiThread(socketErrorRunnable)
                try {
                    socket!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                socket = null
            } finally {
                runOnUiThread { finish() }
            }
        }
        connectThread.start()
    }

    private val socketErrorRunnable = Runnable {
        Toast.makeText(applicationContext,
            "Cannot establish connection", Toast.LENGTH_SHORT).show()
        mBluetoothAdapter!!.startDiscovery()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menu.add(0, Menu.FIRST, Menu.NONE, "Refresh Scanning")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.itemId) {
            Menu.FIRST -> initDevicesList()
        }
        return true
    }

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(mBTReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val TAG = "---DeviceList"
        const val REQUEST_CONNECT_BT = 0 * 2300
        private const val REQUEST_ENABLE_BT = 0 * 1000
        private var mBluetoothAdapter: BluetoothAdapter? = null
        private var mArrayAdapter: ArrayAdapter<String>? = null
        private var btDevices: ArrayAdapter<BluetoothDevice>? = null
        private val SPP_UUID = UUID
            .fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")

        // UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        var socket: BluetoothSocket? = null
            private set
    }
}