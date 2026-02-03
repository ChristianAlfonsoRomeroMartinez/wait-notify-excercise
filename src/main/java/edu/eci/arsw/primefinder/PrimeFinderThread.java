package edu.eci.arsw.primefinder;

import java.util.LinkedList;
import java.util.List;

public class PrimeFinderThread extends Thread{

	
	int a,b;
	
	private List<Integer> primes;
    //Lock compartido para la pausa y reanudacion
    private final Object pausedLock;
    //Control para guardar el estado y saber cuando esta en pausa y hacer el conteo
    private final Control control;
	
    //Agrego los nuevos parametros que definimos anteriormente
	public PrimeFinderThread(int a, int b, Object pausedLock, Control control) {
		super();
        this.primes = new LinkedList<>();
		this.a = a;
		this.b = b;
		this.pausedLock = pausedLock;
		this.control = control;
	}

        @Override
	public void run(){
            for (int i= a;i < b;i++){		
                //Verificamos si esta en pausa
                checkPaused();				
                if (isPrime(i)){
                    primes.add(i);
                    control.incrementPrimerCount();
                    System.out.println(i);
                }
            }
	}
	
	boolean isPrime(int n) {
	    boolean ans;
            if (n > 2) { 
                ans = n%2 != 0;
                for(int i = 3;ans && i*i <= n; i+=2 ) {
                    ans = n % i != 0;
                }
            } else {
                ans = n == 2;
            }
	    return ans;
	}

	public List<Integer> getPrimes() {
		return primes;
	}

    //metodo para esperar cuando este en pausa
    private void checkPaused() {
        synchronized (pausedLock) {
            while (control.isPaused()) {
                try {
                    pausedLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
	
}
