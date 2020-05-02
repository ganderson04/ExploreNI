package com.ganderson.exploreni.ui.components

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.ganderson.exploreni.R

/**
 * Convenience class to create an AlertDialog with a loading message and spinner.
 */
class LoadingDialog(context: Context, message: String) : AlertDialog(context) {

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val loadingLayout = inflater.inflate(R.layout.dialog_loading, null)
        val loadingMessage = loadingLayout.findViewById<TextView>(R.id.tvLoadingMessage)
        loadingMessage.text = message

        setView(loadingLayout)
        setCancelable(false)
    }
}