package com.elad.examapp.ui.screens.splash

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.elad.examapp.R
import com.elad.examapp.utils.Constants.NAVIGATION_DELAY

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleNavigation(view.findNavController())
    }

    /**
     * A function to navigate after delay to the map destination.
     */
    private fun handleNavigation(navController: NavController) {
        Handler(Looper.getMainLooper()).postDelayed({
            navController.navigate(R.id.action_splash_dest_to_map_dest)
        }, NAVIGATION_DELAY)
    }
}