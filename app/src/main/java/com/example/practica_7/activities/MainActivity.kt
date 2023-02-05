package com.example.practica_7.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.practica_7.R
import com.example.practica_7.databinding.ActivityMainBinding
import com.example.practica_7.fragments.AddFragment
import com.example.practica_7.fragments.HomeFragment
import com.example.practica_7.fragments.ProfileFragment
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var mActiveFragment: Fragment
    private lateinit var mFragmentManager: FragmentManager

    private val RC_SING_IN = 21
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    private var mFirebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setupAuth()

        setBottomBar()
    }

    private fun setupAuth() {
        mFirebaseAuth = FirebaseAuth.getInstance()
        mAuthStateListener = FirebaseAuth.AuthStateListener {
            val user = it.currentUser
            if (user == null) {
                startActivityForResult(
                    AuthUI.getInstance().createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(
                            Arrays.asList(
                                AuthUI.IdpConfig.EmailBuilder().build()
                            )
                    ).build(), RC_SING_IN)
            }
        }
    }

    private fun setBottomBar() {

        mFragmentManager = supportFragmentManager

        val homeFragment = HomeFragment()
        val addFragment = AddFragment()
        val profileFragment = ProfileFragment()

        mActiveFragment = homeFragment

        mFragmentManager.beginTransaction()
            .add(R.id.hostFragment,profileFragment,ProfileFragment::class.java.name)
            .hide(profileFragment).commit()
        mFragmentManager.beginTransaction()
            .add(R.id.hostFragment, addFragment, AddFragment::class.java.name)
            .hide(addFragment).commit()
        mFragmentManager.beginTransaction()
            .add(R.id.hostFragment, homeFragment, HomeFragment::class.java.name).commit()

        binding.babApp.setOnItemSelectedListener {
            when(it.itemId){
                R.id.action_home -> {
                    mFragmentManager.beginTransaction().hide(mActiveFragment).show(homeFragment).commit()
                    mActiveFragment = homeFragment
                    true
                }
                R.id.action_add -> {
                    mFragmentManager.beginTransaction().hide(mActiveFragment).show(addFragment).commit()
                    mActiveFragment = addFragment
                    true
                }
                R.id.action_profile -> {
                    mFragmentManager.beginTransaction().hide(mActiveFragment).show(profileFragment).commit()
                    mActiveFragment = profileFragment
                    true
                }
                else -> false
            }

        }

    }

    override fun onResume() {
        super.onResume()

        mFirebaseAuth?.addAuthStateListener(mAuthStateListener)
    }

    override fun onPause() {
        super.onPause()

        mFirebaseAuth?.removeAuthStateListener(mAuthStateListener)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SING_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Bienvenido...",
                    Toast.LENGTH_SHORT).show()
            } else {
                if (IdpResponse.fromResultIntent(data) == null) {
                    finish()
                }
            }
        }

    }


}