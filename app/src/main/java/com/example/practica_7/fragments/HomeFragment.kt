package com.example.practica_7.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.practica_7.R
import com.example.practica_7.databinding.FragmentHomeBinding
import com.example.practica_7.databinding.ItemDocumentBinding
import com.example.practica_7.entities.Archivo
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import java.io.File


class HomeFragment : Fragment() {

    private lateinit var mBinding: FragmentHomeBinding

    private lateinit var mFirebaseAdapter: FirebaseRecyclerAdapter<Archivo,ArchivoHolder>

    private lateinit var mLayoutManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mBinding = FragmentHomeBinding.inflate(inflater,container,false)

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()
    }

    private fun setUpRecyclerView(){
        val query = FirebaseDatabase.getInstance("https://gestor-b1acf-default-rtdb.europe-west1.firebasedatabase.app").reference.child("archivos")

        val options = FirebaseRecyclerOptions.Builder<Archivo>()
            .setQuery(query, Archivo::class.java).build()

        mFirebaseAdapter = object : FirebaseRecyclerAdapter<Archivo, ArchivoHolder>(options) {
            private lateinit var mContext: Context

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchivoHolder {
                mContext = parent.context

                val view = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_document, parent, false)

                return ArchivoHolder(view)
            }

            override fun onBindViewHolder(holder: ArchivoHolder, position: Int, model: Archivo) {
                val archivo = getItem(position)

                with(holder) {
                    setListener(archivo)

                    var f = File(archivo.title)

                    val icon : Int

                    when (f.extension) {
                        "pdf" -> icon = R.drawable.pdf_icon
                        "doc" -> icon = R.drawable.doc_icon
                        "txt" -> icon = R.drawable.txt_icon
                        "xls" -> icon = R.drawable.xls_icon
                        else -> {
                            icon = R.drawable.unknown_icon
                        }
                    }
                    binding.tvDocName.text = archivo.title
                    Glide.with(mContext)
                        .load(icon)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(binding.imgBtnDoc)
                }
            }

            override fun onDataChanged() {
                super.onDataChanged()

                mBinding.progressBar.visibility = View.GONE
                notifyDataSetChanged()
            }

            override fun onError(error: DatabaseError) {
                super.onError(error)

                Toast.makeText(mContext, error.message, Toast.LENGTH_SHORT).show()
            }
        }

        mLayoutManager = GridLayoutManager(context,3)

        mBinding.rvDocs.apply {
            layoutManager = mLayoutManager
            adapter = mFirebaseAdapter
        }
    }

    override fun onStart() {
        super.onStart()

        mFirebaseAdapter.startListening()

    }

    override fun onStop() {
        super.onStop()

        mFirebaseAdapter.stopListening()
    }

    private fun openFile(url : String){
        val webpage: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        startActivityForResult(intent,777)
    }

    inner class ArchivoHolder(view: View): RecyclerView.ViewHolder(view) {
        val binding = ItemDocumentBinding.bind(view)

        fun setListener(archivo: Archivo){
            binding.imgBtnDoc.setOnClickListener{
                openFile(archivo.fileUri)
            }
        }
    }

}