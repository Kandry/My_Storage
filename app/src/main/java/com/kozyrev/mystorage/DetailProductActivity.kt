package com.kozyrev.mystorage

import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.room.Room

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast

import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.textfield.TextInputEditText
import com.kozyrev.mystorage.db.Product
import com.kozyrev.mystorage.db.ProductDB
import com.squareup.picasso.Picasso


import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable

class DetailProductActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PRODUCT_ID = "id"
        const val PRODUCT_ID = "productId"
        const val REQUEST_GALLERY = 200
    }

    private lateinit var db: ProductDB

    private lateinit var rootViewDetail: CoordinatorLayout
    internal lateinit var imageProduct: PhotoView
    internal lateinit var textTitle: TextInputEditText
    internal lateinit var textPrice: TextInputEditText
    internal lateinit var textCurrency: TextInputEditText

    private var productId: Int = 0
    private var isImageAdding = false
    private var isDelete = false
    internal var originalUri: Uri? = null

    private var onClickListener: View.OnClickListener = View.OnClickListener {
        isImageAdding = true
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        this@DetailProductActivity.startActivityForResult(Intent.createChooser(intent, this@DetailProductActivity.resources.getString(R.string.gallery_title)), REQUEST_GALLERY)
    }

    private val imageUriString: String
        get() = if (originalUri != null)
            originalUri!!.toString()
        else
            ""

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putInt(PRODUCT_ID, productId)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_product)

        initView()
        initDB()

        productId = savedInstanceState?.getInt(PRODUCT_ID)
            ?: if (intent.extras != null) intent.extras!!.get(EXTRA_PRODUCT_ID) as Int else -1

        if (productId > -1)
            downloadData()
        else
            Picasso.get()
                .load(R.drawable.add_image)
                .fit()
                .centerInside()
                .into(imageProduct)
    }

    override fun onPause() {
        super.onPause()

        if (!isImageAdding && !isDelete) {
            if (productId < 0)
                addProduct()
            else
                updateProduct()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_GALLERY -> {
                if (resultCode == Activity.RESULT_OK) {
                    getImageFromGallery(data!!)
                }
                isImageAdding = false
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        rootViewDetail = findViewById(R.id.rootViewDetail)
        textTitle = findViewById(R.id.textTitle)
        textPrice = findViewById(R.id.textPrice)
        textPrice.addTextChangedListener(object : TextWatcher {
            lateinit var beforeText: CharSequence

            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                beforeText = charSequence
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (!isNumeric(charSequence.toString()) && charSequence.toString() != "") {
                    textPrice.error = resources.getString(R.string.real_number_error)
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        textCurrency = findViewById(R.id.textCurrency)
        textCurrency.isEnabled = true
        textCurrency.isFocusable = false
        textCurrency.isFocusableInTouchMode = false
        textCurrency.setOnClickListener(View.OnClickListener { this.showPopupMenu(it) })

        imageProduct = findViewById(R.id.imageProduct)
        imageProduct.setOnClickListener(onClickListener)
    }

    private fun initDB() {
        db = Room.databaseBuilder<ProductDB>(applicationContext, ProductDB::class.java!!, "productdatabase")
            .allowMainThreadQueries()
            .build()
    }

    private fun downloadData() {
        val productSingle = db.productDAO.getProductByIdSingle(productId)
        val observer = object : SingleObserver<Product> {
            override fun onSubscribe(d: Disposable) {}

            override fun onSuccess(product: Product) {
                textTitle.setText(product.name)
                textPrice.setText(product.price)
                textCurrency.setText(product.currency)

                if (product.imageResource != "") {
                    val imageUri = Uri.parse(product.imageResource)
                    originalUri = imageUri
                    imageProduct.contentDescription = product.name
                    Picasso.get()
                        .load(imageUri)
                        .fit()
                        .centerInside()
                        .into(imageProduct)
                } else {
                    Picasso.get()
                        .load(R.drawable.add_image)
                        .fit()
                        .centerInside()
                        .into(imageProduct)
                }
            }

            override fun onError(e: Throwable) {
                e.printStackTrace()
            }
        }
        productSingle.subscribe(observer)
    }

    private fun showPopupMenu(v: View) {
        val popupMenu = PopupMenu(this, v)
        popupMenu.inflate(R.menu.currency_menu)
        popupMenu
            .setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.euro -> {
                        textCurrency.setText(R.string.EUR)
                        true
                    }
                    R.id.dollar -> {
                        textCurrency.setText(R.string.USD)
                        true
                    }
                    R.id.ruble -> {
                        textCurrency.setText(R.string.RUB)
                        true
                    }
                    else -> false
                }
            }

        try {
            val fields = popupMenu.javaClass.declaredFields
            for (field in fields) {
                if ("mPopup" == field.name) {
                    field.isAccessible = true
                    val menuPopupHelper = field.get(popupMenu)
                    val classPopupHelper = Class.forName(menuPopupHelper!!.javaClass.name)
                    val setForceIcons = classPopupHelper.getMethod("setForceShowIcon", Boolean::class.javaPrimitiveType)
                    setForceIcons.invoke(menuPopupHelper, true)
                    break
                }
            }
        } catch (ex: Exception) {
            Log.e("Main", "Error showing menu icons", ex)
        } finally {
            popupMenu.show()
        }
    }

    private fun addProduct() {
        val priceString = if (textPrice.error == null) textPrice.text!!.toString() else ""
        val product = Product(textTitle.text!!.toString(), priceString, textCurrency.text!!.toString(), imageUriString)
        db.productDAO.insert(product)
    }

    private fun updateProduct() {
        val product = db.productDAO.getProductById(productId)
        product.name = textTitle.text!!.toString()
        if (textPrice.error == null) product.price = textPrice.text!!.toString()
        product.currency = textCurrency.text!!.toString()
        product.imageResource = imageUriString
        db.productDAO.update(product)
    }

    fun deleteProduct(view: View) {
        val ad = AlertDialog.Builder(this)
        ad.setTitle(resources.getString(R.string.dialog_title))
        ad.setMessage(resources.getString(R.string.dialog_message))
        ad.setPositiveButton(resources.getString(R.string.positive_button)) { dialogInterface, i ->
            isDelete = true
            val product = db.productDAO.getProductById(productId)
            db.productDAO.delete(product)
            this@DetailProductActivity.onBackPressed()

            Toast.makeText(this@DetailProductActivity.applicationContext, this@DetailProductActivity.resources.getString(R.string.dialog_postmessage),
                Toast.LENGTH_LONG).show()
        }
        ad.setNegativeButton(resources.getString(R.string.negative_button), null)
        ad.setCancelable(true)
        val dialog = ad.create()
        dialog.show()
        dialog.window!!.setBackgroundDrawableResource(R.drawable.customborder)
    }

    private fun getImageFromGallery(data: Intent) {
        originalUri = data.data

        val takeFlags = data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        contentResolver.takePersistableUriPermission(originalUri!!, takeFlags)

        Picasso.get()
            .load(originalUri)
            .fit()
            .centerCrop()
            .into(imageProduct)
    }

    private fun isNumeric(x: String): Boolean {
        return try {
            java.lang.Double.parseDouble(x)
            true
        } catch (e: Exception) {
            false
        }

    }
}
