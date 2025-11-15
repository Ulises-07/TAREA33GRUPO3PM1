package com.example.tarea33grupo3pm1;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AyudanteBDGrabacionesAudio extends SQLiteOpenHelper {

    private static final String NOMBRE_BASE_DATOS = "AudioRecorder.db";
    private static final int VERSION_BASE_DATOS = 1;

    public static final String NOMBRE_TABLA = "GrabacionesAudio";
    public static final String COLUMNA_ID = "_id";
    public static final String COLUMNA_NOMBRE = "audio_nombre";
    public static final String COLUMNA_RUTA_ARCHIVO = "ruta_archivo";
    public static final String COLUMNA_DURACION = "duracion_segundos";
    public static final String COLUMNA_FECHA = "fecha_grabacion";

    // Script SQL para crear la tabla
    private static final String SQL_CREAR_ENTRADAS =
            "CREATE TABLE " + NOMBRE_TABLA + " (" +
                    COLUMNA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMNA_NOMBRE + " TEXT NOT NULL," +
                    COLUMNA_RUTA_ARCHIVO + " TEXT NOT NULL," +
                    COLUMNA_DURACION + " INTEGER," +
                    COLUMNA_FECHA + " TEXT)";

    private static final String SQL_ELIMINAR_ENTRADAS =
            "DROP TABLE IF EXISTS " + NOMBRE_TABLA;

    public AyudanteBDGrabacionesAudio(Context context) {
        super(context, NOMBRE_BASE_DATOS, null, VERSION_BASE_DATOS);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("AyudanteBD", "Creando tabla: " + SQL_CREAR_ENTRADAS);
        db.execSQL(SQL_CREAR_ENTRADAS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_ELIMINAR_ENTRADAS);
        onCreate(db);
    }
}
