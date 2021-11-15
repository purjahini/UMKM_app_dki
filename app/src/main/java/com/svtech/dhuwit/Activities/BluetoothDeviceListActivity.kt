package com.svtech.dhuwit.Activities

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.svtech.dhuwit.Models.DeviceBluetooth
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.MyConstant
import com.svtech.dhuwit.Utils.setToolbar
import kotlinx.android.synthetic.main.activity_bluetooth_device_list.*

class BluetoothDeviceListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_device_list)
        setToolbar(this,"Perangkat Bluetooth Terpasang")
        /*Adapter listview*/
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val mPairedDevice = bluetoothAdapter.bondedDevices
        val listPairedDevice = mutableListOf<DeviceBluetooth>()
        if(mPairedDevice.size > 0){
            for (item in mPairedDevice){
                listPairedDevice.add(DeviceBluetooth(item.name, item.address))
            }
        }else{
            listPairedDevice.add(DeviceBluetooth("Belum ada perangkat bluetooth yang terpasang",""))
        }
        /*Set listview*/
        listBluetoothDevice.adapter = ArrayAdapter<DeviceBluetooth>(this, android.R.layout.simple_list_item_1, listPairedDevice)

        listBluetoothDevice.setOnItemClickListener { adapterView, view, i, l ->
            try {
                bluetoothAdapter.cancelDiscovery()
                val address = (adapterView.adapter.getItem(i) as DeviceBluetooth).address
                val name = (adapterView.adapter.getItem(i) as DeviceBluetooth).name
                val inten = Intent()
                inten.putExtra(MyConstant.DEVICE_ADDRESS, address)
                inten.putExtra(MyConstant.DEVICE_NAME, name)
                setResult(Activity.RESULT_OK, inten)
                finish()
            }catch (e :Exception){
                e.printStackTrace()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(bluetoothAdapter != null) bluetoothAdapter.cancelDiscovery()
    }

}