package org.sakidoa;


import org.sakidoa.core.*;
import org.sakidoa.*;
import org.sakidoa.fullyconnectednetwork.FullyConnectedNetwork;
import org.sakidoa.meshnetwork.MeshNetwork;
import org.sakidoa.starnetwork.StarNetwork;
import org.sakidoa.switchednetwork.SwitchedNetwork;

import java.util.Scanner;

public class Main {
	public static void main(String[] args) throws InterruptedException {
		Scanner scanner = new Scanner(System.in);
		NetworkTopology topology = null;

		System.out.println("=== SIMULADOR DE TOPOLOGÍAS DE RED ===");
		System.out.println("1. Mesh Network");
		System.out.println("2. Star Network");
		System.out.println("3. Fully Connected Network");
		System.out.println("4. Switched Network");
		System.out.print("Selecciona una opción: ");
		int option = scanner.nextInt();

		System.out.print("Cantidad de nodos: ");
		int numNodes = scanner.nextInt();

		switch (option) {
			case 1 -> topology = new MeshNetwork();
			case 2 -> topology = new StarNetwork();
			case 3 -> topology = new FullyConnectedNetwork();
			case 4 -> topology = new SwitchedNetwork();
			default -> {
				System.out.println("Opción inválida.");
				System.exit(0);
			}
		}

		topology.configureNetwork(numNodes);
		topology.runNetwork();

		System.out.println("\nRed iniciada. Enviando mensajes de prueba...");
		
		for (int i = 0; i < numNodes; i++) {
			int to = (i + 1) % numNodes;
			topology.sendMessage(i, to, "Hola de nodo " + i + " a nodo " + to);
			Thread.sleep(500);
		}

		System.out.println("\nPresiona ENTER para detener la red...");
		scanner.nextLine();
		scanner.nextLine();

		topology.shutdownNetwork();
		System.out.println("Red detenida.");
	}
}