public class Memoria {
    private int dato[];         // Memoria de datos
    private int instruccione[]; // Memoria de instrucciones

    private final int TAMDATOS = 96;
    private final int TAMINSTRUCCIONES = 640;

    public Memoria(){
        dato = new int[TAMDATOS];
        instruccione = new int[TAMINSTRUCCIONES];

        for(int i = 0; i < TAMDATOS; ++i)
            dato[i] = 1;
    }

    /**
     * Escribe una nueva instruccion en la memoria principal de instrucciones según las indicaciones
     * @param dir Dirección de memoria en donde se almacenará el bloque
     * @param instruccion Bloque de instrucción a ser guardado
     */
    public void escribirInstruccion(int dir, int[] instruccion){
        dir -= 384;

        // Si la dirección es negativa o empieza después del inicio del último bloque
        // Indique el error y salga del programa.
        if(dir < 0 || dir > TAMINSTRUCCIONES){
            System.out.println("La dirección de memoria " + dir + " no existe, Revise el código");
            System.exit(-1);
        }

        // Se escribe la instrucción en memoria
        for(int i = 0; i < instruccion.length; ++i){
            instruccione[dir + i] = instruccion[i];
        }
    }

    /**
     * Escribe un nuevo bloque de datos en la memoria principal de datos según las indicaciones
     * @param dir Dirección de memoria en donde se almacenará el bloque
     * @param bloque Bloque de datos a ser guardado
     */
    public void escribirBloqueDatos(int dir, int[] bloque){
        // Si la dirección es negativa o empieza después del inicio del último bloque
        // Indique el error y salga del programa.
        if(dir < 0 || dir > TAMDATOS * 4){
            System.out.println("La dirección de memoria " + dir + " no existe, Revise el código");
            System.exit(-1);
        }

        int numBloque = dir/16;
        int posicionMem = numBloque * 4;

        // Se escribe el bloque de datos en la memoria
        for(int offset = 0; offset < 4; ++offset)
            dato[posicionMem + offset] = bloque[offset];
    }

    /**
     * Lee un bloque de instrucciones y lo carga en un vector para ser enviado a procesar
     * @param numBloque Número de bloque que se desea leer
     * @param bloque En donde se almacena todas las instrucciones que se van a subir al caché
     */
    public void leerBloqueInstrucciones(int numBloque, int[] bloque){
        int posicionMem = numBloque * 16;

        // Si la dirección es negativa o empieza después del inicio del último bloque
        // Indique el error y salga del programa.
        if(posicionMem < 0 || posicionMem > TAMINSTRUCCIONES){
            System.out.println("La dirección de memoria " + posicionMem + " no existe, Revise el código");
            System.exit(-1);
        }

        // Se escribe en el vector auxiliar los datos del bloque de instrucción
        for(int offset = 0; offset < 16; ++offset)
            bloque[offset] = instruccione[posicionMem + offset];
    }

    /**
     * Lee un bloque de datos y lo carga en el vector para ser transmitido al solicitante
     * @param dir Dirección del bloque de datos que quiere ser transmitido
     * @param bloque En donde se va a almacerar el bloque de datos solicitados
     */
    public void leerBloqueDatos(int dir, int[] bloque){
        // Si la dirección es negativa o empieza después del inicio del último bloque
        // Indique el error y salga del programa.
        if(dir < 0 || dir > TAMDATOS * 4){
            System.out.println("La dirección de memoria " + dir + " no existe, Revise el código");
            System.exit(-1);
        }

        int numBloque = dir/16;
        int posicionMem = (numBloque % 16) / 4;

        // Guarda en el vector auxiliar el bloque de datos deseado
        for(int offset = 0; offset < 4; ++offset)
            bloque[offset] = dato[posicionMem + offset];
    }

    /**
     * Imprime en pantalla cada uno de los datos que se encuentran almacenados en la memoria
     */
    public void print(){
        int offset = 0;
        int dir = 0;

        System.out.println("\n*************************************************************************");

        for(int fila = 0; fila < 6; ++fila){

            System.out.print("Dir  ");
            for(int numDir = 0; numDir < 16; ++numDir, ++dir)
                System.out.print("\t" + dir);

            System.out.print("\nDato ");

            for(int col = 0; col < 16; ++col, ++offset)
                System.out.print("\t" + dato[offset]);

            System.out.print("\n");
        }

        System.out.println("*************************************************************************");
    }
}
