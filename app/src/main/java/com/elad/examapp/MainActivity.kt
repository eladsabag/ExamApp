package com.elad.examapp

import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.elad.examapp.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.my_nav_host_fragment) as NavHostFragment? ?: return
        navController = navHostFragment.navController

        initBottomTabsView()
    }

    private fun initBottomTabsView() {
        navController.addOnDestinationChangedListener(onDestinationChangedListener)
        binding.tabsLayout.addOnTabSelectedListener(onTabSelectedListener)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            // Do nothing
            true
        } else super.dispatchKeyEvent(event)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    private val onDestinationChangedListener : NavController.OnDestinationChangedListener =
        NavController.OnDestinationChangedListener { _, destination, _ ->
            binding.showTabsLayout = destination.id != R.id.splash_dest
        }

    private val onTabSelectedListener : OnTabSelectedListener = object : OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            when(tab?.position) {
                0 -> if (navController.currentDestination?.id == R.id.bluetooth_dest) navController.navigate(R.id.action_bluetooth_dest_to_map_dest)
                1 -> if (navController.currentDestination?.id == R.id.map_dest) navController.navigate(R.id.action_map_dest_to_bluetooth_dest)
            }
        }
        override fun onTabUnselected(tab: TabLayout.Tab?) {}
        override fun onTabReselected(tab: TabLayout.Tab?) {}
    }
}