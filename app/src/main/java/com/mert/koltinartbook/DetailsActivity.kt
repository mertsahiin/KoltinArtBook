package com.mert.koltinartbook

import android.Manifest
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.Image
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.mert.koltinartbook.databinding.ActivityDetailsBinding
import java.io.ByteArrayOutputStream
import java.lang.Exception
import kotlin.math.max

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    var selectedBitmap : Bitmap? = null
    private lateinit var database : SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        database = this.openOrCreateDatabase("Arts", Context.MODE_PRIVATE,null)
        RegisterLauncher()
        val intent = intent
        val info = intent.getStringExtra("info")
        if(info.equals("new")){
            binding.txtArtName.setText("")
            binding.txtArtistName.setText("")
            binding.txtYear.setText("")
            binding.button.visibility = View.VISIBLE
            val selectedImageBackGround = BitmapFactory.decodeResource(applicationContext.resources,R.drawable.indir)
            binding.imageView.setImageBitmap(selectedImageBackGround)
        }else{
            binding.button.visibility = View.INVISIBLE
            val selectedId = intent.getIntExtra("id" , 1)
            val cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(selectedId.toString()))
            val artNameIx = cursor.getColumnIndex("artName")
            val artistNameIx = cursor.getColumnIndex("artistName")
            val yearIx = cursor.getColumnIndex("year")
            val imageIx = cursor.getColumnIndex("image")

            while(cursor.moveToNext()){
                binding.txtArtName.setText(cursor.getString(artNameIx))
                binding.txtArtistName.setText(cursor.getString(artistNameIx))
                binding.txtYear.setText(cursor.getString(yearIx))
                val byteArray = cursor.getBlob(imageIx)
                val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                binding.imageView.setImageBitmap(bitmap)
            }
            cursor.close()

        }
    }

    fun changeImage(view : View){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(view,"Permission Needed For Gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",View.OnClickListener {
                    //izin iste
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }).show()
            }
            else{
                // izin iste
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        else{
            val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intent)

        }

    }
    fun SaveImage(view : View){
        val artistName = binding.txtArtistName.text.toString()
        val artName = binding.txtArtName.text.toString()
        val year = binding.txtYear.text.toString()
        if(selectedBitmap != null){
            val smallBitmap = MakeSmallerBitmap(selectedBitmap!!,300)
            val outPutStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outPutStream)
            val byteArray = outPutStream.toByteArray()

            try{
                //val database = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)
                database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artistName VARCHAR,artName VARCHAR , year VARCHAR,image BLOB )")
                val sqlString = "INSERT INTO arts(artistName,artName,year,image) VALUES(?,?,?,?)"
                val statement = database.compileStatement(sqlString)
                statement.bindString(1,artistName)
                statement.bindString(2,artName)
                statement.bindString(3,year)
                statement.bindBlob(4,byteArray)
                statement.execute()

            }catch ( e : Exception){
                e.printStackTrace()
            }
            val intent = Intent(this@DetailsActivity,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }


    }
   private fun MakeSmallerBitmap(image : Bitmap , maximumSize : Int) : Bitmap{
       var widht = image.width
       var height = image.height
       val bitmapRatio : Double = widht.toDouble() / height.toDouble()
       if(bitmapRatio < 1 ){
           //dikey
           height = maximumSize
           val scaledWidth = height * bitmapRatio
           widht = scaledWidth.toInt()
       }
       else{
           //yatay
           widht = maximumSize
           val scaledHeight = widht / bitmapRatio
           height = scaledHeight.toInt()
       }
       return Bitmap.createScaledBitmap(image,widht,height,true)


    }
    private fun RegisterLauncher(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == RESULT_OK){
                val intentFromResult = result.data
                if(intentFromResult != null){
                    val imageData = intentFromResult.data
                    if(imageData != null){
                        try {
                            if(Build.VERSION.SDK_INT <= 28){
                                val source = ImageDecoder.createSource(this.contentResolver,imageData)
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }else{
                                selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver,imageData)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }
                        }catch (e : Exception){
                            e.printStackTrace()
                        }

                    }

                }
            }
        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->

            if(result){
                val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            }else{
                Toast.makeText(this,"Permission Needed",Toast.LENGTH_LONG).show()
            }
        }

    }
}