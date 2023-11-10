import java.util.concurrent.Semaphore;

public class FabricaJuguetes {

    public static void main(String[] args) {
        Semaphore semaforoCajaLlena = new Semaphore(0);
        Semaphore semaforoCajaVacia = new Semaphore(1);

        MaxSteel FigurasCaja = new MaxSteel(semaforoCajaLlena, semaforoCajaVacia);

        GuardadoFig guardadoFig = new GuardadoFig(FigurasCaja);
        Empaquetador empaquetador = new Empaquetador(FigurasCaja, semaforoCajaLlena, semaforoCajaVacia);

        guardadoFig.start();
        empaquetador.start();

    }
}

class MaxSteel {

    private int MuñecasEnCaja;
    private int FigurasEnCaja;
    private Semaphore semaforoCajaLlena;
    private Semaphore semaforoCajaVacia;

    public MaxSteel(Semaphore semaforoCajaLlena, Semaphore semaforoCajaVacia) {

        this.MuñecasEnCaja = 0;
        this.FigurasEnCaja = 0;
        this.semaforoCajaLlena = semaforoCajaLlena;
        this.semaforoCajaVacia = semaforoCajaVacia;
    }

    public void agregarJuguete() throws InterruptedException {
        semaforoCajaVacia.acquire(); // Adquirir el semáforo de caja vacía
        FigurasEnCaja++;
        System.out.println("Figura agregada a la caja (" + FigurasEnCaja + " Figuras en la caja).");

        if ((FigurasEnCaja == 10)) {
            System.out.println("figuras completas en la caja, señalando al Empaquetador.");
            semaforoCajaLlena.release(); // Señal al Empaquetador
            FigurasEnCaja = 0;

        }

        semaforoCajaVacia.release(); // Liberar el semáforo de caja vacía
    }

    public void agregarMuñeca() throws InterruptedException {
        semaforoCajaVacia.acquire(); // Adquirir el semáforo de caja vacía

        MuñecasEnCaja++;
        System.out.println("Muñeca agregada a la caja (" + MuñecasEnCaja + " Muñecas en la caja).");

        if ((MuñecasEnCaja == 10)) {
            System.out.println("todas las muñecas estan en la caja, señalando al Empaquetador.");
            semaforoCajaLlena.release(); // Señal al Empaquetador

            MuñecasEnCaja = 0;
        }

        semaforoCajaVacia.release(); // Liberar el semáforo de caja vacía
    }

}

class GuardadoFig extends Thread {
    private MaxSteel FigurasCaja;

    public GuardadoFig(MaxSteel FigurasCaja) {
        this.FigurasCaja = FigurasCaja;

    }

    @Override
    public void run() {

        while (true) {
            try {
                FigurasCaja.agregarJuguete();
                FigurasCaja.agregarMuñeca();
                Thread.sleep(2000); // Simulacion el tiempo de guardado del juguete

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class Empaquetador extends Thread {
    private MaxSteel FigurasCaja;
    private Semaphore semaforoCajaLlena;
    private Semaphore semaforoCajaVacia;

    public Empaquetador(MaxSteel FigurasCaja, Semaphore semaforoCajaLlena, Semaphore semaforoCajaVacia) {

        this.FigurasCaja = FigurasCaja;
        this.semaforoCajaLlena = semaforoCajaLlena;
        this.semaforoCajaVacia = semaforoCajaVacia;
    }

    @Override
    public void run() {

        while (true) {
            try {
                semaforoCajaLlena.acquire(); // Esperar a que la caja esté llena
                System.out.println("Almacen ha tomado una caja llena para guardar.");

                // Simular el proceso de sellado y almacenamiento
                Thread.sleep(1000);

                System.out.println("Nueva caja de 20  juguetes depositada.");
                semaforoCajaVacia.release(); // Señal al embotellador de que hay una caja vacía disponible
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}