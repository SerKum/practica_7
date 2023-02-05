package com.example.practica_7.fragments

import android.content.Context
import android.content.Intent
import android.icu.text.CaseMap.Title
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import com.example.practica_7.R
import com.example.practica_7.activities.MainActivity
import com.example.practica_7.databinding.FragmentAddBinding
import com.example.practica_7.entities.Archivo
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File


class AddFragment : Fragment() {


    private val PATH_SNAPSHOT = "archivos"

    private lateinit var binding: FragmentAddBinding

    private lateinit var mStoregeReference: StorageReference
    private lateinit var mDatabaseReference: DatabaseReference

    private lateinit var fileName : String

    private var mFileUri: Uri? = null

    private var mActivity: MainActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddBinding.inflate(inflater,container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mActivity = activity as? MainActivity

        mStoregeReference = FirebaseStorage.getInstance().reference
        mDatabaseReference = FirebaseDatabase
            .getInstance("https://gestor-b1acf-default-rtdb.europe-west1.firebasedatabase.app")
            .reference
            .child(PATH_SNAPSHOT)

        binding.btnAddFile.setOnClickListener { openFileExplorer() }
    }

    private fun openFileExplorer(){
        val intent = Intent()
            .setType("*/*")
            .setAction(Intent.ACTION_GET_CONTENT)

        startActivityForResult(Intent.createChooser(intent,"Selecciona un archivo"),777)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 777){
            mFileUri = data?.data
        }

        fileName = getFileNameFromUri(requireContext(),mFileUri)!!
        postFile()
    }

    fun getFileNameFromUri(context: Context, uri: Uri?): String? {
        return DocumentFile.fromSingleUri(context, uri!!)?.name
    }

    private fun postFile() {
        binding.horizontalProgressBar.visibility = View.VISIBLE
        val key = mDatabaseReference.push().key!!
        val storageReference = mStoregeReference.child(PATH_SNAPSHOT).child(fileName)
        if (mFileUri != null) {
            storageReference.putFile(mFileUri!!)
                .addOnProgressListener {
                    val progress = (100 * it.bytesTransferred / it.totalByteCount).toDouble()
                    binding.horizontalProgressBar.progress = progress.toInt()
                    binding.tvMessage.text = "$progress%"
                }
                .addOnCompleteListener {
                    binding.horizontalProgressBar.visibility = View.INVISIBLE
                }
                .addOnSuccessListener {
                    Snackbar.make(
                        binding.root,
                        "Archivo subido con éxito",
                        Snackbar.LENGTH_SHORT).show()
                    it.storage.downloadUrl.addOnSuccessListener {
                        saveFile(key,it.toString(),fileName)
                        binding.tvMessage.text = getString(R.string.post_message_title)
                    }
                }
                .addOnFailureListener {
                    Snackbar.make(
                        binding.root,
                        "No se ha podido subir, inténtalo más tarde",
                        Snackbar.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveFile(key: String,uri:String,title: String) {
        val archivo = Archivo(title= title, fileUri = uri)
        mDatabaseReference.child(key).setValue(archivo)
    }

}