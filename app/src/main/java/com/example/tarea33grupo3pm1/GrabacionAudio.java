package com.example.tarea33grupo3pm1;

public class GrabacionAudio {
    private long id;
    private String nombre;
    private String rutaArchivo;
    private int duracionSegundos;
    private String fechaGrabacion;

    public GrabacionAudio(long id, String nombre, String rutaArchivo, int duracionSegundos, String fechaGrabacion) {
        this.id = id;
        this.nombre = nombre;
        this.rutaArchivo = rutaArchivo;
        this.duracionSegundos = duracionSegundos;
        this.fechaGrabacion = fechaGrabacion;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public int getDuracionSegundos() {
        return duracionSegundos;
    }

    public void setDuracionSegundos(int duracionSegundos) {
        this.duracionSegundos = duracionSegundos;
    }

    public String getFechaGrabacion() {
        return fechaGrabacion;
    }

    public void setFechaGrabacion(String fechaGrabacion) {
        this.fechaGrabacion = fechaGrabacion;
    }
}
