package infixsoft.imrankst1221.printer

import infixsoft.imrankst1221.printer.Utils.decodeBitmap
import infixsoft.imrankst1221.printer.DeviceListActivity.Companion.socket
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.graphics.Bitmap
import android.os.Bundle
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import android.content.Intent
import android.bluetooth.BluetoothSocket
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.squareup.picasso.Target
import java.io.IOException
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.lang.Exception
import java.util.*

/**
 * Created by https://goo.gl/UAfmBd on 2/6/2017.
 */
class MainActivity : AppCompatActivity() {
    private val TAG = "Main Activity"
    var txtMessage: EditText? = null
    var btnPrint: Button? = null
    var btnBill: Button? = null
    var imageView: ImageView? = null
    var FONT_TYPE: Byte = 0
    private var mBitmap: Bitmap? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        txtMessage = findViewById<View>(R.id.txtMessage) as EditText
        btnPrint = findViewById<View>(R.id.btnPrint) as Button
        btnBill = findViewById<View>(R.id.btnBill) as Button
        imageView = findViewById<View>(R.id.imageView) as ImageView
        btnPrint!!.setOnClickListener { printDemo() }
        btnBill!!.setOnClickListener { printBill() }
        Picasso.get()
            .load("https://backoffice.alorferi.com/images/defaults/logo_large.png")
            .placeholder(R.mipmap.ic_launcher)
            .into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
                    mBitmap = bitmap
                    imageView?.setImageBitmap(bitmap)
                }

                override fun onBitmapFailed(e: Exception, errorDrawable: Drawable) {}
                override fun onPrepareLoad(placeHolderDrawable: Drawable) {}
            })
    }

    protected fun printBill() {
        if (btsocket == null) {
            val BTIntent = Intent(applicationContext, DeviceListActivity::class.java)
            this.startActivityForResult(BTIntent, DeviceListActivity.REQUEST_CONNECT_BT)
        } else {
            var opstream: OutputStream? = null
            try {
                opstream = btsocket!!.outputStream
            } catch (e: IOException) {
                e.printStackTrace()
            }
            outputStream = opstream

            //print command
            try {
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                outputStream = btsocket!!.outputStream
                val printformat = byteArrayOf(0x1B, 0x21, 0x03)
                outputStream?.run {
                    write(printformat)
                }
                printCustom("Fair Group BD", 2, 1)
                printCustom("Pepperoni Foods Ltd.", 0, 1)
                printPhoto(R.drawable.img)
                printCustom("H-123, R-123, Dhanmondi, Dhaka-1212", 0, 1)
                printCustom("Hot Line: +88000 000000", 0, 1)
                printCustom("Vat Reg : 0000000000,Mushak : 11", 0, 1)
                val dateTime = dateTime
                printText(leftRightAlign(dateTime[0], dateTime[1]))
                printText(leftRightAlign("Qty: Name", "Price "))
                printCustom(String(CharArray(32)).replace("\u0000", "."), 0, 1)
                printText(leftRightAlign("Total", "2,0000/="))
                printNewLine()
                printCustom("Thank you for coming & we look", 0, 1)
                printCustom("forward to serve you again", 0, 1)
                printNewLine()
                printNewLine()
                outputStream?.run {
                    printText(leftRightAlign(dateTime[0], dateTime[1]))
                    printText(leftRightAlign("Qty: Name", "Price "))
                    printCustom(String(CharArray(32)).replace("\u0000", "."), 0, 1)
                    printText(leftRightAlign("Total", "2,0000/="))
                    printNewLine()
                    printCustom("Thank you for coming & we look", 0, 1)
                    printCustom("forward to serve you again", 0, 1)
                    printNewLine()
                    printNewLine()
                    flush()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun printDemo() {
        if (btsocket == null) {
            val btIntent = Intent(applicationContext, DeviceListActivity::class.java)
            this.startActivityForResult(btIntent, DeviceListActivity.REQUEST_CONNECT_BT)
        } else {
            var opstream: OutputStream? = null
            try {
                opstream = btsocket!!.outputStream
            } catch (e: IOException) {
                e.printStackTrace()
            }
            outputStream = opstream

            //print command
            try {
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                outputStream = btsocket!!.outputStream
                val printformat = byteArrayOf(0x1B, (0 * 21).toByte(), FONT_TYPE)

                outputStream?.run {
                    this.run {
                        write(printformat)
                    }

                    //print title
                    printLineNumberSymbol()
                    printCustom("Alor Feri Pathagar", 1, 1)
                    //                printLineEqualSymbol()
                    printLineMinusSymbol()

                    //print normal text
                    printCustom(txtMessage!!.text.toString(), 0, 0)
                    //                printPhoto(R.drawable.img);
                    printNewLine()
                    printText("     >>>>   Thank you  <<<<     ") // total 32 char in a single line
                    //resetPrint(); //reset printer
                    printLineNumberSymbol()
                    printNewLine()
                    printNewLine()
                    flush()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    //print custom
    private fun printCustom(msg: String, size: Int, align: Int) {
        //Print config "mode"
        val cc = byteArrayOf(0x1B, 0x21, 0x03) // 0- normal size text
        //byte[] cc1 = new byte[]{0x1B,0x21,0x00};  // 0- normal size text
        val bb = byteArrayOf(0x1B, 0x21, 0x08) // 1- only bold text
        val bb2 = byteArrayOf(0x1B, 0x21, 0x20) // 2- bold with medium text
        val bb3 = byteArrayOf(0x1B, 0x21, 0x10) // 3- bold with large text
        try {
            when (size) {
                0 -> outputStream!!.write(cc)
                1 -> outputStream!!.write(bb)
                2 -> outputStream!!.write(bb2)
                3 -> outputStream!!.write(bb3)
            }
            when (align) {
                0 ->                     //left align
                    outputStream!!.write(PrinterCommands.ESC_ALIGN_LEFT)
                1 ->                     //center align
                    outputStream!!.write(PrinterCommands.ESC_ALIGN_CENTER)
                2 ->                     //right align
                    outputStream!!.write(PrinterCommands.ESC_ALIGN_RIGHT)
            }
            outputStream!!.write(msg.toByteArray())
            outputStream!!.write(PrinterCommands.LF.toInt())
            //outputStream.write(cc);
            //printNewLine();
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //print photo
    fun printPhoto(img: Int) {
        try {

//            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();

//            imageView.buildDrawingCache();
            val bmp = mBitmap
            //            Bitmap bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
//            Bitmap bmp = imageView.getDrawingCache();

//            Bitmap bmp = Bitmap.createScaledBitmap(mBitmap, 72, 72, true);

//            Bitmap bmp = BitmapFactory.decodeResource(getResources(), img);
            if (bmp != null) {
                val command = decodeBitmap(bmp)
                outputStream!!.write(PrinterCommands.ESC_ALIGN_CENTER)
                printText(command)
            } else {
                Log.e("Print Photo error", "the file isn't exists")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("PrintTools", "the file isn't exists")
        }
    }

    //print Line NumberSign
    private fun printLineNumberSymbol() {
        try {
            outputStream?.write(PrinterCommands.ESC_ALIGN_CENTER)
            printText(Utils.NUMBER_SYMBOL_TEXT)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun printLineMinusSymbol() {
        try {
            outputStream?.write(PrinterCommands.ESC_ALIGN_CENTER)
            printText(Utils.MINUS_SYMBOL_TEXT)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    private fun printLineEqualSymbol() {
        try {
            outputStream?.write(PrinterCommands.ESC_ALIGN_CENTER)
            printText(Utils.EQUAL_SYMBOL_TEXT)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //print new line
    private fun printNewLine() {
        try {
            outputStream!!.write(PrinterCommands.FEED_LINE)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //print text
    private fun printText(msg: String) {
        try {
            // Print normal text
            outputStream!!.write(msg.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //print byte[]
    private fun printText(msg: ByteArray?) {
        try {
            // Print normal text
            outputStream!!.write(msg)
            printNewLine()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun leftRightAlign(str1: String?, str2: String?): String {
        var ans = str1 + str2
        if (ans.length < 31) {
            val n = 31 - str1!!.length + str2!!.length
            ans = str1 + String(CharArray(n)).replace("\u0000", " ") + str2
        }
        return ans
    }

    private val dateTime: Array<String?>
        private get() {
            val c = Calendar.getInstance()
            val dateTime = arrayOfNulls<String>(2)
            dateTime[0] =
                c[Calendar.DAY_OF_MONTH].toString() + "/" + c[Calendar.MONTH] + "/" + c[Calendar.YEAR]
            dateTime[1] = c[Calendar.HOUR_OF_DAY].toString() + ":" + c[Calendar.MINUTE]
            return dateTime
        }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (btsocket != null) {
                outputStream!!.close()
                btsocket!!.close()
                btsocket = null
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            btsocket = socket
            if (btsocket != null) {
                printText(txtMessage!!.text.toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private var btsocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    fun resetPrint() {
        try {
            outputStream!!.write(PrinterCommands.ESC_FONT_COLOR_DEFAULT)
            outputStream!!.write(PrinterCommands.FS_FONT_ALIGN)
            outputStream!!.write(PrinterCommands.ESC_ALIGN_LEFT)
            outputStream!!.write(PrinterCommands.ESC_CANCEL_BOLD)
            outputStream!!.write(PrinterCommands.LF.toInt())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {

    }
}