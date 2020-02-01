package com.ganderson.exploreni.ui.components

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.ganderson.exploreni.R

class LoadingDialog(cxt: Context, message: String) : AlertDialog(cxt) {

    init {
        val inflater = cxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val loadingLayout = inflater.inflate(R.layout.dialog_loading, null)
        val loadingMessage = loadingLayout.findViewById<TextView>(R.id.tvLoadingMessage)
        loadingMessage.text = message

        setView(loadingLayout)
        setCancelable(false)

    }
}