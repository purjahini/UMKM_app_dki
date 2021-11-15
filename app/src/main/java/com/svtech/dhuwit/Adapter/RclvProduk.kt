package com.svtech.dhuwit.Adapter

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.orm.SugarRecord
import com.svtech.dhuwit.Activities.AddProdukActivity
import com.svtech.dhuwit.Activities.MenuPembelianActivity
import com.svtech.dhuwit.Models.ItemTransaksi
import com.svtech.dhuwit.Models.Produk
import com.svtech.dhuwit.Models.Transaksi
import com.svtech.dhuwit.R
import com.svtech.dhuwit.Utils.numberToCurrency
import kotlinx.android.synthetic.main.layout_item_produk.view.*
import kotlinx.android.synthetic.main.layout_total_order.view.*
import java.text.SimpleDateFormat
import java.util.*

/*Adapter recycler view untuk menapilkan item produk*/
class RclvProduk :
    RecyclerView.Adapter<RclvProduk.ViewHolder> {
    val context: Context
    var listProduk: MutableList<Produk>
    val order: Boolean
    var saveListProduk: MutableList<Produk>

    constructor(
        context: Context,
        listProduk: MutableList<Produk>,
        sort: Boolean,
        order: Boolean
    ) : super() {
        this.context = context
        this.listProduk =
            if (sort) listProduk.sortedBy { it.nama } as MutableList<Produk> else listProduk
        this.order = order
        this.saveListProduk = listProduk
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(produk: Produk, context: Context) {
            Glide.with(context).load(Base64.decode(produk.foto, Base64.DEFAULT)).fitCenter()
                .into(itemView.imgFoto)
            itemView.tvNamaProduk.text = produk.nama
            itemView.tvKategori.text = produk.kategori?.nama
            itemView.tvHargaProduk.text = numberToCurrency(produk.harga!!)
            itemView.tvStok.text = "Stok : " + produk.stok.toString()
            /*Tampilkan label stok habis*/
            if (produk.stok == 0) {
                itemView.imgStokHabis.visibility = View.VISIBLE
            } else {
                itemView.imgStokHabis.visibility = View.INVISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_item_produk, parent, false)
        )
    }

    override fun getItemCount(): Int = listProduk.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val produk = listProduk[position]
        holder.bind(produk, context)
        holder.itemView.setOnClickListener {
            if (order) {
                /*return jika stok habis*/
                if (produk.stok == 0) {
                    Toast.makeText(context, "STOK HABIS :(", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val transaksi =
                    SugarRecord.find(Transaksi::class.java, "status = ?", "1").firstOrNull()
                val itemTransaksi = SugarRecord.find(
                    ItemTransaksi::class.java,
                    "produk_id = ? and id_transaksi = ?",
                    produk.id.toString(),
                    transaksi?.id.toString()
                ).firstOrNull()


                val activity = context as MenuPembelianActivity
                val view =
                    LayoutInflater.from(context).inflate(R.layout.layout_total_order, null, false)
                MaterialAlertDialogBuilder(
                    context
                )
                    .setView(view)
                    .setNegativeButton("Batal") { dialogInterface, _ -> dialogInterface.dismiss() }
                    .setPositiveButton("Tambah") { _, _ ->
                        /*Cek apakah sudah mencapai stok maksimum*/
                        if (itemTransaksi != null) {
                            if (itemTransaksi.jumlah?.plus(
                                    view.tvJumlah.text.toString().toInt()
                                )!! > produk.stok!!
                            ) {
                                Toast.makeText(
                                    context,
                                    "Pembelian telah mencapai batas maksimum !!!",
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                                return@setPositiveButton
                            }
                        }

                        if (transaksi == null) {
                            val transaksi = Transaksi(
                                status = true,
                                tanggalTrasaksi = SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(
                                    Date().time
                                )
                            )
                            transaksi.save()
                            val item = ItemTransaksi(
                                jumlah = view.tvJumlah.text.toString().toInt(),
                                namaProduk = produk.nama,
                                hargaProduk = produk.harga,
                                fotoProduk = produk.foto,
                                kategori = produk.kategori?.nama,
                                minimalPembelianProduk = produk.minimalPembelian,
                                diskonProduk = produk.diskon,
                                stokProduk = produk.stok,
                                satuan = produk.satuan,
                                produkId = produk.id,
                                idTransaksi = transaksi.id
                            )
                            item.save()
                            transaksi.totalPembayaran = item.jumlah!! * item.hargaProduk!!
                            /*jika ada diskon*/
                            if (item.minimalPembelianProduk != null && item.jumlah!! >= item.minimalPembelianProduk!!) {
                                transaksi.diskon =
                                    (item.jumlah!! * item.hargaProduk!!) * (item.diskonProduk!! / 100)
                                transaksi.totalPembayaran =
                                    transaksi.totalPembayaran?.minus(transaksi.diskon!!)
                            }
                            transaksi.save()
                        } else {
                            /*Tambah item yang sudah ada di keranjang*/
                            if (itemTransaksi != null) {
                                val tempJumlah = itemTransaksi.jumlah;
                                val jumlahItemTambahan = view.tvJumlah.text.toString().toInt()
                                itemTransaksi.jumlah =
                                    itemTransaksi.jumlah?.plus(jumlahItemTambahan)
                                itemTransaksi.save()
                                transaksi.totalPembayaran =
                                    transaksi.totalPembayaran?.plus(
                                        (jumlahItemTambahan * itemTransaksi.hargaProduk!!)
                                    )
                                /*jika ada diskon*/
                                if (itemTransaksi.minimalPembelianProduk != null && itemTransaksi.jumlah!! >= itemTransaksi.minimalPembelianProduk!!) {
                                    /*jika item sebelumnya sudah di diskon*/
                                    if (tempJumlah!! >= itemTransaksi.minimalPembelianProduk!!) {
                                        val diskonTambahan =
                                            (jumlahItemTambahan * itemTransaksi.hargaProduk!!) * (itemTransaksi.diskonProduk!! / 100)
                                        transaksi.diskon = transaksi.diskon?.plus(diskonTambahan)
                                        transaksi.totalPembayaran =
                                            transaksi.totalPembayaran?.minus(diskonTambahan)
                                    } else {
                                        val diskon =
                                            (itemTransaksi.jumlah!! * itemTransaksi.hargaProduk!!) * (itemTransaksi.diskonProduk!! / 100)
                                        transaksi.diskon = transaksi.diskon?.plus(diskon)
                                        transaksi.totalPembayaran =
                                            transaksi.totalPembayaran?.minus(diskon)
                                    }

                                }
                                transaksi.save()
                            } else {
                                val item = ItemTransaksi(
                                    jumlah = view.tvJumlah.text.toString().toInt(),
                                    idTransaksi = transaksi.id,
                                    namaProduk = produk.nama,
                                    hargaProduk = produk.harga,
                                    fotoProduk = produk.foto,
                                    minimalPembelianProduk = produk.minimalPembelian,
                                    diskonProduk = produk.diskon,
                                    stokProduk = produk.stok,
                                    satuan = produk.satuan,
                                    produkId = produk.id,
                                    kategori = produk.kategori?.nama
                                )
                                item.save()
                                transaksi.totalPembayaran =
                                    transaksi.totalPembayaran?.plus((item.jumlah!! * item.hargaProduk!!))
                                /*jika ada diskon*/
                                if (item.minimalPembelianProduk != null && item.jumlah!! >= item.minimalPembelianProduk!!) {
                                    transaksi.diskon =
                                        transaksi.diskon?.plus((item.jumlah!! * item.hargaProduk!!) * (item.diskonProduk!! / 100))
                                    transaksi.totalPembayaran =
                                        transaksi.totalPembayaran?.minus(transaksi.diskon!!)
                                }
                                transaksi.save()
                            }

                        }
                        activity.setBadgeKeranjang()
                    }.show()

                view.btnPlus.setOnClickListener {
                    val jumlahDiKeranjang = if (itemTransaksi == null) 0 else itemTransaksi.jumlah
                    val jumlahTambahan = view.tvJumlah.text.toString().toInt()
                    val jumlahTotal = jumlahDiKeranjang?.plus(jumlahTambahan)

                    Log.d("jumlah", jumlahTotal.toString())

                    /*Cek apakah sudah mencapai stok maksimum*/
                    if (jumlahTotal!! < produk.stok!!) {
                        view.tvJumlah.text = (view.tvJumlah.text.toString().toInt() + 1).toString()
                    } else {
                        Toast.makeText(
                            context,
                            "Pembelian telah mencapai batas maksimum !!!",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                }
                view.btnMinus.setOnClickListener {
                    if (view.tvJumlah.text.toString().toInt() != 1) {
                        view.tvJumlah.text =
                            (view.tvJumlah.text.toString().toInt() - 1).toString()
                    }
                }
            } else {
                val popupMenu = PopupMenu(context, it)
                popupMenu.menuInflater.inflate(R.menu.menu_edit_item, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.menuEdit -> editItem(produk)
                        R.id.menuHapus -> hapusItem(produk)
                    }
                    return@setOnMenuItemClickListener true
                }
                popupMenu.show()
            }
        }
    }

    private fun hapusItem(produk: Produk) {
        MaterialAlertDialogBuilder(context).setTitle("Hapus")
            .setMessage("Apakah anda yakin ingin menghapus?")
            .setPositiveButton("Hapus", DialogInterface.OnClickListener { dialogInterface, i ->
                listProduk.removeAt(listProduk.indexOf(produk))
                produk.delete()
                notifyDataSetChanged()
            })
            .setNegativeButton(
                "Batal",
                DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
            .show()

    }

    private fun editItem(produk: Produk) {
        val intent = Intent(context, AddProdukActivity::class.java)
        intent.putExtra("produk", produk.id)
        intent.putExtra("update", true)
        context.startActivity(intent)
    }

    fun sortItem(sort: String) {
        when (sort) {
            "Asc" -> {
                listProduk.sortBy { it.nama }
                notifyDataSetChanged()
            }
            "Dsc" -> {
                listProduk.sortBy { it.nama }
                listProduk.reverse()
                notifyDataSetChanged()
            }
        }
    }

    fun searchItem(search: String) {
        if (search.isNotEmpty()) {
            val search = saveListProduk.filter { produk ->
                produk.nama!!.trim().toLowerCase()
                    .contains(search.trim().toLowerCase()) || produk.kategori?.nama!!.trim()
                    .toLowerCase().contains(search.trim().toLowerCase())
            }
            listProduk = search as MutableList<Produk>
            notifyDataSetChanged()
        } else {
            listProduk = saveListProduk
            notifyDataSetChanged()
        }
    }
}

