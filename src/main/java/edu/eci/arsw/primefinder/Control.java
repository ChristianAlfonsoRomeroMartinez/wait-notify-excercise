/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.primefinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public class Control extends Thread {
    
    private final static int NTHREADS = 3;
    private final static int MAXVALUE = 30000000;
    private final static int TMILISECONDS = 5;

    private final int NDATA = MAXVALUE / NTHREADS;

    private PrimeFinderThread pft[];

    //El lock y la pausa se compartido para los trabajadores
    private final Object pausedLock = new Object();
    //uso volatile porque me asegura la visibilidad entre hilos
    private volatile boolean paused = false;
    private int primeCount = 0;
    //pool de hilos con executerservice
    private ExecutorService pool;


     public boolean isPaused() {
        return paused;
    }
    
    private Control() {
        super();
        this.pft = new  PrimeFinderThread[NTHREADS];

        int i;
        for(i = 0;i < NTHREADS - 1; i++) {
            //Envio el pause y lock al trabajador
            PrimeFinderThread elem = new PrimeFinderThread(i*NDATA, (i+1)*NDATA, pausedLock, this);
            pft[i] = elem;
        }
        pft[i] = new PrimeFinderThread(i*NDATA, MAXVALUE + 1, pausedLock, this);
    }
    
    public static Control newControl() {
        return new Control();
    }

    @Override
    public void run() {

        //Creo el pool y envio a los trabajadores
        pool = Executors.newFixedThreadPool(NTHREADS);

        for(int i = 0;i < NTHREADS;i++ ) {
            pool.submit(pft[i]);
        }
        pool.shutdown();

        //Pausa y conteo
        while (!allFinished()) {
            try {
                Thread.sleep(TMILISECONDS);
                pauseWorkers();
                System.out.println("Pausado. Conteo de primos: " + getPrimeCount());
                waitEnter();
               resumeWorkers();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            
            
            
        }
    }


    public synchronized void incrementPrimerCount() {
        primeCount++;
    }

    public synchronized int getPrimeCount() {
        return primeCount;
    }

    private void pauseWorkers() {
        paused = true;
    }

    private void resumeWorkers() {
        synchronized (pausedLock) {
            paused = false;
            pausedLock.notifyAll();
        }
    }

    private boolean allFinished() {
        if (pool == null) return true;
        return pool.isTerminated();
    }

    private void waitEnter() {
        System.out.println("Presiona ENTER");
        try {
            new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (IOException e) {
            
        }
    }
    
}
