package com.example.floristics_app

import android.R.attr.data
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
                var product = ""
                code = (result.contents).toString()
                setContentView(R.layout.plant)
                var cursor = mDb!!.rawQuery("SELECT * FROM light where _id =" + code[0].toString() + "", null)
                cursor.moveToFirst()
                product = cursor.getString(1)
                findViewById<TextView>(R.id.light).setText(product);

                cursor = mDb!!.rawQuery("SELECT * FROM max_temp where _id =" + code[1].toString() + "", null)
                cursor.moveToFirst()
                product = cursor.getString(1)
                findViewById<TextView>(R.id.max_temp).setText(product);

                cursor = mDb!!.rawQuery("SELECT * FROM min_temp where _id =" + code[2].toString() + "", null)
                cursor.moveToFirst()
                product = cursor.getString(1)
                findViewById<TextView>(R.id.min_temp).setText(product);

                cursor = mDb!!.rawQuery("SELECT * FROM mode where _id =" + code[3].toString() + "", null)
                cursor.moveToFirst()
                product = cursor.getString(1)
                findViewById<TextView>(R.id.mode).setText(product);

                cursor = mDb!!.rawQuery("SELECT * FROM water where _id =" + code[4].toString() + "", null)
                cursor.moveToFirst()
                product = cursor.getString(1)
                findViewById<TextView>(R.id.water).setText(product);

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
        val data = arrayOfNulls<String>(cursor.getInt(0))
        cursor = mDb!!.rawQuery("SELECT text FROM light", null)
        cursor.moveToFirst()
        var i = 0
        while (!cursor.isAfterLast) {
            data[i] = cursor.getString(0)
            i++
            cursor.moveToNext()
        }
        cursor.close()
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val spinner = findViewById<View>(R.id.spinner_light) as Spinner
        spinner.adapter = adapter
    }
}