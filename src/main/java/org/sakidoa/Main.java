package org.sakidoa;

import org.sakidoa.busnetwork.BusNetwork;
import org.sakidoa.core.NetworkTopology;
import org.sakidoa.core.enums.ConsoleColor;
import org.sakidoa.fullyconnectednetwork.FullyConnectedNetwork;
import org.sakidoa.hypercubenetwork.HyperCubeNetwork;
import org.sakidoa.meshnetwork.MeshNetwork;
import org.sakidoa.ringnetwork.RingNetwork;
import org.sakidoa.starnetwork.StarNetwork;
import org.sakidoa.switchednetwork.SwitchedNetwork;
import org.sakidoa.treenetwork.TreeNetwork;

import java.util.Scanner;

public class Main {
    private static final int MESSAGE_DELAY_MS = 500;
    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        try {
            showWelcome();
            int topologyOption = getTopologySelection();
            int nodeCount = getNodeCount(topologyOption);

            NetworkTopology topology = createTopology(topologyOption);
            executeSimulation(topology, nodeCount);
        } catch (Exception e) {
            handleError("Simulation error", e);
        } finally {
            closeResources();
        }
    }

    private void showWelcome() {
        printColored("╔═══════════════════════════════════════════╗", ConsoleColor.CYAN);
        printColored("║     SIMULADOR DE TOPOLOGÍAS DE RED        ║", ConsoleColor.CYAN);
        printColored("╚═══════════════════════════════════════════╝", ConsoleColor.CYAN);
        System.out.println();
        showTopologyOptions();
    }

    private void showTopologyOptions() {
        printColored("Topologías disponibles:", ConsoleColor.BRIGHT_BLUE);
        System.out.println("  1. Mesh Network");
        System.out.println("  2. Star Network");
        System.out.println("  3. Fully Connected Network");
        System.out.println("  4. Switched Network");
        System.out.println("  5. Bus Network");
        System.out.println("  6. Ring Network");
        System.out.println("  7. HyperCube Network");
        System.out.println("  8. Tree Network");
        System.out.println();
    }

    private int getTopologySelection() {
        return getValidatedInput("Selecciona una opción (1-8): ", 1, 8);
    }

    private int getNodeCount(int topologyOption) {
        return switch (topologyOption) {
            case 6 -> getRingNodeCount();
            case 7 -> getHyperCubeNodeCount();
            default -> getStandardNodeCount();
        };
    }

    private int getRingNodeCount() {
        return getValidatedInput("Cantidad de nodos (mínimo 3): ", 3, Integer.MAX_VALUE);
    }

    private int getHyperCubeNodeCount() {
        printColored("HyperCube requiere un número de nodos que sea potencia de 2", ConsoleColor.YELLOW);
        printColored("Opciones válidas: 2, 4, 8, 16, 32, 64, 128, 256, etc.", ConsoleColor.YELLOW);

        while (true) {
            int input = getValidatedInput("Cantidad de nodos: ", 2, Integer.MAX_VALUE);
            if (isPowerOfTwo(input)) {
                return input;
            }
            printColored("Por favor, ingresa un número que sea potencia de 2", ConsoleColor.RED);
        }
    }

    private int getStandardNodeCount() {
        return getValidatedInput("Cantidad de nodos (mínimo 2): ", 2, Integer.MAX_VALUE);
    }

    private boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }

    private int getValidatedInput(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int input = scanner.nextInt();
                if (input >= min && input <= max) {
                    return input;
                }
                printColored("Por favor, ingresa un valor entre " + min + " y " + max, ConsoleColor.YELLOW);
            } catch (Exception e) {
                printColored("Por favor, ingresa un número válido", ConsoleColor.RED);
                scanner.nextLine();
            }
        }
    }

    private NetworkTopology createTopology(int option) {
        return switch (option) {
            case 1 -> new MeshNetwork();
            case 2 -> new StarNetwork();
            case 3 -> new FullyConnectedNetwork();
            case 4 -> new SwitchedNetwork();
            case 5 -> new BusNetwork();
            case 6 -> new RingNetwork();
            case 7 -> new HyperCubeNetwork();
            case 8 -> new TreeNetwork();
            default -> throw new IllegalArgumentException("Opción de topología inválida: " + option);
        };
    }

    private void executeSimulation(NetworkTopology topology, int nodeCount) throws InterruptedException {
        configureAndStartNetwork(topology, nodeCount);
        simulateMessages(topology, nodeCount);
        waitForUserToStop();
        stopNetwork(topology);
    }

    private void configureAndStartNetwork(NetworkTopology topology, int nodeCount) {
        topology.configureNetwork(nodeCount);
        topology.runNetwork();
        printColored("Red iniciada exitosamente", ConsoleColor.GREEN);
    }

    private void simulateMessages(NetworkTopology topology, int nodeCount) throws InterruptedException {
        printColored("Enviando mensajes de prueba...", ConsoleColor.BRIGHT_BLUE);

        for (int i = 0; i < nodeCount; i++) {
            int targetNode = calculateTargetNode(i, nodeCount);
            sendTestMessage(topology, i, targetNode);
            Thread.sleep(MESSAGE_DELAY_MS);
        }
    }

    private int calculateTargetNode(int currentNode, int totalNodes) {
        return (currentNode + 1) % totalNodes;
    }

    private void sendTestMessage(NetworkTopology topology, int from, int to) {
        String message = "Mensaje de prueba del nodo " + from + " al nodo " + to;
        topology.sendMessage(from, to, message);
        printColored("  -> Nodo " + from + " -> Nodo " + to, ConsoleColor.BRIGHT_CYAN);
    }

    private void waitForUserToStop() {
        System.out.println();
        printColored("Simulación en ejecución", ConsoleColor.BRIGHT_GREEN);
        System.out.println("Presiona ENTER para detener la simulación...");
        scanner.nextLine();
        scanner.nextLine();
    }

    private void stopNetwork(NetworkTopology topology) {
        topology.shutdownNetwork();
        printColored("Red detenida gracefully", ConsoleColor.PURPLE);
    }

    private void handleError(String context, Exception e) {
        printColored("Error en " + context + ": " + e.getMessage(), ConsoleColor.RED);
        e.printStackTrace();
    }

    private void closeResources() {
        scanner.close();
    }

    private void printColored(String message, ConsoleColor color) {
        System.out.println(color + message + ConsoleColor.RESET);
    }
}