public class CacheDatosD implements CacheDatos{
    private int entrada[][];        // Vector de bloque de datos
    private int etiqueta[];         // Vector que indica el número del bloque en cierta entrada
    private char estado[];          // Vector que indica el estado del bloque en cierta entrada
    private Memoria memoria;        // Referencia de la memoria principal
    private CacheDatos otraCache;  // Referencia de la otra caché de datos
    private int rl;                 // Referencia de el RL del núcleo

    private final int ENTRADASCACHE = 8;
    private final int TAMENTRADA = 4;

    public CacheDatosD(Memoria memoria){
        entrada = new int[TAMENTRADA][ENTRADASCACHE];
        etiqueta = new int[ENTRADASCACHE];
        estado = new char[ENTRADASCACHE];
        this.memoria = memoria;

        for(int i = 0; i < ENTRADASCACHE; ++i) {
            etiqueta[i] = -1;
            estado[i] = 'I';
        }
    }

    /**
     * Verifica que el bloque de datos al que pertenece el dato que se desea escribir este en el caché, si no lo está
     * lo trae del otro caché o la memoria y luego escribe en ese bloque el dato deseado
     * @param dir Dirección del memoria en donde se desea escribir el dato
     * @param dato Dato que se desea escribir
     * @return numCiclos Cantidad de ciclos necesarios para realizar la instrucción
     */
    @Override
    public int escribirDato(int dir, int dato){
        int numDato = (dir % 16) / 4;
        int numBloque = dir / 16;
        int numBloqueCache = numBloque % ENTRADASCACHE;
        int numCiclos = 1;

        // Si el bloque no existe en nuestro caché carguelo y si existe verifique que el otro caché no lo tengo o lo
        // si lo tiene que lo tenga invalido
        if(!existeBloque(numBloque))
            numCiclos = cargarBloque(dir, numBloque);
        else
            otraCache.cambiarBandera(numBloque, 'I');

        // Se guarda el nuevo valor y se cambia la bandera a modificado
        entrada[numDato][numBloqueCache] = dato;
        estado[numBloqueCache] = 'M';

        return numCiclos;
    }

    /**
     * Verifica que el bloque en donde se encuentra la dirección de memoria este en el caché, si no lo está lo trae del
     * otro caché o la memoria principal y luego lee el dato deseado
     * @param dir Dirección de memoria que se desea leer
     * @param datos El dato que se leyó en esa dirección de memoria y numCiclos número de ciclos necesarios para hacer la escritura
     */
    @Override
    public void leerDato(int dir, int[] datos){
        int numDato = (dir % 16) / 4;
        int numBloque = dir / 16;
        int numBloqueCache = numBloque % ENTRADASCACHE;

        datos[1] = 1; // Un ciclos del reloj si leo de mi caché

        // Si el bloque de datos no existe en el caché carguelo
        if(!existeBloque(numBloque))
            datos[1] = cargarBloque(dir, numBloque);

        // Escriba el dato deseado en la primera entrada del vector, la segunda será para la cantidad de ciclos
        datos[0] = entrada[numDato][numBloqueCache];
    }

    /**
     * Verifica que el bloque de caché en donde se va a colocar el nuevo bloque no este modificado, si lo está lo guarda
     * en memoria. Luego carga el bloque deseado desde la otra caché(si lo tiene) o desde la memoria principal
     * @param dir De memoria que se desea cargar
     * @param numBloque Número del bloque que se desea cargar
     */
    @Override
    public int cargarBloque(int dir, int numBloque){
        int[] bloque = new int[4];
        int numBloqueCache = numBloque % ENTRADASCACHE;
        int numCiclos = 0;

        // Si el bloque esta modificado guardelo en memoria antes de cambiarlo
        if(estado[numBloqueCache] == 'M') {
            for(int i = 0; i < TAMENTRADA; ++i)
                bloque[i] = entrada[i][numBloqueCache];

            memoria.escribirBloqueDatos(etiqueta[numBloqueCache] * 16, bloque);
            numCiclos = 32;
        }

        // Trae el bloque de la otra caché o la memoria principal
        if(otraCache.existeBloque(numBloque)) {
            otraCache.enviarBloque(numBloque, bloque);
            numCiclos += 2;
        }
        else {
            memoria.leerBloqueDatos(dir, bloque);
            numCiclos += 32;
        }

        // Copia el bloque en el caché
        for(int i = 0; i < TAMENTRADA; ++i)
            entrada[i][numBloqueCache] = bloque[i];

        estado[numBloqueCache] = 'C';
        etiqueta[numBloqueCache] = numBloque;

        return numCiclos;
    }

    /**
     * Busca el bloque de memoria solicitado en la caché para enviarlo a la otra caché. Si este bloque está modificado
     * lo almacena en memoria para que este compartido
     * @param numBloque Número del bloque que se desea enviar
     * @param bloque Vector en donde se puede almacenar el bloque para transmitir los datos
     */
    @Override
    public void enviarBloque(int numBloque, int[] bloque){
        int numBloqueCache = numBloque % ENTRADASCACHE;

        // Si el bloque que se desea enviar esta modificado lo guarda en memoria
        if(estado[numBloqueCache] == 'M'){
            int[] bloqueGuardar = new int[TAMENTRADA];

            for(int i = 0; i < TAMENTRADA; ++i)
                bloqueGuardar[i] = entrada[i][numBloqueCache];

            memoria.escribirBloqueDatos(etiqueta[numBloqueCache] * 16, bloqueGuardar);
            estado[numBloqueCache] = 'C';
        }

        // Carga el bloque en el vector para ser transmitido
        for(int i = 0; i < TAMENTRADA; ++i)
            bloque[i] = entrada[i][numBloqueCache];
    }

    /**
     * Busca en el caché si el bloque deseado se encuentra en el
     * @param numBloque Número del bloque que se desea verificar si existe
     * @return False si el bloque deseado no existe en el caché y True si existe
     */
    @Override
    public boolean existeBloque(int numBloque){
        boolean existe = false;
        int numBloqueCache = numBloque % ENTRADASCACHE;

        // Si esta en el caché y no esta inválido se puede usar
        if(numBloque == etiqueta[numBloqueCache] && estado[numBloqueCache] != 'I')
            existe = true;

        return existe;
    }

    /**
     * Busca si el bloque deseado esta en la caché, si lo esta cambia el valor de su bandera de estado
     * @param numBloque Número del bloque deseado
     * @param bandera Nuevo valor de la bandera de ese bloque de datos
     */
    @Override
    public void cambiarBandera(int numBloque, char bandera){
        int numBloqueCache = numBloque % ENTRADASCACHE;

        if(numBloque == etiqueta[numBloqueCache])
            estado[numBloqueCache] = bandera;
    }

    @Override
    public char obtenerBandera(int numBloqueCache){
        return estado[numBloqueCache];
    }

    @Override
    public int getRl() {
        return rl;
    }

    @Override
    public void setRl(int rl) {
        this.rl = rl;
    }

    @Override
    public void setOtraCache(CacheDatos otraCache){
        this.otraCache = otraCache;
    }

    /**
     * Imprime toda la información deseada del caché de datos
     */
    @Override
    public void print(){
        System.out.println("\n*************************************************************************");

        for(int i = 0; i < TAMENTRADA; ++i) {
            for (int j = 0; j < ENTRADASCACHE; ++j)
                System.out.print(entrada[i][j] + "\t\t");
            System.out.print("\n");
        }

        for(int i = 0; i < ENTRADASCACHE; ++i)
            System.out.print(etiqueta[i] + "\t\t");

        System.out.print("\n");

        for(int i = 0; i < ENTRADASCACHE; ++i)
            System.out.print(estado[i] + "\t\t");

        System.out.print("\n");

        System.out.println("*************************************************************************");
    }
}