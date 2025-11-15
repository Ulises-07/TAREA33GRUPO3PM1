package com.example.tarea33grupo3pm1;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements AdaptadorGrabaciones.EscuchadorItemClick {

    private static final String ETIQUETA_LOG = "GrabadoraAudio";
    private static final int CODIGO_PETICION_PERMISOS = 200;
    private final String[] PERMISOS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private EditText entradaNombreGrabacion;
    private TextView pantallaTemporizador;
    private Button btnAlternarGrabacion;
    private Button btnGuardar;
    private Button btnCancelar;
    private LinearLayout layoutControles;
    private RecyclerView vistaReciclable;

    private MediaRecorder grabadoraMedios = null;
    private String rutaArchivoActual = null;
    private boolean estaGrabando = false;
    private int tiempoGrabacionSegundos = 0;

    private AyudanteBDGrabacionesAudio ayudanteBD;
    private AdaptadorGrabaciones adaptador;
    private List<GrabacionAudio> listaGrabaciones;


    private Handler manejadorTemporizador = new Handler(Looper.getMainLooper());
    private final Runnable ejecutableTemporizador = new Runnable() {
        @Override
        public void run() {
            if (estaGrabando) {
                tiempoGrabacionSegundos++;
                pantallaTemporizador.setText(formatearTiempo(tiempoGrabacionSegundos));
                manejadorTemporizador.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ayudanteBD = new AyudanteBDGrabacionesAudio(this);

        inicializarUI();

        listaGrabaciones = new ArrayList<>();
        adaptador = new AdaptadorGrabaciones(this, listaGrabaciones, this);
        vistaReciclable.setLayoutManager(new LinearLayoutManager(this));
        vistaReciclable.setAdapter(adaptador);

        cargarGrabacionesDesdeBD();

        solicitarPermisos();
    }

    private void inicializarUI() {
        entradaNombreGrabacion = findViewById(R.id.input_recording_name);
        pantallaTemporizador = findViewById(R.id.timer_display);
        btnAlternarGrabacion = findViewById(R.id.btn_record_toggle);
        btnGuardar = findViewById(R.id.btn_save);
        btnCancelar = findViewById(R.id.btn_cancel);
        layoutControles = findViewById(R.id.controls_layout);
        vistaReciclable = findViewById(R.id.recycler_view_recordings);

        btnAlternarGrabacion.setOnClickListener(v -> alternarGrabacion());
        btnGuardar.setOnClickListener(v -> guardarGrabacion());
        btnCancelar.setOnClickListener(v -> cancelarGrabacion());

        pantallaTemporizador.setText("00:00");
        establecerEstadoGrabacion(false);
    }

    private void solicitarPermisos() {
        if (!verificarPermisos()) {
            ActivityCompat.requestPermissions(this, PERMISOS, CODIGO_PETICION_PERMISOS);
        }
    }

    private boolean verificarPermisos() {
        int resultadoAudio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return resultadoAudio == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODIGO_PETICION_PERMISOS) {
            boolean concedido = true;
            for (int res : grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED) {
                    concedido = false;
                    break;
                }
            }
            if (!concedido) {
                Toast.makeText(this, "Permisos de grabación son necesarios para la funcionalidad.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void alternarGrabacion() {
        if (!verificarPermisos()) {
            solicitarPermisos();
            return;
        }

        if (estaGrabando) {
            detenerGrabacion();
        } else {
            iniciarGrabacion();
        }
    }

    private void iniciarGrabacion() {
        if (adaptador.reproductorMedios != null) {
            adaptador.detenerReproduccion();
        }

        String marcaTiempo = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        rutaArchivoActual = getExternalCacheDir().getAbsolutePath() + "/" + marcaTiempo + ".3gp";

        grabadoraMedios = new MediaRecorder();
        grabadoraMedios.setAudioSource(MediaRecorder.AudioSource.MIC);
        grabadoraMedios.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        grabadoraMedios.setOutputFile(rutaArchivoActual);
        grabadoraMedios.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            grabadoraMedios.prepare();
            grabadoraMedios.start();
            estaGrabando = true;
            establecerEstadoGrabacion(true);
            Toast.makeText(this, "Grabando...", Toast.LENGTH_SHORT).show();
            iniciarTemporizador();
        } catch (IOException e) {
            Log.e(ETIQUETA_LOG, "Error en iniciarGrabacion()", e);
            Toast.makeText(this, "Error al iniciar la grabación.", Toast.LENGTH_SHORT).show();
        }
    }

    private void detenerGrabacion() {
        if (grabadoraMedios != null) {
            try {
                grabadoraMedios.stop();
                grabadoraMedios.release();
                grabadoraMedios = null;
                estaGrabando = false;
                detenerTemporizador();
                establecerEstadoGrabacion(false);
                Toast.makeText(this, "Grabación detenida. Pulse GUARDAR.", Toast.LENGTH_SHORT).show();
            } catch (RuntimeException e) {
                Log.e(ETIQUETA_LOG, "Error al detener o liberar MediaRecorder: " + e.getMessage());
                Toast.makeText(this, "Error al detener la grabación (demasiado corta?).", Toast.LENGTH_LONG).show();
                new File(rutaArchivoActual).delete();
                reiniciarEstadoGrabacion();
            }
        }
    }

    private void guardarGrabacion() {
        String nombre = entradaNombreGrabacion.getText().toString().trim();
        if (TextUtils.isEmpty(nombre)) {
            nombre = "Grabación " + new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date());
        }

        SQLiteDatabase db = ayudanteBD.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put(AyudanteBDGrabacionesAudio.COLUMNA_NOMBRE, nombre);
        valores.put(AyudanteBDGrabacionesAudio.COLUMNA_RUTA_ARCHIVO, rutaArchivoActual);
        valores.put(AyudanteBDGrabacionesAudio.COLUMNA_DURACION, tiempoGrabacionSegundos);
        valores.put(AyudanteBDGrabacionesAudio.COLUMNA_FECHA, new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));

        long idNuevaFila = db.insert(AyudanteBDGrabacionesAudio.NOMBRE_TABLA, null, valores);

        if (idNuevaFila != -1) {
            Toast.makeText(this, "Grabación guardada con éxito!", Toast.LENGTH_SHORT).show();
            cargarGrabacionesDesdeBD();
            reiniciarEstadoGrabacion();
        } else {
            Toast.makeText(this, "Error al guardar en la base de datos.", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelarGrabacion() {
        if (rutaArchivoActual != null) {
            File archivo = new File(rutaArchivoActual);
            if (archivo.exists()) {
                archivo.delete();
            }
        }
        reiniciarEstadoGrabacion();
        Toast.makeText(this, "Grabación cancelada.", Toast.LENGTH_SHORT).show();
    }

    private void cargarGrabacionesDesdeBD() {
        listaGrabaciones.clear();
        SQLiteDatabase db = ayudanteBD.getReadableDatabase();

        String[] proyeccion = {
                AyudanteBDGrabacionesAudio.COLUMNA_ID,
                AyudanteBDGrabacionesAudio.COLUMNA_NOMBRE,
                AyudanteBDGrabacionesAudio.COLUMNA_RUTA_ARCHIVO,
                AyudanteBDGrabacionesAudio.COLUMNA_DURACION,
                AyudanteBDGrabacionesAudio.COLUMNA_FECHA
        };

        Cursor cursor = db.query(
                AyudanteBDGrabacionesAudio.NOMBRE_TABLA,
                proyeccion,
                null, null, null, null,
                AyudanteBDGrabacionesAudio.COLUMNA_ID + " DESC"
        );

        while (cursor.moveToNext()) {
            long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(AyudanteBDGrabacionesAudio.COLUMNA_ID));
            String nombre = cursor.getString(cursor.getColumnIndexOrThrow(AyudanteBDGrabacionesAudio.COLUMNA_NOMBRE));
            String ruta = cursor.getString(cursor.getColumnIndexOrThrow(AyudanteBDGrabacionesAudio.COLUMNA_RUTA_ARCHIVO));
            int duracion = cursor.getInt(cursor.getColumnIndexOrThrow(AyudanteBDGrabacionesAudio.COLUMNA_DURACION));
            String fecha = cursor.getString(cursor.getColumnIndexOrThrow(AyudanteBDGrabacionesAudio.COLUMNA_FECHA));

            listaGrabaciones.add(new GrabacionAudio(itemId, nombre, ruta, duracion, fecha));
        }
        cursor.close();
        adaptador.notifyDataSetChanged();
    }

    @Override
    public void alHacerClickEnEliminar(GrabacionAudio grabacion) {
        adaptador.detenerReproduccion();

        SQLiteDatabase db = ayudanteBD.getWritableDatabase();
        String seleccion = AyudanteBDGrabacionesAudio.COLUMNA_ID + " = ?";
        String[] argumentosSeleccion = { String.valueOf(grabacion.getId()) };
        int filasEliminadas = db.delete(AyudanteBDGrabacionesAudio.NOMBRE_TABLA, seleccion, argumentosSeleccion);

        if (filasEliminadas > 0) {
            File archivo = new File(grabacion.getRutaArchivo());
            if (archivo.exists()) {
                archivo.delete();
            }

            Toast.makeText(this, "Grabación eliminada: " + grabacion.getNombre(), Toast.LENGTH_SHORT).show();
            cargarGrabacionesDesdeBD();
        } else {
            Toast.makeText(this, "Error al eliminar de la base de datos.", Toast.LENGTH_SHORT).show();
        }
    }


    private void iniciarTemporizador() {
        tiempoGrabacionSegundos = 0;
        manejadorTemporizador.post(ejecutableTemporizador);
    }

    private void detenerTemporizador() {
        manejadorTemporizador.removeCallbacks(ejecutableTemporizador);
    }

    private void establecerEstadoGrabacion(boolean grabando) {
        if (grabando) {
            btnAlternarGrabacion.setText("DETENER");
            btnAlternarGrabacion.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            layoutControles.setVisibility(View.GONE);
            entradaNombreGrabacion.setEnabled(false);
        } else {
            if (rutaArchivoActual != null) {
                btnAlternarGrabacion.setText("DETENER");
                btnAlternarGrabacion.setVisibility(View.GONE);
                layoutControles.setVisibility(View.VISIBLE);
                entradaNombreGrabacion.setEnabled(true);
            } else {
                btnAlternarGrabacion.setText("GRABAR");
                btnAlternarGrabacion.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
                btnAlternarGrabacion.setVisibility(View.VISIBLE);
                layoutControles.setVisibility(View.GONE);
                entradaNombreGrabacion.setEnabled(true);
            }
        }
    }

    private void reiniciarEstadoGrabacion() {
        rutaArchivoActual = null;
        tiempoGrabacionSegundos = 0;
        pantallaTemporizador.setText("00:00");
        entradaNombreGrabacion.setText("");
        establecerEstadoGrabacion(false);
    }

    private String formatearTiempo(int totalSegundos) {
        int minutos = totalSegundos / 60;
        int segundos = totalSegundos % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detenerTemporizador();
        if (grabadoraMedios != null) {
            grabadoraMedios.release();
        }
        adaptador.detenerReproduccion();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (estaGrabando) {
            detenerGrabacion();
        }
        adaptador.detenerReproduccion();
    }
}