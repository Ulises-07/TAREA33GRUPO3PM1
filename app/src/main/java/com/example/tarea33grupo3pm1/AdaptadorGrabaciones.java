package com.example.tarea33grupo3pm1;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;


public class AdaptadorGrabaciones extends RecyclerView.Adapter<AdaptadorGrabaciones.ContenedorVistaGrabacion> {

    private final Context contexto;
    private final List<GrabacionAudio> listaGrabaciones;
    private final EscuchadorItemClick escuchador;

    public MediaPlayer reproductorMedios = null;
    private ContenedorVistaGrabacion holderReproduciendoActualmente = null;


    public interface EscuchadorItemClick {
        void alHacerClickEnEliminar(GrabacionAudio grabacion);
    }

    public AdaptadorGrabaciones(Context contexto, List<GrabacionAudio> listaGrabaciones, EscuchadorItemClick escuchador) {
        this.contexto = contexto;
        this.listaGrabaciones = listaGrabaciones;
        this.escuchador = escuchador;
    }

    @NonNull
    @Override
    public ContenedorVistaGrabacion onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(contexto).inflate(R.layout.item_grabacion, parent, false);
        return new ContenedorVistaGrabacion(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ContenedorVistaGrabacion holder, int position) {
        GrabacionAudio grabacion = listaGrabaciones.get(position);
        holder.enlazar(grabacion);

        if (holder == holderReproduciendoActualmente && reproductorMedios != null && reproductorMedios.isPlaying()) {
            holder.btnReproducirPausar.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            holder.btnReproducirPausar.setImageResource(android.R.drawable.ic_media_play);
        }

        holder.btnReproducirPausar.setOnClickListener(v -> alternarReproduccion(holder, grabacion));

        holder.btnEliminar.setOnClickListener(v -> escuchador.alHacerClickEnEliminar(grabacion));
    }

    @Override
    public int getItemCount() {
        return listaGrabaciones.size();
    }

    private void alternarReproduccion(ContenedorVistaGrabacion holder, GrabacionAudio grabacion) {
        if (reproductorMedios != null && reproductorMedios.isPlaying() && holder == holderReproduciendoActualmente) {
            reproductorMedios.pause();
            holder.btnReproducirPausar.setImageResource(android.R.drawable.ic_media_play);
        } else if (reproductorMedios != null && !reproductorMedios.isPlaying() && holder == holderReproduciendoActualmente) {
            reproductorMedios.start();
            holder.btnReproducirPausar.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            detenerReproduccion();

            holderReproduciendoActualmente = holder;
            holder.btnReproducirPausar.setImageResource(android.R.drawable.ic_media_pause);
            iniciarReproduccion(grabacion.getRutaArchivo());
        }
    }

    private void iniciarReproduccion(String rutaArchivo) {
        try {
            File archivo = new File(rutaArchivo);
            if (!archivo.exists()) {
                Toast.makeText(contexto, "Error: Archivo de audio no encontrado.", Toast.LENGTH_SHORT).show();
                detenerReproduccion();
                return;
            }

            reproductorMedios = new MediaPlayer();
            reproductorMedios.setDataSource(rutaArchivo);
            reproductorMedios.prepare();
            reproductorMedios.start();

            reproductorMedios.setOnCompletionListener(mp -> detenerReproduccion());

        } catch (Exception e) {
            Toast.makeText(contexto, "Error al reproducir audio: " + e.getMessage(), Toast.LENGTH_LONG).show();
            detenerReproduccion();
        }
    }

    public void detenerReproduccion() {
        if (reproductorMedios != null) {
            reproductorMedios.stop();
            reproductorMedios.release();
            reproductorMedios = null;
        }

        if (holderReproduciendoActualmente != null) {
            holderReproduciendoActualmente.btnReproducirPausar.setImageResource(android.R.drawable.ic_media_play);
            holderReproduciendoActualmente = null;
        }
    }

    public class ContenedorVistaGrabacion extends RecyclerView.ViewHolder {
        public TextView nombreGrabacion;
        public TextView duracionGrabacion;
        public TextView fechaGrabacion;
        public ImageButton btnReproducirPausar;
        public ImageButton btnEliminar;

        public ContenedorVistaGrabacion(@NonNull View itemView) {
            super(itemView);
            nombreGrabacion = itemView.findViewById(R.id.recording_name);
            duracionGrabacion = itemView.findViewById(R.id.recording_duration);
            fechaGrabacion = itemView.findViewById(R.id.recording_date);
            btnReproducirPausar = itemView.findViewById(R.id.btn_play_pause);
            btnEliminar = itemView.findViewById(R.id.btn_delete);
        }

        public void enlazar(GrabacionAudio grabacion) {
            nombreGrabacion.setText(grabacion.getNombre());
            fechaGrabacion.setText(grabacion.getFechaGrabacion());
            duracionGrabacion.setText(formatearTiempo(grabacion.getDuracionSegundos()));
        }
    }

    private String formatearTiempo(int totalSegundos) {
        int minutos = totalSegundos / 60;
        int segundos = totalSegundos % 60;
        return String.format("%02d:%02d", minutos, segundos);
    }
}
