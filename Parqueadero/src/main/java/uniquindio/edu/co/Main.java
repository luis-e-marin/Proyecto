package uniquindio.edu.co;

import uniquindio.edu.co.enums.Rol;
import uniquindio.edu.co.enums.TipoUsuario;
import uniquindio.edu.co.enums.TipoVehiculo;
import uniquindio.edu.co.exceptions.*;
import uniquindio.edu.co.model.Cuenta;
import uniquindio.edu.co.model.Espacio;
import uniquindio.edu.co.model.Parqueadero;
import uniquindio.edu.co.model.Vehiculo;

import java.util.List;
import java.util.Scanner;

public class Main {

    private static Parqueadero parqueadero = new Parqueadero();
    private static Cuenta usuarioActual = null;
    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("     SISTEMA DE GESTIÓN DE PARQUEADERO - PARKUQ     ");
        System.out.println("          Universidad del Quindío                  ");
        System.out.println("==================================================\n");

        if (!hacerLogin()) {
            System.out.println("Demasiados intentos fallidos. Saliendo...");
            return;
        }

        int opcion;
        do {
            mostrarMenu();
            opcion = sc.nextInt();
            sc.nextLine(); // limpiar buffer

            switch (opcion) {
                case 1 -> registrarIngreso();
                case 2 -> registrarSalida();
                case 3 -> consultarVehiculosDentro();
                case 4 -> consultarEspacios();
                case 5 -> gestionarEspacios();
                case 6 -> generarReporte();
                case 0 -> System.out.println("Saliendo del sistema...");
                default -> System.out.println("Opción inválida.");
            }
        } while (opcion != 0);

        sc.close();
    }

    private static boolean hacerLogin() {
        System.out.println("=== INICIO DE SESIÓN ===");

        for (int intentos = 1; intentos <= 3; intentos++) {
            System.out.print("Usuario: ");
            String user = sc.nextLine().trim();

            System.out.print("Clave: ");
            String pass = sc.nextLine().trim();

            // Cuentas de prueba
            Cuenta operador = new Cuenta("operador", "1234", Rol.OPERADOR, null);
            Cuenta admin = new Cuenta("admin", "admin", Rol.ADMIN, null);

            if (operador.login(user, pass)) {
                usuarioActual = operador;
                System.out.println("✓ Login exitoso como Operador");
                return true;
            }
            if (admin.login(user, pass)) {
                usuarioActual = admin;
                System.out.println("✓ Login exitoso como Administrador");
                return true;
            }

            System.out.println("✗ Credenciales incorrectas. Intentos restantes: " + (3 - intentos));
        }
        return false;
    }

    private static void mostrarMenu() {
        System.out.println("\n--- MENÚ PRINCIPAL ---");
        System.out.println("1. Registrar ingreso de vehículo");
        System.out.println("2. Registrar salida de vehículo");
        System.out.println("3. Consultar vehículos dentro");
        System.out.println("4. Consultar espacios");
        System.out.println("5. Gestionar espacios (deshabilitar/habilitar)");
        System.out.println("6. Reporte del día");
        System.out.println("0. Salir");
        System.out.print("Opción: ");
    }

    private static void registrarIngreso() {
        System.out.print("Placa: ");
        String placa = sc.nextLine().trim().toUpperCase();

        System.out.print("Tipo (1=Carro, 2=Moto, 3=Bicicleta): ");
        int tipoInt = sc.nextInt();
        sc.nextLine();

        TipoVehiculo tipo = switch (tipoInt) {
            case 1 -> TipoVehiculo.CARRO;
            case 2 -> TipoVehiculo.MOTO;
            case 3 -> TipoVehiculo.BICICLETA;
            default -> null;
        };

        if (tipo == null) {
            System.out.println("Tipo inválido.");
            return;
        }

        System.out.print("Nombre del conductor: ");
        String nombre = sc.nextLine().trim();

        System.out.print("Identificación: ");
        String id = sc.nextLine().trim();

        Vehiculo vehiculo = new Vehiculo(placa, tipo, nombre, id, "");

        try {
            parqueadero.registrarIngreso(vehiculo);
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage());
        }
    }

    private static void registrarSalida() {
        System.out.print("Placa del vehículo que sale: ");
        String placa = sc.nextLine().trim();

        try {
            double valor = parqueadero.registrarSalida(placa);
            System.out.println("Valor total a pagar: $" + String.format("%.0f", valor));
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage());
        }
    }

    private static void consultarVehiculosDentro() {
        List<Vehiculo> dentro = parqueadero.getVehiculosDentro();
        if (dentro.isEmpty()) {
            System.out.println("No hay vehículos dentro del parqueadero.");
            return;
        }
        System.out.println("\nVEHÍCULOS DENTRO DEL PARQUEADERO:");
        for (Vehiculo v : dentro) {
            System.out.println(v);
        }
    }

    private static void consultarEspacios() {
        System.out.println("\nESTADO DE ESPACIOS:");
        for (Espacio e : parqueadero.getEspacios()) {
            System.out.println(e);
        }
        System.out.println("Total: " + parqueadero.getTotalEspacios() +
                " | Ocupados: " + parqueadero.getEspaciosOcupados() +
                " | Disponibles: " + parqueadero.getEspaciosDisponiblesCount());
    }

    private static void gestionarEspacios() {
        if (!usuarioActual.esAdministrador()) {
            System.out.println("✗ Solo el administrador puede gestionar espacios.");
            return;
        }

        System.out.print("Código del espacio: ");
        String codigo = sc.nextLine().trim();

        System.out.print("1. Deshabilitar | 2. Habilitar → ");
        int op = sc.nextInt();
        sc.nextLine();

        if (op == 1) {
            parqueadero.deshabilitarEspacio(codigo);
        } else if (op == 2) {
            parqueadero.habilitarEspacio(codigo);
        } else {
            System.out.println("Opción inválida.");
        }
    }

    private static void generarReporte() {
        System.out.println("\n=== REPORTE DEL DÍA ===");
        System.out.println("Total espacios       : " + parqueadero.getTotalEspacios());
        System.out.println("Espacios ocupados    : " + parqueadero.getEspaciosOcupados());
        System.out.println("Vehículos dentro     : " + parqueadero.getVehiculosDentro().size());
        System.out.println("Tarifas pendientes de implementar completamente");
    }

}
