package com.example.floristics_app

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.prototype.DatabaseHelper
import com.google.zxing.integration.android.IntentIntegrator
import net.glxn.qrgen.core.scheme.VCard
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {
    var code = ""
    private var mDb: SQLiteDatabase? = null
    private var mDBHelper: DatabaseHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        mDBHelper = DatabaseHelper(this)
        try {
            mDBHelper!!.updateDataBase()
        } catch (mIOException: IOException) {
            throw Error("UnableToUpdateDatabase")
        }
        try {
            mDb = mDBHelper!!.getWritableDatabase()
        } catch (mSQLException: SQLException) {
            throw mSQLException
        }
        //Check for storage permission

        if (!checkPermissionForExternalStorage()) {
            requestPermissionForExternalStorage()
        }
        layout = 1
    }

    fun qrScan(view: View) {
        scanCode()
    }

    fun scanCode() {
        val integrator = IntentIntegrator(this)
        integrator.captureActivity = CaptureAct::class.java
        integrator.setOrientationLocked(false)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.setPrompt("Scanning Code")
        integrator.initiateScan()
    }

    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?
    ) {
        val result =
                IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                var plant = ""
                code = (result.contents).toString()
                check = code

                var cursor = mDb!!.rawQuery("SELECT code, text FROM plant WHERE _id =" + code + "", null)
                cursor.moveToFirst()
                code = cursor.getString(0)
                setContentView(R.layout.plant_info)
                layout = 2

                findViewById<TextView>(R.id.name_plant).setText(cursor.getString(1));

                cursor = mDb!!.rawQuery("SELECT * FROM light where _id =" + code[0].toString() + "", null)
                cursor.moveToFirst()
                plant = cursor.getString(1)
                findViewById<TextView>(R.id.light).setText(plant);

                cursor = mDb!!.rawQuery("SELECT * FROM min_temp where _id =" + code[1].toString() + "", null)
                cursor.moveToFirst()
                plant = cursor.getString(1)
                findViewById<TextView>(R.id.min_temp).setText(plant);

                cursor = mDb!!.rawQuery("SELECT * FROM max_temp where _id =" + code[2].toString() + "", null)
                cursor.moveToFirst()
                plant = cursor.getString(1)
                findViewById<TextView>(R.id.max_temp).setText(plant);

                cursor = mDb!!.rawQuery("SELECT * FROM mode where _id =" + code[3].toString() + "", null)
                cursor.moveToFirst()
                plant = cursor.getString(1)
                findViewById<TextView>(R.id.mode).setText(plant);

                cursor = mDb!!.rawQuery("SELECT * FROM water where _id =" + code[4].toString() + "", null)
                cursor.moveToFirst()
                plant = cursor.getString(1)
                findViewById<TextView>(R.id.water).setText(plant);
                cursor.close()
            } else {
                Toast.makeText(this, "No Results", Toast.LENGTH_LONG).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun add_plant(view: View){
        setContentView(R.layout.add_plant)
        layout = 3
        var cursor = mDb!!.rawQuery("SELECT COUNT(*) FROM light", null)
        cursor.moveToFirst()
        var data = arrayOfNulls<String>(cursor.getInt(0))
        cursor = mDb!!.rawQuery("SELECT text FROM light", null)
        cursor.moveToFirst()
        var i = 0
        while (!cursor.isAfterLast) {
            data[i] = cursor.getString(0)
            i++
            cursor.moveToNext()
        }
        cursor.close()
        var adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        var spinner = findViewById<View>(R.id.spinner_light) as Spinner
        spinner.adapter = adapter

        cursor = mDb!!.rawQuery("SELECT COUNT(*) FROM min_temp", null)
        cursor.moveToFirst()
        data = arrayOfNulls<String>(cursor.getInt(0))
        cursor = mDb!!.rawQuery("SELECT text FROM min_temp", null)
        cursor.moveToFirst()
        i = 0
        while (!cursor.isAfterLast) {
            data[i] = cursor.getString(0)
            i++
            cursor.moveToNext()
        }
        cursor.close()
        adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner = findViewById<View>(R.id.spinner_min_temp) as Spinner
        spinner.adapter = adapter

        cursor = mDb!!.rawQuery("SELECT COUNT(*) FROM max_temp", null)
        cursor.moveToFirst()
        data = arrayOfNulls<String>(cursor.getInt(0))
        cursor = mDb!!.rawQuery("SELECT text FROM max_temp", null)
        cursor.moveToFirst()
        i = 0
        while (!cursor.isAfterLast) {
            data[i] = cursor.getString(0)
            i++
            cursor.moveToNext()
        }
        cursor.close()
        adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner = findViewById<View>(R.id.spinner_max_temp) as Spinner
        spinner.adapter = adapter

        cursor = mDb!!.rawQuery("SELECT COUNT(*) FROM mode", null)
        cursor.moveToFirst()
        data = arrayOfNulls<String>(cursor.getInt(0))
        cursor = mDb!!.rawQuery("SELECT text FROM mode", null)
        cursor.moveToFirst()
        i = 0
        while (!cursor.isAfterLast) {
            data[i] = cursor.getString(0)
            i++
            cursor.moveToNext()
        }
        cursor.close()

        adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner = findViewById<View>(R.id.spinner_mode) as Spinner
        spinner.adapter = adapter

        cursor = mDb!!.rawQuery("SELECT COUNT(*) FROM water", null)
        cursor.moveToFirst()
        data = arrayOfNulls<String>(cursor.getInt(0))
        cursor = mDb!!.rawQuery("SELECT text FROM water", null)
        cursor.moveToFirst()
        i = 0
        while (!cursor.isAfterLast) {
            data[i] = cursor.getString(0)
            i++
            cursor.moveToNext()
        }
        cursor.close()
        adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner = findViewById<View>(R.id.spinner_water) as Spinner
        spinner.adapter = adapter
    }



    fun add(view: View){
        var check = ""
        check += (findViewById<Spinner>(R.id.spinner_light).selectedItemPosition + 1).toString()
        check += (findViewById<Spinner>(R.id.spinner_min_temp).selectedItemPosition + 1).toString()
        check += (findViewById<Spinner>(R.id.spinner_max_temp).selectedItemPosition + 1).toString()
        check += (findViewById<Spinner>(R.id.spinner_mode).selectedItemPosition + 1).toString()
        check += (findViewById<Spinner>(R.id.spinner_water).selectedItemPosition + 1).toString()

        if(!findViewById<EditText>(R.id.name).getText().toString().equals("")) {
            val database: SQLiteDatabase = mDBHelper!!.getWritableDatabase()
            val contentValues = ContentValues()
            contentValues.put("text", findViewById<EditText>(R.id.name).getText().toString())
            contentValues.put("code", check)
            database.insert("plant", null, contentValues)
            findViewById<EditText>(R.id.name).setText("")
            Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show()
        }

        else{
            Toast.makeText(this, "Пустое поле", Toast.LENGTH_SHORT).show()
        }
    }

    //Function for Generating QR code
    var vCard = VCard("")
    var qrImage = net.glxn.qrgen.android.QRCode.from(vCard).bitmap()
    fun generateQRCode(view: View)
    {
        var flowerStr = findViewById<Spinner>(R.id.input_name).selectedItem.toString()
        var cursor = mDb!!.rawQuery("SELECT _id FROM plant WHERE text ='" + flowerStr + "'", null)
        cursor.moveToFirst()
        code = cursor.getString(0)
        vCard = VCard(code)
        qrImage = net.glxn.qrgen.android.QRCode.from(vCard.name).bitmap()
        if(qrImage != null)
        {
            findViewById<ImageView>(R.id.imageView_qrCode).setImageBitmap(qrImage)
        }
    }
    //function for requesting storage access
    fun requestPermissionForExternalStorage() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "External Storage permission needed. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show()
        }
    }
    //fuunction for checking storage permission
    fun checkPermissionForExternalStorage(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true
        } else {
            return false
        }
    }

    //funtion for saving image into gallery
    fun saveImage(view: View){
        var savedImagePath: String? = null
        var imageFileName = "QR" + getTimeStamp() + ".jpg"
        var storageDir = File(
                getExternalFilesDir(Environment.DIRECTORY_PICTURES), "/QRGenerator")
        var success = true
        if (!storageDir.exists()) {
            success = storageDir.mkdirs()
        }
        if (success) {
            var imageFile = File(storageDir, imageFileName)
            savedImagePath = imageFile.getAbsolutePath()
            try {
                var fOut = FileOutputStream(imageFile)
                qrImage.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                fOut.close()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

            var mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            var f = File(savedImagePath)
            var contentUri = Uri.fromFile(f)
            mediaScanIntent.setData(contentUri)
            sendBroadcast(mediaScanIntent)
            Toast.makeText(this,"QR Image saved into folder: QRGenerator in Gallery",Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this,"ERROR SAVING IMAGE",Toast.LENGTH_SHORT).show()
        }
    }


    fun getTimeStamp(): String? {
        val tsLong = System.currentTimeMillis() / 1000
        val ts = tsLong.toString()

        return ts
    }

    fun gen(view: View){
        setContentView(R.layout.generate_qr)
        layout = 4
        var cursor = mDb!!.rawQuery("SELECT COUNT(*) FROM plant", null)
        cursor.moveToFirst()
        var data = arrayOfNulls<String>(cursor.getInt(0))
        cursor = mDb!!.rawQuery("SELECT text FROM plant", null)
        cursor.moveToFirst()
        var i = 0
        while (!cursor.isAfterLast) {
            data[i] = cursor.getString(0)
            i++
            cursor.moveToNext()
        }
        cursor.close()
        var adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        var spinner = findViewById<View>(R.id.input_name) as Spinner
        spinner.adapter = adapter
    }
    var layout = 0
    var check = ""
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            when(layout){
                1 -> android.os.Process.killProcess(android.os.Process.myPid())
                2 -> {setContentView(R.layout.main)
                    layout = 1}
                3 -> {setContentView(R.layout.main)
                    layout = 1}
                4 -> {setContentView(R.layout.main)
                    layout = 1}
                5 -> { setContentView(R.layout.plant_info)
                    layout = 2

                    var cursor = mDb!!.rawQuery("SELECT code, text FROM plant WHERE _id =" + check + "", null)
                    cursor.moveToFirst()
                    code = cursor.getString(0)
                    findViewById<TextView>(R.id.name_plant).setText(cursor.getString(1));

                    cursor = mDb!!.rawQuery("SELECT * FROM light where _id =" + code[0].toString() + "", null)
                    cursor.moveToFirst()
                    var plant = cursor.getString(1)
                    findViewById<TextView>(R.id.light).setText(plant);

                    cursor = mDb!!.rawQuery("SELECT * FROM min_temp where _id =" + code[1].toString() + "", null)
                    cursor.moveToFirst()
                    plant = cursor.getString(1)
                    findViewById<TextView>(R.id.min_temp).setText(plant);

                    cursor = mDb!!.rawQuery("SELECT * FROM max_temp where _id =" + code[2].toString() + "", null)
                    cursor.moveToFirst()
                    plant = cursor.getString(1)
                    findViewById<TextView>(R.id.max_temp).setText(plant);

                    cursor = mDb!!.rawQuery("SELECT * FROM mode where _id =" + code[3].toString() + "", null)
                    cursor.moveToFirst()
                    plant = cursor.getString(1)
                    findViewById<TextView>(R.id.mode).setText(plant);

                    cursor = mDb!!.rawQuery("SELECT * FROM water where _id =" + code[4].toString() + "", null)
                    cursor.moveToFirst()
                    plant = cursor.getString(1)
                    findViewById<TextView>(R.id.water).setText(plant);
                    cursor.close()
                }
            }
        }
        return true
    }

    fun common(view:View){
        setContentView(R.layout.common_info)
        layout = 5
    }
}