package com.example.floristics_app

import android.content.ContentValues
import android.content.Intent
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.prototype.DatabaseHelper
import com.google.zxing.integration.android.IntentIntegrator
import java.io.IOException


class MainActivity : AppCompatActivity(), View.OnClickListener {
    var code = ""
    private var mDb: SQLiteDatabase? = null
    private var mDBHelper: DatabaseHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val scanBtn = findViewById<Button>(R.id.scanBtn)
        scanBtn.setOnClickListener(this)

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
    }

    override fun onClick(v: View?) {
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

                var cursor = mDb!!.rawQuery("SELECT code FROM plant WHERE text ='" + code + "'", null)
                cursor.moveToFirst()
                code = cursor.getString(0)

                setContentView(R.layout.plant)
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
}