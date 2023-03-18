package com.elad.examapp.ui.adapters

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elad.examapp.databinding.BluetoothDeviceItemBinding

class BluetoothDeviceListAdapter(
    var deviceList: ArrayList<BluetoothDevice> = ArrayList()
) : RecyclerView.Adapter<BluetoothDeviceListAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding : BluetoothDeviceItemBinding = BluetoothDeviceItemBinding.inflate(inflater, parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = deviceList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(position)
    }

    fun addDevice(bluetoothDevice: BluetoothDevice) {
        deviceList.add(bluetoothDevice)
    }

    fun filterList(filteredList: ArrayList<BluetoothDevice>) {
        deviceList = filteredList
        notifyDataSetChanged()
    }

    fun clear() {
        deviceList = ArrayList()
        notifyDataSetChanged()
    }

    inner class MyViewHolder(private val binding : BluetoothDeviceItemBinding) : RecyclerView.ViewHolder(binding.root) {

            fun bind(position: Int) {
                try {
                    binding.deviceName = deviceList[position].name
                    binding.macAddress = deviceList[position].address
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        }
}