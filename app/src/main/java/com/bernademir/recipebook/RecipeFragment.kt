package com.bernademir.recipebook

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import java.io.ByteArrayOutputStream
import java.lang.Exception

class RecipeFragment : Fragment() {

    var selectedImageUri : Uri? = null
    var selectedBitmap : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view : View = inflater.inflate(R.layout.fragment_recipe, container, false)
        // Inflate the layout for this fragment
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.saveButton).setOnClickListener{
            save(it)
        }

        view.findViewById<ImageView>(R.id.imageView).setOnClickListener {
            selectImage(it)
        }

        arguments?.let {
            var information = RecipeFragmentArgs.fromBundle(it).info
            if(information == "frommenu"){
                view.findViewById<TextView>(R.id.mealNameText).setText("")
                view.findViewById<TextView>(R.id.ingredientText).setText("")
                view.findViewById<Button>(R.id.saveButton).visibility = View.VISIBLE

                val imageBackground = BitmapFactory.decodeResource(context?.resources, R.drawable.image)
                view.findViewById<ImageView>(R.id.imageView).setImageBitmap(imageBackground)

            }else{
                view.findViewById<Button>(R.id.saveButton).visibility = View.INVISIBLE

                val selectedId = RecipeFragmentArgs.fromBundle(it).id
                context?.let {
                    try {
                        val db = it.openOrCreateDatabase("Meals", Context.MODE_PRIVATE, null)
                        val cursor = db.rawQuery("SELECT * FROM meals WHERE id = ?", arrayOf(selectedId.toString()))

                        val mealNameInd = cursor.getColumnIndex("mealName")
                        val ingredientsInd = cursor.getColumnIndex("ingredients")
                        val mealImageInd = cursor.getColumnIndex("mealImage")

                        while (cursor.moveToNext()){
                            view.findViewById<TextView>(R.id.mealNameText).setText(cursor.getString(mealImageInd))
                            view.findViewById<TextView>(R.id.ingredientText).setText(cursor.getString(ingredientsInd))

                            val byteArray = cursor.getBlob(mealImageInd)
                            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                            view.findViewById<ImageView>(R.id.imageView).setImageBitmap(bitmap)
                        }
                        cursor.close()

                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun save(view: View){
        val mealName = view.findViewById<TextView>(R.id.mealNameText).text.toString()
        val mealIngredient = view.findViewById<TextView>(R.id.ingredientText).text.toString()

        if(selectedBitmap != null){
            val smallerBitmap = resizeBitmap(selectedBitmap!!, 300)
            //bitmapi sqlite a kaydetmek icin veriye cevirmek gerekli
            val outputStream = ByteArrayOutputStream()
            smallerBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteArray = outputStream.toByteArray()

            try {
                context?.let {
                    val db = it.openOrCreateDatabase("Meals", Context.MODE_PRIVATE, null)
                    db.execSQL("CREATE TABLE IF NOT EXISTS meals (id INTEGER PRIMARY KEY, mealName VARCHAR, ingredients VARCHAR, mealImage BLOB)")
                    val sqlString = "INSERT INTO meals (mealName, ingredients, mealImage) VALUES (?, ?, ?)"
                    val statement = db.compileStatement(sqlString)
                    statement.bindString(1, mealName)
                    statement.bindString(2, mealIngredient)
                    statement.bindBlob(3, byteArray)
                    statement.execute()
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
            val action = RecipeFragmentDirections.actionRecipeFragmentToListFragment()
            Navigation.findNavController(view).navigate(action)
        }
    }

    private fun selectImage(view: View){

        activity?.let {
            if(ContextCompat.checkSelfPermission(it.applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            }else{
               val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, 2)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if(requestCode == 1){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, 2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){
            selectedImageUri = data.data

            try {
                context?.let {
                    if(selectedImageUri != null){
                        if(Build.VERSION.SDK_INT >= 28){
                            val source = ImageDecoder.createSource(it.contentResolver, selectedImageUri!!)
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                            view?.findViewById<ImageView>(R.id.imageView)?.setImageBitmap(selectedBitmap)

                        }else{
                            selectedBitmap = MediaStore.Images.Media.getBitmap(it.contentResolver, selectedImageUri)
                            view?.findViewById<ImageView>(R.id.imageView)?.setImageBitmap(selectedBitmap)
                        }
                    }
                }
            }catch (e : Exception){
                e.printStackTrace()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap{
        var width = bitmap.width
        var height = bitmap.height

        val bitmapRatio: Double = width.toDouble() / height.toDouble()

        if(bitmapRatio >1){//yatay
            width = maxSize
            val resizedHeight = width/bitmapRatio
            height = resizedHeight.toInt()
        }else{//dikey
            height = maxSize
            val resizedWidth = height * bitmapRatio
            width = resizedWidth.toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, width,height, true)
    }
}