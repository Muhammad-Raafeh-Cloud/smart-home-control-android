package com.example.projectdeliverable1.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.projectdeliverable1.models.Device

/**
 * Assignment #03 local persistence layer.
 * Uses SQLiteOpenHelper directly as required. The schema has two related tables:
 * rooms(room_id PK) -> devices(room_id FK).
 */
class SmartHomeDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_ROOMS (
                $COL_ROOM_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_ROOM_NAME TEXT NOT NULL UNIQUE
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_DEVICES (
                $COL_DEVICE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_DEVICE_NAME TEXT NOT NULL,
                $COL_DEVICE_ROOM_ID INTEGER NOT NULL,
                $COL_DEVICE_STATUS TEXT NOT NULL,
                $COL_DEVICE_TYPE TEXT NOT NULL,
                $COL_DEVICE_UPDATED TEXT NOT NULL,
                $COL_DEVICE_DESCRIPTION TEXT NOT NULL,
                FOREIGN KEY($COL_DEVICE_ROOM_ID) REFERENCES $TABLE_ROOMS($COL_ROOM_ID)
                    ON UPDATE CASCADE ON DELETE RESTRICT
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DEVICES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ROOMS")
        onCreate(db)
    }

    fun seedDefaultDataIfEmpty(defaultDevices: List<Device>) {
        val db = writableDatabase
        if (getDeviceCount(db) > 0) return

        db.beginTransaction()
        try {
            defaultDevices.forEach { device ->
                val roomId = getOrCreateRoom(db, device.location)
                insertDevice(db, device.copy(roomId = roomId))
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getDevices(query: String = "", sortedByName: Boolean = false): List<Device> {
        val cleanQuery = query.trim()
        val whereClause = if (cleanQuery.isBlank()) {
            ""
        } else {
            """
            WHERE d.$COL_DEVICE_NAME LIKE ?
               OR r.$COL_ROOM_NAME LIKE ?
               OR d.$COL_DEVICE_TYPE LIKE ?
               OR d.$COL_DEVICE_STATUS LIKE ?
            """.trimIndent()
        }
        val orderClause = if (sortedByName) {
            "ORDER BY d.$COL_DEVICE_NAME COLLATE NOCASE ASC"
        } else {
            "ORDER BY d.$COL_DEVICE_ID DESC"
        }
        val args = if (cleanQuery.isBlank()) emptyArray<String>() else Array(4) { "%$cleanQuery%" }

        val cursor = readableDatabase.rawQuery(
            """
            SELECT d.$COL_DEVICE_ID,
                   d.$COL_DEVICE_NAME,
                   r.$COL_ROOM_NAME,
                   d.$COL_DEVICE_STATUS,
                   d.$COL_DEVICE_TYPE,
                   d.$COL_DEVICE_UPDATED,
                   d.$COL_DEVICE_DESCRIPTION,
                   d.$COL_DEVICE_ROOM_ID
            FROM $TABLE_DEVICES d
            INNER JOIN $TABLE_ROOMS r ON d.$COL_DEVICE_ROOM_ID = r.$COL_ROOM_ID
            $whereClause
            $orderClause
            """.trimIndent(),
            args
        )
        return cursor.use { readDevices(it) }
    }

    fun insertDevice(device: Device): Long {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val roomId = getOrCreateRoom(db, device.location)
            val insertedId = insertDevice(db, device.copy(roomId = roomId))
            db.setTransactionSuccessful()
            insertedId
        } finally {
            db.endTransaction()
        }
    }

    fun updateDevice(device: Device): Int {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val roomId = getOrCreateRoom(db, device.location)
            val values = ContentValues().apply {
                put(COL_DEVICE_NAME, device.name)
                put(COL_DEVICE_ROOM_ID, roomId)
                put(COL_DEVICE_STATUS, device.status)
                put(COL_DEVICE_TYPE, device.type)
                put(COL_DEVICE_UPDATED, device.lastUpdated)
                put(COL_DEVICE_DESCRIPTION, device.description)
            }
            val rows = db.update(TABLE_DEVICES, values, "$COL_DEVICE_ID = ?", arrayOf(device.id.toString()))
            db.setTransactionSuccessful()
            rows
        } finally {
            db.endTransaction()
        }
    }

    fun deleteDevice(deviceId: Int): Int =
        writableDatabase.delete(TABLE_DEVICES, "$COL_DEVICE_ID = ?", arrayOf(deviceId.toString()))

    fun getDeviceCount(): Int = getDeviceCount(readableDatabase)

    fun getActiveDeviceCount(): Int {
        val cursor = readableDatabase.rawQuery(
            """
            SELECT COUNT(*) FROM $TABLE_DEVICES
            WHERE UPPER($COL_DEVICE_STATUS) IN ('ON', 'ACTIVE', 'LOCKED', 'ONLINE', 'SCHEDULED')
            """.trimIndent(),
            null
        )
        return cursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }
    }

    private fun insertDevice(db: SQLiteDatabase, device: Device): Long {
        val values = ContentValues().apply {
            put(COL_DEVICE_NAME, device.name)
            put(COL_DEVICE_ROOM_ID, device.roomId)
            put(COL_DEVICE_STATUS, device.status)
            put(COL_DEVICE_TYPE, device.type)
            put(COL_DEVICE_UPDATED, device.lastUpdated)
            put(COL_DEVICE_DESCRIPTION, device.description)
        }
        return db.insertOrThrow(TABLE_DEVICES, null, values)
    }

    private fun getDeviceCount(db: SQLiteDatabase): Int {
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_DEVICES", null)
        return cursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }
    }

    private fun getOrCreateRoom(db: SQLiteDatabase, roomName: String): Int {
        val cleanRoomName = roomName.trim().ifBlank { "Unassigned" }
        val cursor = db.rawQuery(
            "SELECT $COL_ROOM_ID FROM $TABLE_ROOMS WHERE $COL_ROOM_NAME = ?",
            arrayOf(cleanRoomName)
        )
        cursor.use {
            if (it.moveToFirst()) return it.getInt(0)
        }

        val values = ContentValues().apply { put(COL_ROOM_NAME, cleanRoomName) }
        return db.insertOrThrow(TABLE_ROOMS, null, values).toInt()
    }

    private fun readDevices(cursor: Cursor): List<Device> {
        val devices = mutableListOf<Device>()
        while (cursor.moveToNext()) {
            devices.add(
                Device(
                    id = cursor.getInt(0),
                    name = cursor.getString(1),
                    location = cursor.getString(2),
                    status = cursor.getString(3),
                    type = cursor.getString(4),
                    lastUpdated = cursor.getString(5),
                    description = cursor.getString(6),
                    roomId = cursor.getInt(7)
                )
            )
        }
        return devices
    }

    companion object {
        private const val DATABASE_NAME = "smart_home_assignment3.db"
        private const val DATABASE_VERSION = 2

        const val TABLE_ROOMS = "rooms"
        const val TABLE_DEVICES = "devices"

        const val COL_ROOM_ID = "room_id"
        const val COL_ROOM_NAME = "room_name"

        const val COL_DEVICE_ID = "device_id"
        const val COL_DEVICE_NAME = "device_name"
        const val COL_DEVICE_ROOM_ID = "room_id"
        const val COL_DEVICE_STATUS = "status"
        const val COL_DEVICE_TYPE = "type"
        const val COL_DEVICE_UPDATED = "last_updated"
        const val COL_DEVICE_DESCRIPTION = "description"
    }
}
