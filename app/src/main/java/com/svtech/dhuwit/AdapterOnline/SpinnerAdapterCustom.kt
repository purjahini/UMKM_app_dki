package com.svtech.dhuwit.AdapterOnline

import android.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.svtech.dhuwit.modelOnline.ItemOption
import java.lang.String


internal class SpinnerAdapterCustom(
    context: Context?,
    textViewResourceId: Int,
    modelArrayList: ArrayList<ItemOption>
) :
    ArrayAdapter<ItemOption>(context!!, textViewResourceId, modelArrayList) {
    private val myarrayList: ArrayList<ItemOption>
    override fun getDropDownView(
        position: Int,
        convertView: View?,
        @NonNull parent: ViewGroup
    ): View {
        return getCustomView(position, parent)
    }

    @Nullable
    override fun getItem(position: Int): ItemOption {
        return myarrayList[position]
    }

    override fun getCount(): Int = myarrayList.size

    @NonNull
    override fun getView(position: Int, convertView: View?, @NonNull parent: ViewGroup): View {
        return getCustomView(position, parent)
    }

    private fun getCustomView(position: Int, parent: ViewGroup): View {
        val model: ItemOption = getItem(position)
        val spinnerRow: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.simple_spinner_dropdown_item, parent, false)
        val label: TextView = spinnerRow.findViewById(R.id.text1)
        label.text = String.format("%s", if (model != null) model.optLabel else "")
        return spinnerRow
    }

    init {
        myarrayList = modelArrayList
    }
}