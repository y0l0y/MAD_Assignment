package com.example.mad_assignment.ui.view

import android.content.Context
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning


/* using google define barcode scanner screen */
fun scanIsbn(
    context: Context,
    onScanned: (String) -> Unit,
    onError: (String) -> Unit
) {
    val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_EAN_13)
        .build()
    GmsBarcodeScanning.getClient(context, options)
        .startScan()
        .addOnSuccessListener { barcode ->
            val code = barcode.rawValue
            if (code.isNullOrBlank()) {
                onError("Could not read the barcode. Please try again.")
            } else {
                onScanned(code)
            }
        }
        .addOnFailureListener {
            onError("The scanner is not available. Please type the ISBN.")
        }
}