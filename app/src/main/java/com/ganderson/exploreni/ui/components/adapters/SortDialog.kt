package com.ganderson.exploreni.ui.components.adapters

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.ganderson.exploreni.R
import kotlinx.android.synthetic.main.dialog_sort.*

class SortDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle("Sort")
        setContentView(R.layout.dialog_sort)
        rbAlphabetical.setOnClickListener { dismiss() }
        rbNearby.setOnClickListener { dismiss() }
    }
}