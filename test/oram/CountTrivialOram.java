package oram;

import org.junit.Test;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.File;

import util.Utils;
import flexsc.CompEnv;
import flexsc.Flag;
import flexsc.Mode;
import flexsc.PMCompEnv;
import flexsc.Party;

public class CountTrivialOram {

	static int newN = 1024*2;
	public  static void main(String args[]) throws Exception {
		//deleting data results file if alread exits
		File f = new File("AndGateResults/trivialAndGateResults.txt");
		f.delete();
		try {
			for(int i = 2; i <=Integer.parseInt(args[0]); i++) {
				GenRunnable gen = new GenRunnable(12345, i, 3, 32, 4, 8);
				EvaRunnable eva = new EvaRunnable("localhost", 12345);
				Thread tGen = new Thread(gen);
				Thread tEva = new Thread(eva);
				tGen.start();
				Thread.sleep(10);
				tEva.start();
				tGen.join();
			}
		}
		catch (Exception e) {
			System.out.println("N-value must be valid Int");
		}
	}
	
	@Test
	public void runThreads() throws Exception {
		GenRunnable gen = new GenRunnable(12345, 20, 3, 32, 8, 6);
		EvaRunnable eva = new EvaRunnable("localhost", 12345);
		Thread tGen = new Thread(gen);
		Thread tEva = new Thread(eva);
		tGen.start();
		Thread.sleep(10);
		tEva.start();
		tGen.join();
		System.out.print("\n");
	}

	final static int writeCount = 1;
	final static int readCount = 0;//(1 << 7);

	public CountTrivialOram() {
	}

	public static class GenRunnable extends network.Server implements Runnable {
		int port;
		int logN;
		int N;
		int recurFactor;
		int cutoff;
		int capacity;
		int dataSize;
		int logCutoff;

		GenRunnable(int port, int logN, int capacity, int dataSize,
				int recurFactor, int logCutoff) {
			this.port = port;
			this.logN = logN;
			this.N = 1 << logN;
			this.recurFactor = recurFactor;
			this.logCutoff = logCutoff;
			this.cutoff = 1 << logCutoff;
			this.dataSize = dataSize;
			this.capacity = capacity;
		}

		public void run() {
			try {
				listen(port);

				os.write(logN);
				os.write(recurFactor);
				os.write(logCutoff);
				os.write(capacity);
				os.write(dataSize);
				os.flush();

//				System.out.println("\nlogN recurFactor  cutoff capacity dataSize");
//				System.out.println(logN + " " + recurFactor + " " + cutoff
//						+ " " + capacity + " " + dataSize);

				@SuppressWarnings("unchecked")
				CompEnv<Boolean> env = CompEnv.getEnv(Mode.COUNT, Party.Alice, this);
				LinearScanOram<Boolean> client = new LinearScanOram<Boolean>(
						env, N, dataSize);

				//write results to doc -> peaky
				PrintWriter writer = new PrintWriter(new FileWriter("AndGateResults/trivialAndGateResults.txt", true));
				for (int i = 0; i < writeCount; ++i) {
					int element = i % N;

					Flag.sw.ands = 0;
					Boolean[] scData = env.inputOfAlice(Utils
							.fromInt(element, dataSize));
					os.flush();
//					Flag.sw.startTotal();
					((PMCompEnv)env).statistic.flush();
					client.write(client.lib.toSignals(element), scData);
					System.out.println(logN+"\t"+((PMCompEnv)env).statistic.andGate);
					//Write to file -> Peaky
					writer.println(logN+" "+((PMCompEnv)env).statistic.andGate);

//					double t = Flag.sw.stopTotal();
//					System.out.println(Flag.sw.ands + " " + t / 1000000000.0
//							+ " " + Flag.sw.ands / t * 1000);
//					Flag.sw.addCounter();
//
//					Runtime rt = Runtime.getRuntime();
//					double usedMB = (rt.totalMemory() - rt.freeMemory()) / 1024.0 / 1024.0;
//					System.out.println("mem: " + usedMB);
				}
				writer.close();


				for (int i = 0; i < readCount; ++i) {
					int element = i % N;
					Boolean[] scb = client.read(client.lib
							.toSignals(element));
					boolean[] b = env.outputToAlice(scb);

					// Assert.assertTrue(Utils.toInt(b) == element);
					if (Utils.toInt(b) != element)
						System.out.println("inconsistent: " + element + " "
								+ Utils.toInt(b));
				}

				os.flush();

				disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public static class EvaRunnable extends network.Client implements Runnable {

		String host;
		int port;

		EvaRunnable(String host, int port) {
			this.host = host;
			this.port = port;
		}

		public void run() {
			try {
				connect(host, port);

				int logN = is.read();
				int recurFactor = is.read();
				int logCutoff = is.read();
				int cutoff = 1 << logCutoff;
				int capacity = is.read();
				int dataSize = is.read();

				int N = 1 << logN;
//				System.out
//						.println("\nlogN recurFactor  cutoff capacity dataSize");
//				System.out.println(logN + " " + recurFactor + " " + cutoff
//						+ " " + capacity + " " + dataSize);

				@SuppressWarnings("unchecked")
				CompEnv<Boolean> env = CompEnv.getEnv(Mode.COUNT, Party.Bob, this);
				LinearScanOram<Boolean> server = new LinearScanOram<Boolean>(
						env, N, dataSize);
				
				for (int i = 0; i < writeCount; ++i) {
					int element = i % N;
					Boolean[] scData = server.env
							.inputOfAlice(new boolean[dataSize]);
//					Flag.sw.startTotal();
					server.write(server.lib.toSignals(element), scData);
//					 Flag.sw.stopTotal();
//					 Flag.sw.addCounter();
//					printStatistic();
				}

				int cnt = 0;
				for (int i = 0; i < readCount; ++i) {
					int element = i % N;
					Boolean[] scb = server.read(server.lib
							.toSignals(element));
					server.env.outputToAlice(scb);
					if (i % N == 0)
						System.out.println(cnt++);
				}

				disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}