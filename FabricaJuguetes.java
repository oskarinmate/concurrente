import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

public class FabricaJuguetes {

    public static void main(String[] args) {

        // creacion de los Semaphore
        Semaphore semaforoCajaLlena = new Semaphore(0);
        Semaphore semaforoCajaVacia = new Semaphore(1);

        // lugar en donde le pedimos el numero de cajas que vamos a producir
        int requerido = 0;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Ingresa el número de cajas de juguetes que necesitas:");
        requerido = scanner.nextInt();

        CountDownLatch countDownLatch = new CountDownLatch(2);

        // creacion de los objetos para mandarlos a llamar
        MaxSteel FigurasCaja = new MaxSteel(semaforoCajaLlena, semaforoCajaVacia, requerido);
        GuardadoFig guardadoFig = new GuardadoFig(FigurasCaja);
        Empaquetador empaquetador = new Empaquetador(FigurasCaja, semaforoCajaLlena, semaforoCajaVacia);
        Distribuidor distribuidor = new Distribuidor(FigurasCaja, countDownLatch);

        guardadoFig.start();

        try {
            // esperar que ambos threads terminen su ejecucion
            guardadoFig.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        empaquetador.start();
        distribuidor.start();

        try {
            // esperar que ambos threads terminen su ejecucion
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class MaxSteel {

    // se encarga de tener dentro los metodos de empaquetado de los juguetes
    // y de ocupar los Semaphore para obtener sus permisos y llenar las cajas

    private int MuñecasEnCaja;
    private int FigurasEnCaja;
    private Semaphore semaforoCajaLlena;
    private Semaphore semaforoCajaVacia;
    private int requerido;

    public MaxSteel(Semaphore semaforoCajaLlena, Semaphore semaforoCajaVacia, int requerido) {

        this.MuñecasEnCaja = 0;
        this.FigurasEnCaja = 0;
        this.semaforoCajaLlena = semaforoCajaLlena;
        this.semaforoCajaVacia = semaforoCajaVacia;
        this.requerido = requerido;
    }

    // metodo para obtener el valor de la variable requerido
    public int getRequerido() {
        return requerido;
    }

    // metodo para actualizar el valor de la variable despues de la produccion de
    // una caja
    public synchronized void cajaProducida() {
        requerido--;
    }

    // creacion de metodos para agregar figuras, usamos synchronized para evitar
    // problemas de concurrencia, especificamente la inconsistencia de los datos,
    // como la variable requerido, fue un dolor de cabeza eso jajaja
    public synchronized void agregarJuguete() throws InterruptedException {
        semaforoCajaVacia.acquire(); // Adquirir el permiso de caja vacía
        FigurasEnCaja++;
        System.out.println("Figura agregada a la caja (" + FigurasEnCaja + " Figuras en la caja).");

        if ((FigurasEnCaja == 10 && MuñecasEnCaja == 10)) {
            System.out.println("figuras completas en la caja, señalando al Empaquetador.");
            semaforoCajaLlena.release(); // Señal al Empaquetador
            FigurasEnCaja = 0;
            MuñecasEnCaja = 0;
            cajaProducida();

        }

        semaforoCajaVacia.release(); // Liberar el semáforo de caja vacía
    }

    public synchronized void agregarMuñeca() throws InterruptedException {
        semaforoCajaVacia.acquire(); // Adquirir el semáforo de caja vacía

        MuñecasEnCaja++;
        System.out.println("Muñeca agregada a la caja (" + MuñecasEnCaja + " Muñecas en la caja).");

        if ((FigurasEnCaja == 10 && MuñecasEnCaja == 10)) {
            System.out.println("todas las muñecas estan en la caja, señalando al Empaquetador.");
            semaforoCajaLlena.release(); // Señal al Empaquetador

            FigurasEnCaja = 0;
            MuñecasEnCaja = 0;
            cajaProducida();
        }

        semaforoCajaVacia.release(); // Liberar el semáforo de caja vacía
    }

}

class GuardadoFig extends Thread {

    // clase que nos permite emplear los metodos anteriormentecreados en MaxSteel
    // class
    // simula tambien un tiempo de empaquetado

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

                // revisa si el numero requerido ya llego a 0 para parar la produccion
                if (FigurasCaja.getRequerido() <= 0) {
                    break;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class Empaquetador extends Thread {

    // comprueba si la caja está llena y libera el permiso de una caja nueva

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
                semaforoCajaVacia.release(); // Señal de que hay una caja vacía disponible

                // revisa si el numero requerido ya llego a 0 para parar la produccion, ambas
                // clases lo tienen porque son hilos, necesitamos parar ambos hilos

                if (FigurasCaja.getRequerido() <= 0) {
                    System.out.println("las cajas solicitadas están listas...");
                    break;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class Distribuidor extends Thread {
    private MaxSteel FigurasCaja;
    private CountDownLatch countDownLatch;

    public Distribuidor(MaxSteel FigurasCaja, CountDownLatch countDownLatch) {
        this.FigurasCaja = FigurasCaja;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        while (true) {
            try {
                FigurasCaja.cajaProducida(); // Informa que se ha producido una caja

                // Simular el tiempo de distribución de la caja a la tienda
                Thread.sleep(3000);

                System.out.println("Caja de juguetes entregada a la tienda.");

                // revisa si todas las cajas requeridas ya se han entregado
                if (FigurasCaja.getRequerido() <= 0) {
                    System.out.println("Todas las cajas han sido entregadas a la tienda.");
                    break;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                countDownLatch.countDown(); // informa que el thread a terminado
            }
        }
    }
}