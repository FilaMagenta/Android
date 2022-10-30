package com.arnyminerz.filamagenta.auth

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

class AuthProvider: ContentProvider() {
    /*
     * Always return true, indicating that the
     * provider loaded correctly.
     * TODO: Check connection to the server
     */
    override fun onCreate(): Boolean  = true

    /*
     * Return no type for MIME type
     */
    override fun getType(uri: Uri): String?  = null

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0
}
